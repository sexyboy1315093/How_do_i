package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Email_join extends AppCompatActivity {


    EditText edit_email, edit_pass, edit_pass2, edit_name;
    Button btn_join;
    TextView btn_check;

    String email,pass,pass2,name;

    Pattern emailPattern = Patterns.EMAIL_ADDRESS;

    String site;
    boolean validate = false;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_tab_fragment);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edit_email = findViewById(R.id.edit_email);
        edit_pass = findViewById(R.id.edit_pass);
        edit_pass2 = findViewById(R.id.edit_pass2);
        edit_name = findViewById(R.id.edit_name);

        //아이디 중복확인
        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = edit_email.getText().toString();

                if(checkEmail1(email)){
                    if(checkEmail2(email)){
                            emailCheckThread thread = new emailCheckThread();
                            thread.start();
                    }else {
                        Toast.makeText(Email_join.this, "이메일 형식에 맞게 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Email_join.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //회원가입
        btn_join = findViewById(R.id.btn_join);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = edit_email.getText().toString();
                pass = edit_pass.getText().toString();
                pass2 = edit_pass2.getText().toString();
                name = edit_name.getText().toString();

                if(checkEmail1(email)){
                    if(checkEmail2(email)){
                        if(validate){
                            if(pass.length()!=0 && pass2.length()!=0){
                                if(pass.equals(pass2)){
                                    if(name.length()!=0){
                                        JoinThread thread = new JoinThread();
                                        thread.start();

                                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                               .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                   @Override
                                                   public void onSuccess(AuthResult authResult) {
                                                       firebaseUser = firebaseAuth.getCurrentUser();
                                                       LoginUser loginUser = new LoginUser(name,null, email, firebaseUser.getUid());
                                                       db.collection("loginUser").document(firebaseUser.getUid()).set(loginUser)
                                                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                   @Override
                                                                   public void onSuccess(Void unused) {
                                                                       Toast.makeText(Email_join.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                                                       Intent intent = new Intent(Email_join.this,MainActivity.class);
                                                                       startActivity(intent);
                                                                       finish();
                                                                   }
                                                               });
                                                   }
                                               });

                                    }else {
                                        Toast.makeText(Email_join.this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(Email_join.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(Email_join.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(Email_join.this, "중복확인 해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(Email_join.this, "이메일 형식에 맞게 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(Email_join.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //이메일 공란 체크
    public boolean checkEmail1(String email){
        if(email.length()!=0){
            return true;
        }else {
            return false;
        }
    }

    //이메일 형식 체크
    public boolean checkEmail2(String email){
        if(emailPattern.matcher(email).matches()){
            return true;
        }else {
            return false;
        }
    }

    class JoinThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                site = "http://192.168.0.102:8090/HowDoI/member_join.jsp";
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder = builder.url(site);

                email = edit_email.getText().toString();
                pass = edit_pass.getText().toString();
                pass2 = edit_pass2.getText().toString();
                name = edit_name.getText().toString();

                FormBody.Builder bodyBuilder = new FormBody.Builder();
                bodyBuilder.add("email", email);
                bodyBuilder.add("pass", pass);
                bodyBuilder.add("pass2", pass2);
                bodyBuilder.add("name", name);
                FormBody body = bodyBuilder.build();
                builder = builder.post(body);

                Request request = builder.build();
                Call call = client.newCall(request);
                NetworkCallback callback = new NetworkCallback();
                call.enqueue(callback);

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class NetworkCallback implements Callback{

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class emailCheckThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                site = "http://192.168.0.102:8090/HowDoI/member_check.jsp";
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder = builder.url(site);

                email = edit_email.getText().toString();

                FormBody.Builder bodyBuilder = new FormBody.Builder();
                bodyBuilder.add("email", email);
                FormBody body = bodyBuilder.build();
                builder = builder.post(body);

                Request request = builder.build();
                Call call = client.newCall(request);
                emailCheckCallback callback = new emailCheckCallback();
                call.enqueue(callback);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class emailCheckCallback implements Callback{

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{
                String result = response.body().string();
                JSONObject obj = new JSONObject(result);
                final String db_email = obj.getString("db_email");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(db_email.equals(email)){
                            Toast.makeText(Email_join.this, "이메일이 중복됩니다. 다른 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            validate = false;
                        }else{
                            Toast.makeText(Email_join.this, "사용가능합니다.", Toast.LENGTH_SHORT).show();
                            validate = true;
                        }
                    }
                });

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
