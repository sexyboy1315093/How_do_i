package kr.co.softcampus.how_do_i;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendFragment extends Fragment implements onBackPressedListener{

    ArrayList<LoginUser> arrayList;
    LinearLayoutManager manager;
    FriendAdapter adapter;
    RecyclerView friend_rv;

    FirebaseFirestore firebaseFirestore;
    LoginUser loginUser;
    CollectionReference collectionReference;

    static final String TAG = "FriendFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        arrayList = new ArrayList<LoginUser>();
        final String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference = firebaseFirestore.collection("loginUser");
        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                           for(QueryDocumentSnapshot document : task.getResult()){
                               Log.d(TAG, document.getId() + " => " + document.getData());
                               friend_rv = view.findViewById(R.id.friend_rv);
                               friend_rv.setHasFixedSize(true);

                               String name =(String) document.getData().get("name");
                               String profile =(String)document.getData().get("profile");
                               String email =(String)document.getData().get("email");
                               String id =(String)document.getData().get("id");

                               if(id.equals(myid)){
                                    continue;
                               }

                               loginUser = new LoginUser(name,profile,email,id);
                               arrayList.add(loginUser);
                               manager = new LinearLayoutManager(getContext());
                               friend_rv.setLayoutManager(manager);
                               adapter = new FriendAdapter(arrayList, getContext());
                               friend_rv.setAdapter(adapter);
                           }
                        }
                    }
                });

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.hide();

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


    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

        ArrayList<LoginUser> arrayList;
        Context context;

        public FriendAdapter(ArrayList<LoginUser> arrayList, Context context){
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list,parent,false);
            FriendAdapter.ViewHolder holder = new FriendAdapter.ViewHolder(view1);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {

            Glide.with(holder.itemView).load(arrayList.get(position).getProfile()).apply(new RequestOptions().circleCrop()).into(holder.friend_image);
            holder.friend_name.setText(arrayList.get(position).getName());
            holder.friend_email.setText(arrayList.get(position).getEmail());
            holder.talk_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),ChatActivity.class);
                    intent.putExtra("destUid",arrayList.get(position).getId());
                    intent.putExtra("profile",arrayList.get(position).getProfile());
                    intent.putExtra("name",arrayList.get(position).getName());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return (arrayList != null ? arrayList.size() : 0);
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView friend_image;
            TextView friend_name, friend_email;
            Button talk_btn;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.friend_image = itemView.findViewById(R.id.friend_image);
                this.friend_name = itemView.findViewById(R.id.friend_name);
                this.friend_email = itemView.findViewById(R.id.friend_email);
                this.talk_btn = itemView.findViewById(R.id.talk_btn);
            }
        }
    }
}