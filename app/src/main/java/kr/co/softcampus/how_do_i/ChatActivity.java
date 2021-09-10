
package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import kr.co.softcampus.how_do_i.databinding.ItemReceiveBinding;
import kr.co.softcampus.how_do_i.databinding.ItemSentBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    String uid, destUid, chatRoomUid, filePath ;
    EditText edit_msg;
    CardView btn_send, photo;
    ImageView chat_profile, chat_back ;
    TextView chat_nickname;

    RecyclerView chat_rv;
    LinearLayoutManager manager;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;

    ProgressDialog dialog;

    int peopleCount = 0;

    LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_back = findViewById(R.id.chat_back);
        chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chat_profile = findViewById(R.id.chat_profile);
        chat_nickname = findViewById(R.id.chat_nickname);

        chat_rv = findViewById(R.id.chat_rv);
        manager = new LinearLayoutManager(this);

        Intent intent = getIntent();
        destUid = intent.getStringExtra("destUid");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("loginUser").document(destUid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    String name = (String)document.get("name");
                    String profile = (String)document.get("profile");
                    Glide.with(ChatActivity.this).load(profile).apply(new RequestOptions().circleCrop()).into(chat_profile);
                    chat_nickname.setText(name);
                }
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        photo = findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                startActivityForResult(intent1,25);
            }
        });

        edit_msg = findViewById(R.id.edit_msg);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_msg.getText().length()!=0){
                    ChatModel chatModel = new ChatModel();
                    chatModel.users.put(uid,true);
                    chatModel.users.put(destUid,true);

                    if(chatRoomUid == null){
                        btn_send.setEnabled(false);
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                checkChatRoom();
                            }
                        });
                    }else {
                        ChatModel.Comment comment = new ChatModel.Comment();
                        comment.uid = uid;
                        comment.message = edit_msg.getText().toString();
                        comment.time = ServerValue.TIMESTAMP;

                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        edit_msg.setText("");
                                    }
                                });
                    }
                }else {
                    Toast.makeText(ChatActivity.this, "메세지를 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkChatRoom();

    }

    public void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot item : snapshot.getChildren()){
                            ChatModel chatModel = item.getValue(ChatModel.class);
                            if(chatModel.users.containsKey(destUid)){
                                chatRoomUid = item.getKey();
                                btn_send.setEnabled(true);
                                chat_rv.setLayoutManager(manager);
                                chat_rv.setAdapter(new ChatAdapter());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        filePath = uri.toString();

                                        ChatModel chatModel = new ChatModel();
                                        chatModel.users.put(uid,true);
                                        chatModel.users.put(destUid,true);

                                        if(chatRoomUid == null){
                                            btn_send.setEnabled(false);
                                            FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    checkChatRoom();
                                                }
                                            });
                                        }else {
                                            ChatModel.Comment comment = new ChatModel.Comment();
                                            comment.uid = uid;
                                            comment.message = "photo";
                                            comment.time = ServerValue.TIMESTAMP;
                                            comment.imageUrl = filePath;

                                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            edit_msg.setText("");

                                                        }
                                                    });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class ChatAdapter extends RecyclerView.Adapter {

        ArrayList<ChatModel.Comment> comments;

        final int ITEM_SENT = 1 ;
        final int ITEM_RECEIVE = 2 ;

        public ChatAdapter(){
            comments = new ArrayList<ChatModel.Comment>();
            firebaseFirestore = FirebaseFirestore.getInstance();
            documentReference = firebaseFirestore.collection("loginUser").document(destUid);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        String name = (String)document.get("name");
                        String profile = (String)document.get("profile");
                        String id = (String)document.get("id");
                        String email = (String)document.get("email");
                        loginUser = new LoginUser(name,profile,email,id);

                        getMessageList();
                    }
                }
            });

        }


        public void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for (DataSnapshot item : snapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);

                        comment_modify.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid)
                            .child("comments").updateChildren(readUsersMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
//                                    notifyDataSetChanged();
                                    chat_rv.scrollToPosition(comments.size() - 1);
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == ITEM_SENT){
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent,parent,false);
                return new SentViewHolder(view);
            }else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receive,parent,false);
                return new ReceiverViewHolder(view);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(FirebaseAuth.getInstance().getUid().equals(comments.get(position).uid)){
                return ITEM_SENT;
            }else {
                return ITEM_RECEIVE;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatModel.Comment comment = comments.get(position);

            if(holder.getClass()== SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder) holder;

                if(comment.message.equals("photo")){
                    viewHolder.binding.image.setVisibility(View.VISIBLE);
                    viewHolder.binding.message.setVisibility(View.GONE);
                    Glide.with(holder.itemView.getContext()).load(comment.imageUrl).override(1028).into(viewHolder.binding.image);
                }

                viewHolder.binding.message.setText(comment.message);
                setReadCount(position,((SentViewHolder) holder).binding.chatReadcount);
            }else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

                if(comment.message.equals("photo")){
                    viewHolder.binding.image.setVisibility(View.VISIBLE);
                    viewHolder.binding.message.setVisibility(View.GONE);
                    Glide.with(holder.itemView.getContext()).load(comment.imageUrl).override(1028).into(viewHolder.binding.image);
                }
                viewHolder.binding.message.setText(comment.message);
                setReadCount(position,((ReceiverViewHolder) holder).binding.chatReadcount);
            }
        }


        public void setReadCount(int position, TextView textview){
            if(peopleCount==0){
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Boolean> users = (Map<String, Boolean>) snapshot.getValue();
                                peopleCount = users.size();
                                int count = peopleCount - comments.get(position).readUsers.size();
                                if(count>0){
                                    textview.setVisibility(View.VISIBLE);
                                    textview.setText(String.valueOf(count));
                                }else {
                                    textview.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }else {
                int count = peopleCount - comments.get(position).readUsers.size();
                if(count>0){
                    textview.setVisibility(View.VISIBLE);
                    textview.setText(String.valueOf(count));
                }else {
                    textview.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }


        public class SentViewHolder extends RecyclerView.ViewHolder {

            ItemSentBinding binding;

            public SentViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = ItemSentBinding.bind(itemView);
            }
        }

        public class ReceiverViewHolder extends RecyclerView.ViewHolder{

            ItemReceiveBinding binding;

            public ReceiverViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = ItemReceiveBinding.bind(itemView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(valueEventListener!=null){
            databaseReference.removeEventListener(valueEventListener);
        }
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }
}