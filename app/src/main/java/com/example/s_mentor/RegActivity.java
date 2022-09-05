package com.example.s_mentor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import io.grpc.stub.annotations.GrpcGenerated;

public class RegActivity extends AppCompatActivity {
    EditText regId, regPs, regPs2, regNm, regPn;
    String id, ps, ps2, name, pn;
    ProgressBar progressBar;
    Button btNext;
    private FirebaseAuth sAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        getSupportActionBar().hide();

        regId = (EditText) findViewById(R.id.regId);
        regPs = (EditText) findViewById(R.id.regPs);
        regPs2 = (EditText) findViewById(R.id.regPs2);
        regNm = (EditText) findViewById(R.id.regNm);
        regPn = (EditText) findViewById(R.id.regPn);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        btNext = (Button) findViewById(R.id.btNext);

        sAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();


        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = regId.getText().toString();
                if(!(id.contains("@skku.edu"))){
                    Toast.makeText(RegActivity.this, "이메일 형식이 잘못되었습니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ps = regPs.getText().toString();
                ps2 = regPs2.getText().toString();
                if(ps.length()<6){
                    Toast.makeText(RegActivity.this, "비밂번호가 너무 짧습니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!(ps.equals(ps2))){
                    Toast.makeText(RegActivity.this, "비밂번호가 일치하지 않습니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                name = regNm.getText().toString();

                if(name.isEmpty()){
                    Toast.makeText(RegActivity.this, "성함을 입력해주십시오.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }


                pn = regPn.getText().toString();

                if(pn.length()<9 && pn.length()>11){
                    Toast.makeText(RegActivity.this, "올바른 번호를 입력해주십시오.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                sAuth.createUserWithEmailAndPassword(id,ps)
                        .addOnCompleteListener(RegActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if(task.isSuccessful()){
                                    sAuth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(RegActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    HashMap<String, Object> user = new HashMap<>();
                                                    //user.put("email", id);
                                                    user.put("name", name);
                                                    user.put("phone", pn);
                                                    user.put("mentoring", " ");
                                                    user.put("token", " ");
                                                    user.put("first", "o");

                                                    database.collection("users").document(id)
                                                            .set(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(RegActivity.this, "이메일 생성이 성공하였습니다.",
                                                                            Toast.LENGTH_SHORT).show();

                                                                    Intent in= new Intent(RegActivity.this, InformActivity.class);
                                                                    in.putExtra("email", id);
                                                                    startActivity(in);
                                                                }
                                                            });

                                                }
                                            });

                                }
                                else{
                                    Toast.makeText(RegActivity.this, "이메일 생성이 실패하였습니다.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

            }
        });

    }

}