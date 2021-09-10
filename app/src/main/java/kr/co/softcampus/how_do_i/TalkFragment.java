package kr.co.softcampus.how_do_i;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class TalkFragment extends Fragment implements onBackPressedListener{

    RecyclerView talk_rv;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private AdView mAdView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_talk, container, false);
        talk_rv = view.findViewById(R.id.talk_rv);
        talk_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        talk_rv.setAdapter(new TalkAdapter());

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.hide();

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        return view;
    }

    @Override
    public void onBackPressed() {
        goToMain();
    }

    private void goToMain(){
        Intent intent = new Intent(getContext(),HomeActivity.class);
        startActivity(intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }

    class TalkAdapter extends RecyclerView.Adapter<TalkAdapter.ViewHolder>{

        ArrayList<ChatModel> chatModels = new ArrayList<>();
        String uid ;
        ArrayList<String> destinationUsers = new ArrayList<>();
        FirebaseFirestore firebaseFirestore;
        DocumentReference documentReference;

        public TalkAdapter(){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            chatModels.clear();
                            for(DataSnapshot item : snapshot.getChildren()){
                                chatModels.add(item.getValue(ChatModel.class));
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        @NonNull
        @Override
        public TalkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.talk_list,parent,false);
            TalkAdapter.ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override

        public void onBindViewHolder(@NonNull TalkAdapter.ViewHolder holder, int position) {

            String destUid = null;
            for(String user : chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destUid = user;
                    destinationUsers.add(destUid);
                }
            }
            firebaseFirestore = FirebaseFirestore.getInstance();
            documentReference = firebaseFirestore.collection("loginUser").document(destUid);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        String name = (String)document.get("name");
                        String profile = (String)document.get("profile");

                        Glide.with(holder.itemView).load(profile)
                                .apply(new RequestOptions().circleCrop())
                                .into(holder.talk_image);
                        holder.talk_title.setText(name);

                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),ChatActivity.class);
                    intent.putExtra("destUid",destinationUsers.get(position));
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getContext(),R.anim.fromright,R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle());
                }
            });


            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            holder.talk_msg.setText(chatModels.get(position).comments.get(lastMessageKey).message);

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long uTime = (long) chatModels.get(position).comments.get(lastMessageKey).time;
            Date date = new Date(uTime);
            holder.talk_time.setText(simpleDateFormat.format(date));
        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView talk_image;
            TextView talk_title, talk_msg, talk_time;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.talk_image = itemView.findViewById(R.id.talk_image);
                this.talk_title = itemView.findViewById(R.id.talk_title);
                this.talk_msg = itemView.findViewById(R.id.talk_msg);
                this.talk_time = itemView.findViewById(R.id.talk_time);
            }
        }
    }
}