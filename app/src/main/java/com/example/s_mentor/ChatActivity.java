package com.example.s_mentor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatActivity extends AppCompatActivity {
    TextView opNm;
    ImageView opPh;
    String id, id2;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        database = FirebaseFirestore.getInstance();

        opNm = (TextView) findViewById(R.id.opName);
        opPh = (ImageView) findViewById(R.id.opPhoto);
        id = getIntent().getStringExtra("email1");
        id2 = getIntent().getStringExtra("email2");

        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        opNm.setText(documentSnapshot.getData().get("name").toString());
                        opPh.setImageBitmap(DecodeImage(documentSnapshot.getData().get("image").toString()));

                    }
                });

        opNm.setText(id2);

    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}