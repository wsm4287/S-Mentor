package com.example.s_mentor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class InformActivity extends AppCompatActivity {

    FrameLayout imageLayout;
    ImageView inform_photo;
    EditText inform_introduction, inform_major;
    Button btType, btSignUp;
    String encodedImage, type, id, major, introduction;

    String[] items = {"취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};
    ArrayAdapter adapter;
    GridView gridView;

    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform);
        getSupportActionBar().hide();

        imageLayout = (FrameLayout) findViewById(R.id.imageLayout);
        inform_photo = (ImageView) findViewById(R.id.inform_photo);
        inform_introduction = (EditText) findViewById(R.id.inform_introduction);
        inform_major = (EditText) findViewById(R.id.inform_major);
        btType = (Button) findViewById(R.id.btType);
        btSignUp = (Button) findViewById(R.id.btSignUp);

        database = FirebaseFirestore.getInstance();

        encodedImage="";
        type="";


        adapter = new ArrayAdapter(this, R.layout.inform_view, items);

        gridView = (GridView) findViewById(R.id.inform_list);
        gridView.setAdapter(adapter);

        id = getIntent().getStringExtra("email");

        imageLayout.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            in.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(in);
        });



        btType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String array[] = {"멘토", "멘티"};

                AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                        .setItems(array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    type = "Mentor";
                                    btType.setText("멘토");
                                }
                                else{
                                    type = "Mentee";
                                    btType.setText("멘티");
                                }

                            }
                        });

                AlertDialog alertDialog = setting.create();
                alertDialog.show();
            }
        });

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                introduction = inform_introduction.getText().toString();
                major = inform_major.getText().toString();

                if(encodedImage.isEmpty()){
                    Toast.makeText(InformActivity.this, "사진을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(major.isEmpty()){
                    Toast.makeText(InformActivity.this, "전공을 입력하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(introduction.isEmpty()){
                    Toast.makeText(InformActivity.this, "자기소개를 입력하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(type.isEmpty()){
                    Toast.makeText(InformActivity.this, "역할을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SparseBooleanArray check = gridView.getCheckedItemPositions();

                if(check.size()<1){
                    Toast.makeText(InformActivity.this, "멘토링 분야를 최소한 하나는 선택해주세요.", Toast.LENGTH_SHORT).show();
                    gridView.setAdapter(adapter);
                    return;
                }

                if(check.size()>4){
                    Toast.makeText(InformActivity.this, "멘토링 분야를 너무 많이 선택하셨습니다.", Toast.LENGTH_SHORT).show();
                    gridView.setAdapter(adapter);
                    return;
                }

                HashMap<String, Object> user = new HashMap<>();

                user.put("image", encodedImage);
                user.put("major", major);
                user.put("introduction", introduction);
                user.put("type", type);

                for(int i=0; i<7; i++){
                    if(check.get(i)){
                        user.put(Integer.toString(i),"o");
                    }
                    else{
                        user.put(Integer.toString(i),"x");
                    }
                }


                database.collection("users").document(id)
                        .update(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(InformActivity.this, "이메일 생성이 성공하였습니다.",
                                        Toast.LENGTH_SHORT).show();

                                Intent in= new Intent(InformActivity.this, MainActivity.class);
                                startActivity(in);
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
                            inform_photo.setImageBitmap(bitmap);
                            encodedImage = EncodeImage(bitmap);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

            });
}