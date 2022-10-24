package com.example.s_mentor;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    EditText mainId, mainPs;
    String id, ps, type, token, name;
    CheckBox autoLogin;
    ProgressBar progressBar;
    Button btLogin, btReg, btFindPs;
    SharedPreferences Auto;
    List<String> userList;
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
        btFindPs = findViewById(R.id.btFindPs);

        Auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor AutoLoginEditor = Auto.edit();
        logout = getIntent().getBooleanExtra("Logout", false);

        userList = new ArrayList<>();

        database.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document : task.getResult()){
                            userList.add(document.getId());
                        }
                    }
                });

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

        btFindPs.setOnClickListener(v ->{
            FindPassword();
        });
    }

    private void Login(){
        btLogin.setOnClickListener(v -> {

            id = mainId.getText().toString();
            ps = mainPs.getText().toString();

            id = id + "@skku.edu";

            if(!userList.contains(id)){
                Toast.makeText(MainActivity.this, "존재하지 않는 이메일입니다.",
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
                            if(sAuth.getCurrentUser().isEmailVerified()){
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
                                Toast.makeText(MainActivity.this, "먼저 이메일 인증을 완료해주세요.",
                                        Toast.LENGTH_SHORT).show();
                            }

/*
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

 */
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

    private void FindPassword(){

        View view = View.inflate(this, R.layout.find_password, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("사용자 정보 입력");
        alert.setView(view);
        alert.setPositiveButton("확인", null).setNegativeButton("취소", null);

        AlertDialog dialog = alert.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();

            if(!userList.contains(id)){
                Toast.makeText(MainActivity.this, "존재하지 않는 이메일입니다.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            database.collection("users").document(email)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString().equals(name)){
                            sAuth.sendPasswordResetEmail(email);
                            Toast.makeText(MainActivity.this, "비밀번호 재설정 이메일을 전송하였습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        else Toast.makeText(MainActivity.this, "사용자 정보가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    });
        });

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