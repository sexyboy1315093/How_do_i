package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SelectBoard extends AppCompatActivity {

    TextView comment_name, comment_title, comment_content, comment_date, comment_re;
    ImageView comment_profile, select_image, popup;
    ConstraintLayout constraintLayout;

    String title, content, image, board_id, publisher, filename, date ;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_board);
        db = FirebaseFirestore.getInstance();

        comment_name = findViewById(R.id.comment_name);
        comment_profile = findViewById(R.id.comment_profile);
        comment_title = findViewById(R.id.comment_title);
        comment_content = findViewById(R.id.comment_content);
        comment_date = findViewById(R.id.comment_date);
        comment_re = findViewById(R.id.comment_re);
        popup = findViewById(R.id.popup);

        select_image = findViewById(R.id.select_image);
        constraintLayout = findViewById(R.id.includeframe);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        image = intent.getStringExtra("image");
        board_id = intent.getStringExtra("id");
        filename = intent.getStringExtra("filename");

        date = intent.getStringExtra("date");
        Date date1 = new Date(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String new_date = simpleDateFormat.format(date1);

        publisher = intent.getStringExtra("publisher");
        db.collection("loginUser").whereEqualTo("id",publisher)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String fire_name = (String) document.getData().get("name");
                                String fire_profile = (String) document.get("profile");
                                comment_name.setText(fire_name+"님");
                                Glide.with(SelectBoard.this).load(fire_profile).into(comment_profile);
                            }
                        }
                    }
                });

        comment_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectBoard.this,ChatActivity.class);
                intent.putExtra("destUid",publisher);
                startActivity(intent);
            }
        });

        Glide.with(this).load(image).thumbnail().into(select_image);
        comment_title.setText(title);
        comment_content.setText(content);
        comment_date.setText(new_date);
        comment_re.setText("댓글");
        comment_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rip_intent = new Intent(SelectBoard.this, RippleActivity.class);
                rip_intent.putExtra("board_id",board_id);
                startActivity(rip_intent);
            }
        });

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.VISIBLE);
            }
        });

        popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(SelectBoard.this, popup);
                    Menu menu = popupMenu.getMenu();

                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.popup_menu,menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            switch(id){
                                case R.id.update:
                                    Intent intent = new Intent(SelectBoard.this, UpdateBoard.class);

                                    intent.putExtra("title",title);
                                    intent.putExtra("content",content);
                                    intent.putExtra("image",image);
                                    intent.putExtra("publisher",publisher);
                                    intent.putExtra("board_id",board_id);

                                    startActivity(intent);
                                    break;
                                case R.id.delete:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SelectBoard.this);
                                    builder.setTitle("게시글 삭제");
                                    builder.setMessage("게시글을 삭제하시겠습니까?");

                                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove();
                                        }
                                    });
                                    builder.setNegativeButton("취소", null);
                                    builder.show();
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });


    }

    public void remove() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(user.getUid().equals(publisher)){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference();
            storageReference.child("images/"+board_id+"/"+filename)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection("oneBoard").document(board_id)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SelectBoard.this, "게시글을 삭제하셨습니다.", Toast.LENGTH_SHORT).show();
                                            finish();
                                            Intent intent1 = new Intent(SelectBoard.this, HomeActivity.class);
                                            startActivity(intent1);
                                        }
                                    });
                        }
                    });
        }else {
            Toast.makeText(SelectBoard.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
       constraintLayout.setVisibility(View.GONE);
       finish();
    }
}