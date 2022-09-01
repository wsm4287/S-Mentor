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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    TextView proNm, proMj, proPn, proTp;
    Button correct;
    String id, encodedImage, type;
    ImageView proPh;
    private FirebaseAuth sAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = getIntent().getStringExtra("email");

        proNm = (TextView) findViewById(R.id.profile_username);
        proMj = (TextView) findViewById(R.id.profile_usermajor);
        proPh = (ImageView) findViewById(R.id.profile_photo);
        proPn = (TextView) findViewById(R.id.profile_userphone);
        proTp = (TextView) findViewById(R.id.profile_type);
        correct = (Button) findViewById(R.id.correct);

        database = FirebaseFirestore.getInstance();

        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        proNm.setText(documentSnapshot.getData().get("name").toString());
                        proMj.setText(documentSnapshot.getData().get("major").toString());
                        proPh.setImageBitmap(DecodeImage(documentSnapshot.getData().get("image").toString()));
                        proPn.setText(documentSnapshot.getData().get("phone").toString());
                        type = documentSnapshot.getData().get("type").toString();
                        proTp.setText(type);
                    }
                });
                /*.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                            proNm.setText(documentSnapshot.getData().get("name").toString());
                            proId.setText(documentSnapshot.getData().get("email").toString());
                            proMj.setText(documentSnapshot.getData().get("major").toString());
                            proPh.setImageBitmap(DecodeImage(documentSnapshot.getData().get("image").toString()));
                        }
                    }
                });*/

        proPh.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            in.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(in);
        });

        correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> user = new HashMap<>();
                user.put("image", encodedImage);
                database.collection("users").document(id)
                        .update(user);
                Toast.makeText(ProfileActivity.this, "개인정보 변경이 완료되었습니다.",
                        Toast.LENGTH_SHORT).show();

            }
        });


    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
                            proPh.setImageBitmap(bitmap);
                            encodedImage = EncodeImage(bitmap);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

            });
}