package kr.co.softcampus.how_do_i;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateBoard extends AppCompatActivity {

    ImageView update_image;
    EditText update_title, update_content;
    Button back_btn, update_btn;

    FirebaseFirestore db;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    String publisher;
    String id;

    String new_title;
    String new_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_board);

        update_image = findViewById(R.id.update_image);
        update_title = findViewById(R.id.update_title);
        update_content = findViewById(R.id.update_content);
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String image = intent.getStringExtra("image");
        publisher = intent.getStringExtra("publisher");
        id = intent.getStringExtra("board_id");

        Glide.with(this).load(image).thumbnail().into(update_image);
        update_title.setHint(title);
        update_content.setHint(content);

        update_btn = findViewById(R.id.update_btn);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                user = firebaseAuth.getCurrentUser();
                db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("oneBoard").document(id);
                if(publisher.equals(user.getUid())){
                    new_title = update_title.getText().toString();
                    new_content = update_content.getText().toString();
                    Map<String,Object> map = new HashMap<>();
                    map.put("title",new_title);
                    map.put("content",new_content);
                    map.put("createAt",new Date());
                    documentReference.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UpdateBoard.this, "업데이트 성공", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent intent1 = new Intent(UpdateBoard.this, HomeActivity.class);
                            startActivity(intent1);
                        }
                    });
                }else {
                    Toast.makeText(UpdateBoard.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}