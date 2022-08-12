package com.example.s_mentor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {
    TextView proNm;
    TextView proId;
    TextView proMj;
    Button correct;
    String id;
    private FirebaseAuth sAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = getIntent().getStringExtra("email");

        proNm = (TextView) findViewById(R.id.profile_username);
        proId = (TextView) findViewById(R.id.profile_useremail);
        proMj = (TextView) findViewById(R.id.profile_usermajor);
        correct = (Button) findViewById(R.id.correct);

        database = FirebaseFirestore.getInstance();

        database.collection("users").whereEqualTo("email", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                            proNm.setText(documentSnapshot.getData().get("name").toString());
                            proId.setText(documentSnapshot.getData().get("email").toString());
                            proMj.setText(documentSnapshot.getData().get("major").toString());
                        }
                    }
                });


    }
}