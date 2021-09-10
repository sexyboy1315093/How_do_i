package kr.co.softcampus.how_do_i;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;


public class ProfileFragment extends DialogFragment  {

    ImageView profile_image;
    TextView profile_nickname, profile_email;

    ProfileChangeFragment profileChangeFragment;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    DocumentReference documentReference;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection("loginUser").document();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        HomeActivity activity =(HomeActivity)getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = getLayoutInflater();
        View v1 = inflater.inflate(R.layout.fragment_profile,null);

        profile_image = v1.findViewById(R.id.profile_image);
        profile_image.setImageResource(R.drawable.free_icon_male_user_profile_picture_21294);
        profile_nickname = v1.findViewById(R.id.profile_nickname);
        profile_email = v1.findViewById(R.id.profile_email);

        documentReference = db.collection("loginUser").document(firebaseUser.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String email = (String)document.getData().get("email");
                        String name = (String)document.getData().get("name");
                        String profile = (String)document.getData().get("profile");

                        if(profile!=null){
                            Glide.with(getActivity()).load(profile).into(profile_image);
                        }else {
                            Glide.with(getActivity()).load(R.drawable.free_icon_male_user_profile_picture_21294);
                        }

                        profile_nickname.setText(name);
                        profile_email.setText(email);
                    }
                }
            }
        });

        builder.setView(v1);

        builder.setPositiveButton("확인",null);
        builder.setNeutralButton("프로필 변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                profileChangeFragment = new ProfileChangeFragment();
                profileChangeFragment.show(manager,"profile_change");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
//                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.rgb(72,209,204));
            }
        });
        return dialog;
    }

//    @Override
//    public void onBackPressed() {
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction tran = fragmentManager.beginTransaction();
//        tran.remove(this);
//        tran.commit();
//        fragmentManager.popBackStack();
//    }
}