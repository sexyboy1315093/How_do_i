package kr.co.softcampus.how_do_i;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //구글
    SignInButton google_login;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    FirebaseAuth firebaseAuth = null;
    FirebaseFirestore db;

    Button facebook_login;
    CallbackManager mCallbackManager;
    private static final String TAG = "FacebookLogin";

    TextView email_join;

    long backBtnTime = 0;
    float v = 0 ;

//    ---------------------------------

    EditText login_email, login_pass;
    Button login_btn;

    String email, pass;
    Pattern Emailpattern = Patterns.EMAIL_ADDRESS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        mCallbackManager = CallbackManager.Factory.create();

        TedPermission.with(this).setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "권한허용", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "권한거절", Toast.LENGTH_SHORT).show();
            }
        }).setRationaleMessage("권한을 허용하셔야 이용 가능합니다.")
                .setDeniedMessage("거부하셨습니다. [설정]->[권한]을 변경하시면 이용 가능합니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google_login = findViewById(R.id.google_login);
        TextView google_text =(TextView) google_login.getChildAt(0);
        google_text.setText("Google Login");
        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signin();
            }
        });

        facebook_login = findViewById(R.id.facebook_login);
        facebook_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin();
            }
        });

        email_join = findViewById(R.id.email_join);
        email_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Email_join.class);
                startActivity(intent);
            }
        });


        login_email = findViewById(R.id.login_email);
        login_pass = findViewById(R.id.login_pass);
        login_btn = findViewById(R.id.email_login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = login_email.getText().toString();
                pass = login_pass.getText().toString();

                if(checkEmail(email)){
                    if(checkEmail2(email)){
                        if(pass.length()!=0){
                            idpacheckThread thread = new idpacheckThread();
                            thread.start();
                        }else {
                            Toast.makeText(MainActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "이메일 형식에 맞게 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        facebook_login.setTranslationY(300);
        google_login.setTranslationY(300);
        login_btn.setTranslationY(300);
        email_join.setTranslationY(300);

        facebook_login.setAlpha(v);
        google_login.setAlpha(v);
        login_btn.setAlpha(v);
        email_join.setAlpha(v);

        facebook_login.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(600).start();
        google_login.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        login_btn.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();
        email_join.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(0).start();

    }

    public boolean checkEmail(String email){
        if(email.length()!=0){
            return true;
        }else {
            return false;
        }
    }

    public boolean checkEmail2(String email){
        if(Emailpattern.matcher(email).matches()){
            return true;
        }else {
            return false;
        }
    }

    class idpacheckThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                email = login_email.getText().toString();
                pass = login_pass.getText().toString();

                String site = "http://192.168.0.102:8090/HowDoI/member_check2.jsp";
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder = builder.url(site);

                FormBody.Builder bodyBuilder = new FormBody.Builder();
                bodyBuilder.add("email",email);
                bodyBuilder.add("pass",pass);
                FormBody body = bodyBuilder.build();
                builder = builder.post(body);

                Request request = builder.build();
                Call call = client.newCall(request);
                idpacheckCallback callback = new idpacheckCallback();
                call.enqueue(callback);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class idpacheckCallback implements Callback {

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{
                String result = response.body().string();
                JSONObject obj = new JSONObject(result);
                final String db_email = obj.getString("db_email");
                final String db_pass = obj.getString("db_pass");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(db_email.equals(email) && db_pass.equals(pass) ){
                                firebaseAuth.signInWithEmailAndPassword(email, pass)
                                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                            }else {
                                Toast.makeText(MainActivity.this, "이메일 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void signin(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "구글 로그인 성공", Toast.LENGTH_SHORT).show();
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            updateUI(firebaseUser);
                        } else {
                            Toast.makeText(MainActivity.this, "구글 로그인 실패", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void facebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() { }

            @Override
            public void onError(FacebookException error) { }
        });
    }

    public void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            updateUI(firebaseUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if(firebaseUser!=null){
            for (UserInfo profile : firebaseUser.getProviderData()) {
                String uid = firebaseUser.getUid();
                String name = profile.getDisplayName();
                String email = profile.getEmail();
                Uri photoUrl = profile.getPhotoUrl();
                String image = String.valueOf(photoUrl);

                LoginUser loginUser = new LoginUser(name, image, email, firebaseUser.getUid());
                db.collection("loginUser").document(firebaseUser.getUid()).set(loginUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        }
    }


    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        long gapTime = now - backBtnTime;
        if(gapTime>0 && gapTime<=2000){
            super.onBackPressed();
        }else {
            backBtnTime = now;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }
}