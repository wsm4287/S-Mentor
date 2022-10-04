package com.example.s_mentor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    EditText mainId, mainPs;
    String id, ps, type, token, name;
    CheckBox autoLogin;
    ProgressBar progressBar;
    Button btLogin;
    Button btReg;
    SharedPreferences Auto;
    boolean logout;

    private FirebaseAuth sAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitMain();
        Login();
        CheckAutoLogin();

    }

    private void InitMain(){
        sAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        mainId = findViewById(R.id.mainId);
        mainPs = findViewById(R.id.mainPs);
        autoLogin = findViewById(R.id.autoLogin);
        progressBar = findViewById(R.id.progressbar);
        btLogin = findViewById(R.id.btLogin);
        btReg = findViewById(R.id.btReg);

        Auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor AutoLoginEditor = Auto.edit();
        logout = getIntent().getBooleanExtra("Logout", false);


        if(logout){
            Toast.makeText(MainActivity.this, "로그아웃 하였습니다.",
                    Toast.LENGTH_SHORT).show();
            AutoLoginEditor.clear();
            AutoLoginEditor.apply();
            FirebaseAuth.getInstance().signOut();
        }

        btReg.setOnClickListener(v -> {
            Intent in = new Intent(MainActivity.this, RegActivity.class);
            startActivity(in);
        });
    }

    private void Login(){
        btLogin.setOnClickListener(v -> {

            id = mainId.getText().toString();
            ps = mainPs.getText().toString();

            if(!(id.contains("@"))){
                Toast.makeText(MainActivity.this, "잘못된 이메일 형식입니다.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            database.collection("users").document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("type")).toString().equals("Mentor")) type = "Mentor";
                        else type = "Mentee";
                        name = Objects.requireNonNull(documentSnapshot.getData().get("name")).toString();
                    });

            progressBar.setVisibility(View.VISIBLE);

            CheckAuthentication();


        });

    }

    private void CheckAuthentication(){
        sAuth.signInWithEmailAndPassword(id,ps)
                .addOnCompleteListener(MainActivity.this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                            /*if(sAuth.getCurrentUser().isEmailVerified()){
                                SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor autoLoginEditor = auto.edit();

                                if(autoLogin.isChecked()){
                                    autoLoginEditor.putString("email", id);
                                    autoLoginEditor.putString("password", ps);
                                    autoLoginEditor.putBoolean("check", true);
                                    autoLoginEditor.commit();
                                }
                                else{
                                    autoLoginEditor.clear();
                                    autoLoginEditor.commit();
                                }

                                updateToken();
                                Intent in = new Intent(MainActivity.this, HomeActivity.class);
                                in.putExtra("email", id);
                                in.putExtra("type", type);
                                in.putExtra("name", name);
                                startActivity(in);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "먼저 이메일 인증을 완료해주세요.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            */


                        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor autoLoginEditor = auto.edit();

                        if(autoLogin.isChecked()){
                            autoLoginEditor.putString("email", id);
                            autoLoginEditor.putString("password", ps);
                            autoLoginEditor.putBoolean("check", true);
                            autoLoginEditor.apply();
                        }
                        else{
                            autoLoginEditor.clear();
                            autoLoginEditor.apply();
                        }

                        updateToken();
                        Intent in = new Intent(MainActivity.this, HomeActivity.class);
                        in.putExtra("email", id);
                        in.putExtra("type", type);
                        in.putExtra("name", name);
                        startActivity(in);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "이메일 혹은 비밀번호가 잘못 되었습니다.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void CheckAutoLogin(){
        if(Auto.getBoolean("check", false)){
            /*mainId.setText(Auto.getString("email", null));
            mainPs.setText(Auto.getString("password", null));
            autoLogin.setChecked(true);
            btLogin.performClick();*/

            id = Auto.getString("email", null);
            database.collection("users").document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("type")).toString().equals("Mentor")) type = "Mentor";
                        else type = "Mentee";

                        name = Objects.requireNonNull(documentSnapshot.getData().get("name")).toString();
                        updateToken();

                        Intent in = new Intent(MainActivity.this, HomeActivity.class);
                        in.putExtra("email", id);
                        in.putExtra("type", type);
                        in.putExtra("name", name);
                        startActivity(in);

                    });
        }
    }

    private void updateToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                token = task.getResult();
                HashMap<String, Object> user = new HashMap<>();
                user.put("token", token);
                database.collection("users").document(id)
                        .update(user)
                        .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, "로그인 하셨습니다.",
                                Toast.LENGTH_SHORT).show());
            }

        });

    }

}