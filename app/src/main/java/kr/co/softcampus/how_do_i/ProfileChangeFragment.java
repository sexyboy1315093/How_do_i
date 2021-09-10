package kr.co.softcampus.how_do_i;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileChangeFragment extends DialogFragment{


    ImageView profile_camera;
    EditText edit_nickname;
    String dir_path;
    String pic_path;
    Uri contentUri;
    Button gallery;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;

    ProfileChangeFragment profileChangeFragment;


    public ProfileChangeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        HomeActivity activity = (HomeActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        profileChangeFragment = this;

        LayoutInflater inflater = getLayoutInflater();
        View v1 = inflater.inflate(R.layout.fragment_profile_change,null);

        profile_camera = v1.findViewById(R.id.profile_camera);
        profile_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String file_name = "/temp_"+System.currentTimeMillis()+".jpg";

                pic_path = dir_path+file_name;

                File file = new File(pic_path);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    contentUri = FileProvider.getUriForFile(getContext(),"kr.co.softcampus.how_do_i.file_provider",file);
                }else {
                    contentUri = Uri.fromFile(file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,contentUri);
                startActivityForResult(intent,2000);
            }
        });
        gallery = v1.findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .start(getContext(),profileChangeFragment);
            }
        });

        edit_nickname = v1.findViewById(R.id.edit_title);
        builder.setView(v1);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(edit_nickname.getText().toString().length()!=0 && contentUri!=null ){
                    documentReference = firebaseFirestore.collection("loginUser").document(firebaseUser.getUid());
                    Map<String,Object> map = new HashMap<>();
                    map.put("name",edit_nickname.getText().toString());
                    map.put("profile",contentUri.getPath());
                    documentReference.update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(activity, "프로필을 등록했습니다", Toast.LENGTH_SHORT).show();
                                }
                            });
                }else {
                    Toast.makeText(activity, "프로필 혹은 닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("취소",null);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    public void init(){
        File f1 = Environment.getExternalStorageDirectory();
        String a1 = f1.getAbsolutePath();
        String a2 = getActivity().getPackageName();
        dir_path = a1+"/android/data/"+a2;

        File file = new File(dir_path);
        if(file.exists()==false){
            file.mkdir();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_change, container, false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==Activity.RESULT_OK){
                contentUri = result.getUri();
                Log.e("asdkljasld",contentUri.getPath());
                Glide.with(getContext()).load(contentUri.getPath()).into(profile_camera);
            }else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                Exception error = result.getError();
            }
        }else if(requestCode==2000){
            if(resultCode==Activity.RESULT_OK){
                Glide.with(getContext()).load(contentUri.getPath()).into(profile_camera);
            }
        }
    }
}