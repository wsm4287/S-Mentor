package com.example.s_mentor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText mainId, mainPs;
    String id, ps;
    CheckBox autoLogin;
    ProgressBar progressBar;
    Button btLogin;
    Button btReg;
    SharedPreferences Auto;
    boolean logout;

    private FirebaseAuth sAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        sAuth =FirebaseAuth.getInstance();
        mainId = (EditText) findViewById(R.id.mainId);
        mainPs = (EditText) findViewById(R.id.mainPs);
        autoLogin = (CheckBox) findViewById(R.id.autoLogin);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        btLogin = (Button) findViewById(R.id.btLogin);
        btReg = (Button) findViewById(R.id.btReg);

        Auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor AutoLoginEditor = Auto.edit();
        logout = getIntent().getBooleanExtra("Logout", false);

        if(logout == true){
            Toast.makeText(MainActivity.this, "로그아웃 하였습니다.",
                    Toast.LENGTH_SHORT).show();
            AutoLoginEditor.clear();
            AutoLoginEditor.commit();
            sAuth.getInstance().signOut();
        }

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = mainId.getText().toString();
                ps = mainPs.getText().toString();

                if(!(id.contains("@"))){
                    Toast.makeText(MainActivity.this, "잘못된 이메일 형식입니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                sAuth.signInWithEmailAndPassword(id,ps)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
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

                                        Intent in = new Intent(MainActivity.this, HomeActivity.class);
                                        in.putExtra("email", id);
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
                                        autoLoginEditor.commit();
                                    }
                                    else{
                                        autoLoginEditor.clear();
                                        autoLoginEditor.commit();
                                    }

                                    Intent in = new Intent(MainActivity.this, HomeActivity.class);
                                    in.putExtra("email", id);
                                    startActivity(in);

                                }
                                else{
                                    Toast.makeText(MainActivity.this, "이메일 혹은 비밀번호가 잘못 되었습니다.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

            }
        });

        if(Auto.getBoolean("check", false)){
            /*mainId.setText(Auto.getString("email", null));
            mainPs.setText(Auto.getString("password", null));
            autoLogin.setChecked(true);
            btLogin.performClick();*/
            id = Auto.getString("email", null);
            Intent in = new Intent(MainActivity.this, HomeActivity.class);
            in.putExtra("email", id);
            startActivity(in);
        }

        btReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, RegActivity.class);
                startActivity(in);
            }
        });



    }
}