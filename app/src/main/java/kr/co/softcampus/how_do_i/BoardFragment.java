//package kr.co.softcampus.how_do_i;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.cardview.widget.CardView;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//import com.bumptech.glide.Glide;
//import com.facebook.all.All;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.MutableData;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
//import com.google.firebase.firestore.Query;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.firestore.Transaction;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import org.jetbrains.annotations.NotNull;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//
//
//public class BoardFragment extends Fragment {
//
//    ArrayList<OneBoard> arrayList;
//    LinearLayoutManager manager;
//    CustomAdapter adapter;
//    RecyclerView recyclerView;
//
//    FirebaseFirestore db;
//    FirebaseStorage firebaseStorage;
//    StorageReference storageReference;
//    OneBoard oneBoard;
//    CollectionReference collectionReference;
//
//    SwipeRefreshLayout swipeRefreshLayout;
//    BoardFragment boardFragment;
//
//    static final String TAG = "BoardFragment";
//
//
//    public BoardFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_board, container, false);
//
//        db = FirebaseFirestore.getInstance();
//        arrayList = new ArrayList<OneBoard>();
//        collectionReference = db.collection("oneBoard");
//        collectionReference.orderBy("createAt", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d(TAG, document.getId() + " => " + document.getData());
//                        recyclerView = view.findViewById(R.id.rv);
//                        recyclerView.setHasFixedSize(true);
//
//                        String board_title = (String)document.getData().get("title");
//                        String board_content = (String)document.getData().get("content");
//                        String board_image = (String)document.getData().get("imageUrl");
//                        String board_publisher = (String)document.getData().get("publisher");
//                        Date board_date = new Date(document.getDate("createAt").getTime());
//                        String board_id = (String)document.getData().get("board_id");
//                        String file_name = (String)document.getData().get("file_name");
//
//
//                        oneBoard = new OneBoard(board_title,board_content,board_image,board_publisher,board_date, board_id, file_name);
//                        arrayList.add(oneBoard);
//                        manager = new LinearLayoutManager(getContext());
//                        recyclerView.setLayoutManager(manager);
//                        adapter = new CustomAdapter(arrayList, getContext());
//                        recyclerView.setAdapter(adapter);
//                    }
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
//
//        swipeRefreshLayout = view.findViewById(R.id.newPage);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                boardFragment = new BoardFragment();
//                FragmentTransaction tran = getFragmentManager().beginTransaction();
//                tran.replace(R.id.container,boardFragment);
//                tran.commit();
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//        return view;
//    }
//
//
//    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements Filterable{
//        ArrayList<OneBoard> arrayList;
//        ArrayList<OneBoard> AllarrayList;
//
//
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        FirebaseUser user = firebaseAuth.getCurrentUser();
//
//
//        public CustomAdapter(){
//
//        }
//
//        public CustomAdapter(ArrayList<OneBoard> arrayList) {
//            this.arrayList = arrayList;
//            AllarrayList = new ArrayList<>(arrayList);
//        }
//
//        @NonNull
//        @NotNull
//        @Override
//        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
//            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
//            CustomAdapter.ViewHolder holder = new CustomAdapter.ViewHolder(cardView);
//            return holder;
//        }
//
//
//        @Override
//        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
//            Glide.with(holder.itemView).load(arrayList.get(position).getImageUrl()).thumbnail(0.1f).override(1000).into(holder.tv_image);
//            holder.tv_title.setText(arrayList.get(position).getTitle());
//            holder.tv_content.setText(arrayList.get(position).getContent());
//
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//            String createAt = simpleDateFormat.format(arrayList.get(position).getCreateAt());
//            holder.board_date.setText(createAt);
//
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getContext(), SelectBoard.class);
//
//                    intent.putExtra("title",arrayList.get(position).getTitle());
//                    intent.putExtra("content",arrayList.get(position).getContent());
//                    intent.putExtra("image",arrayList.get(position).getImageUrl());
//                    intent.putExtra("date",String.valueOf(arrayList.get(position).getCreateAt()));
//                    intent.putExtra("publisher",arrayList.get(position).getPublisher());
//                    intent.putExtra("id",arrayList.get(position).getBoard_id());
//
//                    startActivity(intent);
//                }
//            });
//
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setTitle("게시글 삭제");
//                    builder.setMessage("게시글을 삭제하시겠습니까?");
//
//                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            remove(holder.getAdapterPosition());
//                        }
//                    });
//                    builder.setNegativeButton("취소", null);
//                    builder.show();
//                    return true;
//                }
//            });
//
//
//            holder.popup.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PopupMenu popupMenu = new PopupMenu(getContext(),holder.popup);
//                    Menu menu = popupMenu.getMenu();
//
//                    MenuInflater inflater = popupMenu.getMenuInflater();
//                    inflater.inflate(R.menu.popup_menu,menu);
//
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            int id = item.getItemId();
//                            switch(id){
//                                case R.id.update:
//                                    Intent intent = new Intent(getContext(),UpdateBoard.class);
//
//                                    intent.putExtra("title",arrayList.get(position).getTitle());
//                                    intent.putExtra("content",arrayList.get(position).getContent());
//                                    intent.putExtra("image",arrayList.get(position).getImageUrl());
//                                    intent.putExtra("publisher",arrayList.get(position).getPublisher());
//                                    intent.putExtra("id",arrayList.get(position).getBoard_id());
//
//                                    startActivity(intent);
//                                    break;
//                                case R.id.delete:
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                    builder.setTitle("게시글 삭제");
//                                    builder.setMessage("게시글을 삭제하시겠습니까?");
//
//                                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            remove(holder.getAdapterPosition());
//                                        }
//                                    });
//                                    builder.setNegativeButton("취소", null);
//                                    builder.show();
//                                    break;
//                            }
//                            return true;
//                        }
//                    });
//                    popupMenu.show();
//                }
//            });
//        }
//
//
//        @Override
//        public int getItemCount() {
//            return (arrayList != null ? arrayList.size() : 0);
//        }
//
//        @Override
//        public Filter getFilter() {
//            return exampleFilter;
//        }
//
//        public Filter exampleFilter = new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                ArrayList<OneBoard> filteredList = new ArrayList<>();
//
//                if(constraint==null || constraint.length()==0){
//                    filteredList.addAll(AllarrayList);
//                }else {
//                    String filterPattern = constraint.toString().toLowerCase().trim();
//                    for(OneBoard item : AllarrayList){
//                        if(item.getTitle().toLowerCase().contains(filterPattern)){
//                            filteredList.add(item);
//                        }
//                    }
//                }
//                FilterResults results = new FilterResults();
//                results.values = filteredList;
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                arrayList.clear();
//                arrayList.addAll((ArrayList<OneBoard>) results.values);
//                notifyDataSetChanged();
//            }
//        };
//
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//
//            CardView cardView;
//            TextView tv_title, tv_content, board_date;
//            ImageView tv_image, popup;
//
//            public ViewHolder(@NonNull @NotNull CardView v) {
//                super(v);
//                cardView = v;
//                this.tv_title = cardView.findViewById(R.id.tv_title);
//                this.tv_content = cardView.findViewById(R.id.tv_content);
//                this.tv_image = cardView.findViewById(R.id.tv_image);
//                this.board_date = cardView.findViewById(R.id.board_date);
//                this.popup = cardView.findViewById(R.id.popup);
//            }
//        }
//
//
//        public void remove(int position) {
//            firebaseAuth = FirebaseAuth.getInstance();
//            user = firebaseAuth.getCurrentUser();
//
//            if(user.getUid().equals(arrayList.get(position).getPublisher())){
//                firebaseStorage = FirebaseStorage.getInstance();
//                storageReference = firebaseStorage.getReference();
//                storageReference.child("images/"+arrayList.get(position).getBoard_id()+"/"+arrayList.get(position).getFile_name())
//                        .delete()
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                db.collection("oneBoard").document(arrayList.get(position).getBoard_id())
//                                        .delete()
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//                                                Toast.makeText(context, "게시글을 삭제하셨습니다.", Toast.LENGTH_SHORT).show();
//                                                arrayList.remove(position);
//                                                notifyItemRemoved(position);
//                                            }
//                                        });
//                            }
//                        });
//            }else {
//                Toast.makeText(context, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.action_bar,menu);
//        MenuItem search_item = menu.findItem(R.id.search);
//        SearchView search_view = (SearchView)search_item.getActionView();
//        search_view.setQueryHint("보고싶은 게시글을 찾아보세요");
//        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                search_view.clearFocus();
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                adapter.getFilter().filter(newText);
//                return true;
//            }
//        });
//    }
//}