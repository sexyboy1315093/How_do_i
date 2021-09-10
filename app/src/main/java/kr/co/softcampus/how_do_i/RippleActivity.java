package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RippleActivity extends AppCompatActivity {

    ArrayList<HashMap<String,Object>> data_list = new ArrayList<HashMap<String, Object>>();
    EditText edit_ripple;
    Button edit_btn;
    String uid, board_id;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;

    RecyclerView ripple_rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ripple);

        Ripple_BoardThread thread2 = new Ripple_BoardThread();
        thread2.start();

        ripple_rv = findViewById(R.id.ripple_rv);
        ripple_rv.setLayoutManager(new LinearLayoutManager(this));
        ripple_rv.setHasFixedSize(true);
        ripple_rv.setAdapter(new RippleAdapter());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        board_id = intent.getStringExtra("board_id");

        edit_ripple = findViewById(R.id.edit_ripple);
        edit_btn = findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RippleThread thread = new RippleThread();
                thread.start();
            }
        });
    }


    class Ripple_BoardThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                String site = "http://192.168.0.102:8090/HowDoI/RippleBoard.jsp";
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder = builder.url(site);

                FormBody.Builder bodybuilder = new FormBody.Builder();
                bodybuilder.add("board_id",board_id);
                FormBody body = bodybuilder.build();
                builder = builder.post(body);

                Request request = builder.build();
                Call call = client.newCall(request);
                RippleBoardCallback callback = new RippleBoardCallback();
                call.enqueue(callback);

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class RippleBoardCallback implements Callback{

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{
                String result = response.body().string();
                JSONArray root = new JSONArray(result);
                data_list.clear();
                for(int i=0; i<root.length(); i++){
                    JSONObject obj = root.getJSONObject(i);
                    String ripple_id = obj.getString("ripple_id");
                    String ripple_name = obj.getString("ripple_name");
                    String ripple_comment = obj.getString("ripple_comment");
                    String ripple_date = obj.getString("ripple_date");
                    String ripple_profile = obj.getString("ripple_profile");

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("ripple_id",ripple_id);
                    map.put("ripple_name",ripple_name);
                    map.put("ripple_comment",ripple_comment);
                    map.put("ripple_date",ripple_date);
                    map.put("ripple_profile", ripple_profile);

                    data_list.add(map);
                    Log.e("123123123",ripple_profile+","+ripple_name);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RippleAdapter adapter = (RippleAdapter) ripple_rv.getAdapter();
                        adapter.notifyDataSetChanged();
                        edit_ripple.setText("");
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class RippleThread extends Thread{
        @Override
        public void run() {
            super.run();
                documentReference = firebaseFirestore.collection("loginUser").document(uid);
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            try{
                                String site = "http://192.168.0.102:8090/HowDoI/RippleForm.jsp";
                                OkHttpClient client = new OkHttpClient();
                                Request.Builder builder = new Request.Builder();
                                builder = builder.url(site);

                                DocumentSnapshot document = task.getResult();
                                String id = (String) document.get("id");
                                String name = (String) document.get("name");
                                String profile = (String) document.get("profile");
                                String ripple = edit_ripple.getText().toString();

                                Date date_origin = new Date();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
                                String date = simpleDateFormat.format(date_origin);

                                FormBody.Builder bodybuilder = new FormBody.Builder();
                                bodybuilder.add("board_id", board_id);
                                bodybuilder.add("id",id);
                                bodybuilder.add("name",name);
                                bodybuilder.add("profile", profile);
                                bodybuilder.add("date",date);
                                bodybuilder.add("ripple",ripple);
                                FormBody body = bodybuilder.build();
                                builder = builder.post(body);

                                Request request = builder.build();
                                Call call = client.newCall(request);
                                RippleCallback callback = new RippleCallback();
                                call.enqueue(callback);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                    }
                });
        }
    }

    class RippleCallback implements Callback{

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            Ripple_BoardThread thread = new Ripple_BoardThread();
            thread.start();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RippleAdapter adapter = (RippleAdapter) ripple_rv.getAdapter();
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }

    class RippleAdapter extends RecyclerView.Adapter<RippleAdapter.ViewHolder>{


        public RippleAdapter(){
        }

        @NonNull
        @Override
        public RippleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ripple_list,parent,false);
            RippleAdapter.ViewHolder holder = new RippleAdapter.ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RippleAdapter.ViewHolder holder, int position) {
            HashMap<String, Object> map = data_list.get(position);
            String name = (String) map.get("ripple_name");
            String comment = (String) map.get("ripple_comment");
            String date = (String) map.get("ripple_date");
            String profile = (String) map.get("ripple_profile");
            String id = (String) map.get("ripple_id");
            holder.ripple_name.setText(name);
            holder.ripple_comment.setText(comment);
            holder.ripple_date.setText(date);

            Glide.with(holder.itemView).load(profile).apply(new RequestOptions().circleCrop()).into(holder.ripple_profile);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RippleActivity.this);
                    builder.setTitle("댓글을 삭제하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                HashMap<String, Object> map = data_list.get(position);
                                String comment = (String) map.get("ripple_comment");
                                String id = (String) map.get("ripple_id");
                                if(uid.equals(id)){
                                    String site = "http://192.168.0.102:8090/HowDoI/RippleDelete.jsp";
                                    OkHttpClient client = new OkHttpClient();
                                    Request.Builder builder2 = new Request.Builder();
                                    builder2 = builder2.url(site);

                                    FormBody.Builder bodybuilder = new FormBody.Builder();
                                    bodybuilder.add("comment", comment);
                                    bodybuilder.add("id", id);
                                    FormBody body = bodybuilder.build();
                                    builder2 = builder2.post(body);

                                    Request request = builder2.build();
                                    Call call = client.newCall(request);
                                    RippleDeleteCallback callback = new RippleDeleteCallback();
                                    call.enqueue(callback);
                                }else {
                                    Toast.makeText(RippleActivity.this, "권한이 없습니다", Toast.LENGTH_SHORT).show();
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("취소",null);
                    builder.show();
                    return true;
                }
            });
        }

        class RippleDeleteCallback implements Callback{

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Ripple_BoardThread thread = new Ripple_BoardThread();
                thread.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RippleAdapter adapter = (RippleAdapter) ripple_rv.getAdapter();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(RippleActivity.this, "댓글을 삭제하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return data_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView ripple_profile;
            TextView ripple_name, ripple_comment, ripple_date;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.ripple_name = itemView.findViewById(R.id.ripple_name);
                this.ripple_comment = itemView.findViewById(R.id.ripple_comment);
                this.ripple_date = itemView.findViewById(R.id.ripple_date);
                this.ripple_profile = itemView.findViewById(R.id.ripple_profile);
            }
        }
    }
}