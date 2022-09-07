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
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    TextView proNm, proMj, proPn, proTp;
    Button correct;
    String id, encodedImage, type, introduction;
    ImageView proPh;
    EditText proIn;
    private FirebaseFirestore database;

    String[] items = {"취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};
    ArrayAdapter adapter;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().hide();

        id = getIntent().getStringExtra("email");

        proNm = (TextView) findViewById(R.id.profile_userName);
        proMj = (TextView) findViewById(R.id.profile_userMajor);
        proPh = (ImageView) findViewById(R.id.profile_photo);
        proPn = (TextView) findViewById(R.id.profile_userPhone);
        proTp = (TextView) findViewById(R.id.profile_type);
        correct = (Button) findViewById(R.id.correct);
        proIn = (EditText) findViewById(R.id.profile_userIntro);


        adapter = new ArrayAdapter(this, R.layout.inform_view, items);

        gridView = (GridView) findViewById(R.id.profile_list);
        gridView.setAdapter(adapter);

        database = FirebaseFirestore.getInstance();

        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        proNm.setText(documentSnapshot.getData().get("name").toString());
                        proMj.setText(documentSnapshot.getData().get("major").toString());

                        encodedImage = documentSnapshot.getData().get("image").toString();
                        proPh.setImageBitmap(DecodeImage(encodedImage));

                        proPn.setText(documentSnapshot.getData().get("phone").toString());

                        introduction = documentSnapshot.getData().get("introduction").toString();
                        proIn.setText(introduction);

                        type = documentSnapshot.getData().get("type").toString();
                        proTp.setText(type);

                        for(int i=0; i<7; i++){
                            if(((documentSnapshot.getData().get(Integer.toString(i))).toString()).equals("o")){
                                gridView.setItemChecked(i,true);
                            }
                        }

                    }
                });


        proPh.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            in.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(in);
        });

        correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SparseBooleanArray check = gridView.getCheckedItemPositions();

                if(check.size()<1){
                    Toast.makeText(ProfileActivity.this, "멘토링 분야를 최소한 하나는 선택해주세요.", Toast.LENGTH_SHORT).show();
                    gridView.setAdapter(adapter);
                    return;
                }

                if(check.size()>4){
                    Toast.makeText(ProfileActivity.this, "멘토링 분야를 너무 많이 선택하셨습니다.", Toast.LENGTH_SHORT).show();
                    gridView.setAdapter(adapter);
                    return;
                }

                introduction = proIn.getText().toString();
                HashMap<String, Object> user = new HashMap<>();
                user.put("image", encodedImage);
                user.put("introduction", introduction);

                for(int i=0; i<7; i++){
                    if(check.get(i)){
                        user.put(Integer.toString(i),"o");
                    }
                    else{
                        user.put(Integer.toString(i),"x");
                    }
                }

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