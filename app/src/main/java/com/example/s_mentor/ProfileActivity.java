package com.example.s_mentor;

import android.app.AlertDialog;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    TextView proNm, proMj, proPn, proTp;
    Button correct, changePs;
    String id, encodedImage, type, introduction;
    ImageView proPh;
    EditText proIn;
    private FirebaseFirestore database;
    private FirebaseUser user;

    String[] items = {"취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};
    ArrayAdapter<String> adapter;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitProfile();
        GetProfile();
        CorrectProfile();
    }

    private void InitProfile(){
        id = getIntent().getStringExtra("email");

        proNm = findViewById(R.id.profile_userName);
        proMj = findViewById(R.id.profile_userMajor);
        proPh = findViewById(R.id.profile_photo);
        proPn = findViewById(R.id.profile_userPhone);
        proTp = findViewById(R.id.profile_type);
        correct = findViewById(R.id.correct);
        proIn = findViewById(R.id.profile_userIntro);
        changePs = findViewById(R.id.changePs);


        adapter = new ArrayAdapter<>(this, R.layout.inform_view, items);

        gridView = findViewById(R.id.profile_list);
        gridView.setAdapter(adapter);

        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        changePs.setOnClickListener(v -> {
            ChangePassword();
        });
    }

    private void GetProfile(){
        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    proNm.setText(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString());
                    proMj.setText(Objects.requireNonNull(documentSnapshot.getData().get("major")).toString());

                    encodedImage = Objects.requireNonNull(documentSnapshot.getData().get("image")).toString();
                    proPh.setImageBitmap(DecodeImage(encodedImage));

                    proPn.setText(Objects.requireNonNull(documentSnapshot.getData().get("phone")).toString());

                    introduction = Objects.requireNonNull(documentSnapshot.getData().get("introduction")).toString();
                    proIn.setText(introduction);

                    type = Objects.requireNonNull(documentSnapshot.getData().get("type")).toString();
                    proTp.setText(type);

                    for(int i=0; i<7; i++){
                        if(((Objects.requireNonNull(documentSnapshot.getData().get(Integer.toString(i)))).toString()).equals("o")){
                            gridView.setItemChecked(i,true);
                        }
                    }

                });
    }
    private void CorrectProfile(){
        proPh.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            in.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(in);
        });

        correct.setOnClickListener(v -> {

            SparseBooleanArray check = gridView.getCheckedItemPositions();

            if(check.size()<1){
                Toast.makeText(ProfileActivity.this, "멘토링 분야를 최소한 하나는 선택해주세요.", Toast.LENGTH_SHORT).show();
                gridView.setAdapter(adapter);
                return;
            }

            if(check.size()>5){
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

        });
    }

    private void ChangePassword(){
        View view = View.inflate(this, R.layout.change_password, null);
        EditText etBefore = view.findViewById(R.id.etBefore);
        EditText etNew = view.findViewById(R.id.etNew);
        EditText etNew2 = view.findViewById(R.id.etNew2);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("비밀번호 변경");
        alert.setView(view);

        alert.setPositiveButton("확인", null).setNegativeButton("취소", null);

        AlertDialog dialog = alert.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String before = etBefore.getText().toString();
            String psNew = etNew.getText().toString();
            String psNew2 = etNew2.getText().toString();
            AuthCredential authCredential = EmailAuthProvider.getCredential(id, before);
            user.reauthenticate(authCredential)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(psNew.equals(psNew2)){
                                user.updatePassword(psNew);
                                Toast.makeText(ProfileActivity.this, "비밀번호가 변경 되었습니다.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            else Toast.makeText(ProfileActivity.this, "비밀번호가 확인과 다릅니다.", Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(ProfileActivity.this, "이전 비밀번호가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    });
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