package com.example.s_mentor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    EditText regId, regPs, regPs2, regNm, regMj;
    String id, ps, ps2, name, major, encodedImage;
    ProgressBar progressBar;
    Button signUp;
    ImageView regPh;
    TextView phText;
    FrameLayout imageLayout;
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
        regMj = (EditText) findViewById(R.id.regMj);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        signUp = (Button) findViewById(R.id.btSignUp);
        regPh = (ImageView) findViewById(R.id.regPh);
        phText = (TextView) findViewById(R.id.phText);
        imageLayout = (FrameLayout) findViewById(R.id.imageLayout);

        sAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        imageLayout.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            in.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(in);
        });

        signUp.setOnClickListener(new View.OnClickListener() {
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
                major = regMj.getText().toString();

                if(name.isEmpty()){
                    Toast.makeText(RegActivity.this, "성함을 입력해주십시오.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(major.isEmpty()){
                    Toast.makeText(RegActivity.this, "전공을 입력해주십시오.",
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
                                                    user.put("major", major);
                                                    user.put("image", encodedImage);

                                                    database.collection("users").document(id)
                                                            .set(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(RegActivity.this, "이메일 생성이 성공하였습니다.",
                                                                            Toast.LENGTH_SHORT).show();

                                                                    Intent in= new Intent(RegActivity.this, MainActivity.class);
                                                                    startActivity(in);
                                                                }
                                                            });
                                                          /*  .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Toast.makeText(RegActivity.this, "이메일 생성이 성공하였습니다.",
                                                                            Toast.LENGTH_SHORT).show();

                                                                    Intent in= new Intent(RegActivity.this, MainActivity.class);
                                                                    startActivity(in);
                                                                }
                                                            });*/

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

    private String EncodeImage(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> pickImage
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            regPh.setImageBitmap(bitmap);
                            phText.setVisibility(View.GONE);
                            encodedImage = EncodeImage(bitmap);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

    });
}