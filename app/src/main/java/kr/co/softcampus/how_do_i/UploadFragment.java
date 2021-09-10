package kr.co.softcampus.how_do_i;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;


public class UploadFragment extends Fragment implements onBackPressedListener {

    ImageView imageview;
    Button btn1, btn3;

    Uri contentUri;

    EditText edit_title, edit_content;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ProgressBar progressBar;

    UploadFragment uploadFragment ;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        uploadFragment = this;

        progressBar = view.findViewById(R.id.progressBar2);
        imageview = view.findViewById(R.id.imageView);

        btn1 = view.findViewById(R.id.button);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .start(getContext(),uploadFragment);
            }
        });

        edit_title = view.findViewById(R.id.edit_title);
        edit_content = view.findViewById(R.id.edit_content);
        btn3 = view.findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_title.length()!=0){
                    if(edit_content.length()!=0){
                        if(contentUri!=null){
                            firebaseStorage = FirebaseStorage.getInstance();
                            storageReference = firebaseStorage.getReference();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            final DocumentReference documentReference = db.collection("oneBoard").document();

                            Uri file = Uri.fromFile(new File(contentUri.getPath()));
                            StorageReference riversRef = storageReference.child("images/"+documentReference.getId()+"/"+file.getLastPathSegment());
                            UploadTask uploadTask = riversRef.putFile(file);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String title = edit_title.getText().toString();
                                            String content = edit_content.getText().toString();
                                            String file_name = file.getLastPathSegment();
                                            OneBoard oneBoard = new OneBoard(title, content, uri.toString(), firebaseUser.getUid(), new Date(), documentReference.getId(), file_name);

                                            documentReference.set(oneBoard).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    Toast.makeText(getContext(), "업로드 완료.", Toast.LENGTH_SHORT).show();

                                                    progressBar.setVisibility(View.GONE);
                                                    edit_title.setText("");
                                                    edit_content.setText("");

                                                    Intent home_intent = new Intent(getContext(),HomeActivity.class);
                                                    startActivity(home_intent);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                    int pro = Integer.parseInt(String.valueOf(Math.round(progress)));
                                    progressBar.setProgress(pro);
                                }
                            });
                        }else {
                            Toast.makeText(getActivity(), "사진을 등록해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==Activity.RESULT_OK){
                contentUri = result.getUri();
                Log.e("asdkljasld",contentUri.getPath());
                Glide.with(getContext()).load(contentUri.getPath()).into(imageview);
            }else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                Exception error = result.getError();
            }

        }
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

}