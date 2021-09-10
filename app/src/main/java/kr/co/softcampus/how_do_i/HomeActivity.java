package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    SearchView search_view;
    BottomNavigationView bottomNavigationView;

    CustomAdapter adapter;
    ArrayList<OneBoard> arrayList;
    OneBoard oneBoard;

    long backBtnTime = 0;
    private long lastTimeBackPressed;

    RecyclerView recyclerView;

    FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    CollectionReference collectionReference;
    DocumentReference documentReference;

    SwipeRefreshLayout swipeRefreshLayout;

    static final String TAG = "BoardFragment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        arrayList = new ArrayList<OneBoard>();
        collectionReference = db.collection("oneBoard");
        collectionReference.orderBy("createAt", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());

                        recyclerView = findViewById(R.id.rv);
                        recyclerView.setHasFixedSize(true);

                        String board_title = (String)document.getData().get("title");
                        String board_content = (String)document.getData().get("content");
                        String board_image = (String)document.getData().get("imageUrl");
                        String board_publisher = (String)document.getData().get("publisher");
                        Date board_date = new Date(document.getDate("createAt").getTime());
                        String board_id = (String)document.getData().get("board_id");
                        String file_name = (String)document.getData().get("file_name");

                        Log.e("182900sjf",board_title+","+board_id);

                        oneBoard = new OneBoard(board_title,board_content,board_image,board_publisher,board_date, board_id, file_name);
                        arrayList.add(oneBoard);
                        recyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this,3));
                        adapter = new CustomAdapter(arrayList);
                        recyclerView.setAdapter(adapter);

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        swipeRefreshLayout = findViewById(R.id.newPage);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent home_intent = new Intent(HomeActivity.this,HomeActivity.class);
                startActivity(home_intent);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    case R.id.logout:
                        Logout();
                        break;
                    case R.id.profile:
                        setFrag(2);
                        break;
                    case R.id.friend:
                        setFrag(3);
                        break;
                    case R.id.talk:
                        setFrag(4);
                        break;
                    case R.id.home2:
                        Intent home_intent = new Intent(HomeActivity.this,HomeActivity.class);
                        startActivity(home_intent);
                }
                return true;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("How do I look");
        actionBar.setIcon(R.drawable.a3);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        passPushTokenToServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar,menu);

        MenuItem search_item = menu.findItem(R.id.search);
        search_view = (SearchView)search_item.getActionView();
        search_view.setQueryHint("보고싶은 게시글을 찾아보세요");
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.home:
                Intent home_intent = new Intent(HomeActivity.this,HomeActivity.class);
                startActivity(home_intent);
                break;
            case R.id.upload:
                setFrag(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setFrag(int n){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction tran = manager.beginTransaction();
        switch(n){
            case 0:
                tran.replace(R.id.container,new UploadFragment());
                tran.addToBackStack(null);
                recyclerView.setVisibility(View.GONE);
                break;
            case 2:
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.show(manager,"frag_profile");
                break;
            case 3:
                tran.replace(R.id.container,new FriendFragment());
                tran.addToBackStack(null);
                recyclerView.setVisibility(View.GONE);
                break;
            case 4:
                tran.replace(R.id.container,new TalkFragment());
                tran.addToBackStack(null);
                recyclerView.setVisibility(View.GONE);
                break;
        }
        tran.commit();
    }

    public void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                Map<String, Object> map = new HashMap<>();
                map.put("pushToken",token);
                documentReference = db.collection("loginUser").document(uid);
                documentReference.update(map);
            }
        });
    }

  class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements Filterable{
        ArrayList<OneBoard> arrayList;
        ArrayList<OneBoard> AllarrayList;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        public CustomAdapter(){ }

        public CustomAdapter(ArrayList<OneBoard> arrayList) {
            this.arrayList = arrayList;
            AllarrayList = new ArrayList<>(arrayList);
        }


        @NonNull
        @NotNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list2, parent, false);
            CustomAdapter.ViewHolder holder = new CustomAdapter.ViewHolder(cardView);
            return holder;
        }


      @Override
      public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView).load(arrayList.get(position).getImageUrl()).thumbnail(0.1f).override(1000).into(holder.tv_image);
            holder.tv_title.setText(arrayList.get(position).getTitle());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, SelectBoard.class);

                    intent.putExtra("title",arrayList.get(position).getTitle());
                    intent.putExtra("content",arrayList.get(position).getContent());
                    intent.putExtra("image",arrayList.get(position).getImageUrl());
                    intent.putExtra("date",String.valueOf(arrayList.get(position).getCreateAt()));
                    intent.putExtra("publisher",arrayList.get(position).getPublisher());
                    intent.putExtra("id",arrayList.get(position).getBoard_id());
                    intent.putExtra("filename", arrayList.get(position).getFile_name());

                    startActivity(intent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this,R.style.AlertDialogTheme);
                    View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_warning_dialog,
                            (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
                    builder.setView(view);
                    ((TextView) view.findViewById(R.id.textTitle)).setText("선택하세요");
                    ((TextView) view.findViewById(R.id.textMessage)).setText("게시글을 삭제하시겠습니까?");
                    ((TextView) view.findViewById(R.id.buttonYes)).setText("삭제");
                    ((TextView) view.findViewById(R.id.buttonNo)).setText("취소");
                    AlertDialog dialog = builder.create();
                    view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            remove(holder.getAdapterPosition());
                            dialog.dismiss();
                        }
                    });

                    view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                    return true;
                }
            });
        }

      @Override
      public Filter getFilter() {
          return exampleFilter;
      }

      public Filter exampleFilter = new Filter() {
          @Override
          protected FilterResults performFiltering(CharSequence constraint) {
              ArrayList<OneBoard> filteredList = new ArrayList<>();

              if(constraint==null || constraint.length()==0){
                  filteredList.addAll(AllarrayList);
              }else {
                  String filterPattern = constraint.toString().toLowerCase().trim();
                  for(OneBoard item : AllarrayList){
                      if(item.getTitle().toLowerCase().contains(filterPattern)){
                          filteredList.add(item);
                      }
                  }
              }
              FilterResults results = new FilterResults();
              results.values = filteredList;
              return results;
          }

          @Override
          protected void publishResults(CharSequence constraint, FilterResults results) {
              arrayList.clear();
              arrayList.addAll((ArrayList<OneBoard>) results.values);
              notifyDataSetChanged();
          }
      };


        @Override
        public int getItemCount() {
            return (arrayList != null ? arrayList.size() : 0);
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            CardView cardView;
            TextView tv_title;
            ImageView tv_image;

            public ViewHolder(@NonNull @NotNull CardView v) {
                super(v);
                cardView = v;
                this.tv_title = cardView.findViewById(R.id.tv_title);
                this.tv_image = cardView.findViewById(R.id.tv_image);
            }
        }


        public void remove(int position) {
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();

            if(user.getUid().equals(arrayList.get(position).getPublisher())){
                firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReference();
                storageReference.child("images/"+arrayList.get(position).getBoard_id()+"/"+arrayList.get(position).getFile_name())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                db.collection("oneBoard").document(arrayList.get(position).getBoard_id())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(HomeActivity.this, "게시글을 삭제하셨습니다.", Toast.LENGTH_SHORT).show();
                                                arrayList.remove(position);
                                                notifyItemRemoved(position);
                                                recyclerView.getAdapter().notifyDataSetChanged();
                                            }
                                        });
                            }
                        });
            }else {
                Toast.makeText(HomeActivity.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Logout() {
        long gapTime = System.currentTimeMillis() - backBtnTime;
        if(gapTime>=0 && gapTime<=2000){
            Intent main_intent = new Intent(HomeActivity.this,MainActivity.class);
            startActivity(main_intent);
            finish();
            FirebaseAuth.getInstance().signOut();
        }else {
            backBtnTime = System.currentTimeMillis();
            Toast.makeText(this,"한번 더 누르시면 로그인화면으로 이동합니다",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for(Fragment fragment : fragmentList){
            if(fragment instanceof onBackPressedListener){
                ((onBackPressedListener)fragment).onBackPressed();
                return;
            }
        }

        if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
            finish();
            return;
        }
        lastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(this,"'뒤로' 버튼을 한 번 더 누르면 종료됩니다",Toast.LENGTH_SHORT).show();
    }

}