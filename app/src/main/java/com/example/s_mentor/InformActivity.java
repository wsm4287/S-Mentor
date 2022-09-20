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
    EditText inform_introduction;
    Button btType, btSignUp, inform_major;
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
        inform_major = (Button) findViewById(R.id.inform_major);
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

        inform_major.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectMajor();
            }
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

    private void SelectMajor(){
        String array[] = {"유학대학", "문과대학", "사회과학대학", "경제대학", "경영대학", "사범대학", "예술대학", "자연과학대학",
                "정보통신대학", "소프트웨어융합대학", "공과대학", "약학대학", "생명공학대학", "스포츠과학대학", "의과대학", "성균융합원"};

        AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            String a[] = {"유학.동양학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 1){
                            String a[] = {"국어국문학과", "영어영문학과", "프랑스어문학과", "중어중문학과", "독어독문학과",
                            "러시아어문학과", "한문학과", "사학과", "철학과", "문헌정보학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else if(which == 4){
                                                major = a[4];
                                                return;
                                            }
                                            else if(which == 5){
                                                major = a[5];
                                                return;
                                            }
                                            else if(which == 6){
                                                major = a[6];
                                                return;
                                            }
                                            else if(which == 7){
                                                major = a[7];
                                                return;
                                            }
                                            else if(which == 8){
                                                major = a[8];
                                                return;
                                            }
                                            else if(which == 9){
                                                major = a[9];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 2){
                            String a[] = {"행정학과", "정치외교학과", "미디어커뮤니케이션학과", "사회학과", "사회복지학과",
                                    "심리학과", "소비자학과", "아동·청소년학과", "글로벌리더학부", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else if(which == 4){
                                                major = a[4];
                                                return;
                                            }
                                            else if(which == 5){
                                                major = a[5];
                                                return;
                                            }
                                            else if(which == 6){
                                                major = a[6];
                                                return;
                                            }
                                            else if(which == 7){
                                                major = a[7];
                                                return;
                                            }
                                            else if(which == 8){
                                                major = a[8];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 3){
                            String a[] = {"경제학과", "통계학과", "글로벌경제학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 4){
                            String a[] = {"경영학과", "글로벌경영학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 5){
                            String a[] = {"교육학과", "한문교육과", "수학교육과", "컴퓨터교육과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 6){
                            String a[] = {"미술학과", "디자인학과", "무용학과", "영상학과", "연기예술학과",
                                    "의상학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else if(which == 4){
                                                major = a[4];
                                                return;
                                            }
                                            else if(which == 5){
                                                major = a[5];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 7){
                            String a[] = {"생명과학과", "수학과", "물리학과", "화학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 8){
                            String a[] = {"전자전기공학부", "반도체시스템공학과", "소재부품융합공학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 9){
                            String a[] = {"소프트웨어학과", "글로벌융합학부", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 10){
                            String a[] = {"화학공학/고분자공학부", "신소재공학부", "기계공학부", "건설환경공학부",
                            "시스템경영공학과", "건축학과", "나노공학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else if(which == 3){
                                                major = a[3];
                                                return;
                                            }
                                            else if(which == 4){
                                                major = a[4];
                                                return;
                                            }
                                            else if(which == 5){
                                                major = a[5];
                                                return;
                                            }
                                            else if(which == 6){
                                                major = a[6];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 11){
                            String a[] = {"약학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 12){
                            String a[] = {"식품생명공학과", "바이오메카트로닉스학과", "융합생명공학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else if(which == 1){
                                                major = a[1];
                                                return;
                                            }
                                            else if(which == 2){
                                                major = a[2];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 13){
                            String a[] = {"스포츠과학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 14){
                            String a[] = {"의학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else if(which == 15){
                            String a[] = {"글로벌바이오메디컬공학과", "돌아가기"};

                            AlertDialog.Builder setting = new AlertDialog.Builder(InformActivity.this)
                                    .setItems(a, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                major = a[0];
                                                return;
                                            }
                                            else SelectMajor();
                                            inform_major.setText(major);
                                        }
                                    });

                            AlertDialog alertDialog = setting.create();
                            alertDialog.show();
                        }
                        else{

                        }
                    }
                });

        AlertDialog alertDialog = setting.create();
        alertDialog.show();
    }

}