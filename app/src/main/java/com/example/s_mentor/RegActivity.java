package com.example.s_mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Objects;

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
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitReg();

        btNext.setOnClickListener(v -> {
            if(!CheckInform()) return;
            progressBar.setVisibility(View.VISIBLE);
            MakeUser();

        });

    }

    private void InitReg(){
        regId = findViewById(R.id.regId);
        regPs = findViewById(R.id.regPs);
        regPs2 = findViewById(R.id.regPs2);
        regNm = findViewById(R.id.regNm);
        regPn = findViewById(R.id.regPn);
        progressBar = findViewById(R.id.progressbar);
        btNext = findViewById(R.id.btNext);
        sAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    private boolean CheckInform(){
        id = regId.getText().toString();
        name = regNm.getText().toString();
        ps = regPs.getText().toString();
        ps2 = regPs2.getText().toString();
        pn = regPn.getText().toString();

        if(name.isEmpty()){
            Toast.makeText(RegActivity.this, "성함을 입력해주십시오.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!(id.contains("@skku.edu"))){
            Toast.makeText(RegActivity.this, "이메일 형식이 잘못되었습니다.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(ps.length()<6){
            Toast.makeText(RegActivity.this, "비밂번호가 너무 짧습니다.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!(ps.equals(ps2))){
            Toast.makeText(RegActivity.this, "비밂번호가 일치하지 않습니다.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(pn.length()<9 || pn.length()>11){
            Toast.makeText(RegActivity.this, "올바른 번호를 입력해주십시오.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void MakeUser(){
        sAuth.createUserWithEmailAndPassword(id,ps)
                .addOnCompleteListener(RegActivity.this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Objects.requireNonNull(sAuth.getCurrentUser()).sendEmailVerification()
                                .addOnCompleteListener(RegActivity.this, task1 -> {

                                    HashMap<String, Object> user = new HashMap<>();
                                    user.put("name", name);
                                    user.put("phone", pn);
                                    user.put("mentoring", " ");
                                    user.put("token", " ");

                                    database.collection("users").document(id)
                                            .set(user)
                                            .addOnSuccessListener(unused -> {
                                                Intent in= new Intent(RegActivity.this, InformActivity.class);
                                                in.putExtra("email", id);
                                                startActivity(in);
                                            });

                                });

                    }
                    else{
                        Toast.makeText(RegActivity.this, "이메일 생성이 실패하였습니다.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

}