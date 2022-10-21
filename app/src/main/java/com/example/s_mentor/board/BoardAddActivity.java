package com.example.s_mentor.board;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.s_mentor.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class BoardAddActivity extends AppCompatActivity {

    Button upload, add_photo, delete_photo;
    EditText write, title;
    String id, name, text, encodedImage, text_title, downloadUri, fileName, docId, uri;
    Uri imageUri;
    ImageView photo;
    Boolean modify;

    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardadd);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitBoard();
    }

    private void InitBoard(){
        id = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        modify = getIntent().getBooleanExtra("modify", false);
        upload = findViewById(R.id.add_board);
        write = findViewById(R.id.write_board);
        title = findViewById(R.id.write_title);
        add_photo = findViewById(R.id.add_photo);
        photo = findViewById(R.id.photo_board);
        delete_photo = findViewById(R.id.photo_delete);
        fileName = "";
        downloadUri = "";

        photo.setVisibility(View.GONE);
        delete_photo.setVisibility(View.GONE);

        if(modify){
            docId = getIntent().getStringExtra("docId");
            GetBoard();
        }

        database.collection("users").document(id)
                .get().addOnCompleteListener(task -> {
                    encodedImage = Objects.requireNonNull(task.getResult().getData().get("image")).toString();
                });

        add_photo.setOnClickListener(v -> {
            UploadPhoto();
        });

        upload.setOnClickListener(v -> {
            UploadBoard();
        });

        delete_photo.setOnClickListener(v -> {
            photo.setImageResource(0);
            fileName = "";
            downloadUri = "";
            delete_photo.setVisibility(View.GONE);
        });

    }

    private void GetBoard(){
        database.collection("board").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                   text_title = Objects.requireNonNull(documentSnapshot.getData().get("title")).toString();
                   text = Objects.requireNonNull(documentSnapshot.getData().get("text")).toString();
                   fileName = Objects.requireNonNull(documentSnapshot.getData().get("fileName")).toString();

                   title.setText(text_title);
                   write.setText(text);

                   uri = Objects.requireNonNull(documentSnapshot.getData().get("uri")).toString();
                   if(!uri.equals("")){
                       photo.setVisibility(View.VISIBLE);
                       delete_photo.setVisibility(View.VISIBLE);
                       Picasso.get().load(uri).into(photo);
                       downloadUri = uri;
                   }
                });
    }

    private void UploadPhoto(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            selectImage.launch(intent);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
        }

    }

    private void UploadBoard(){
        text_title = title.getText().toString();

        if(text_title.equals("")){
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        text = write.getText().toString();

        if(text.equals("")){
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar c = Calendar.getInstance();
        TimeZone tz;
        tz = TimeZone.getTimeZone("Asia/Seoul");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
        dateFormat.setTimeZone(tz);
        String datetime = dateFormat.format(c.getTime());

        HashMap<String, Object> question = new HashMap<>();
        question.put("email", id);
        question.put("text", text);
        question.put("name", name);
        question.put("image", encodedImage);
        question.put("time", datetime);
        question.put("title", text_title);
        question.put("fileName", fileName);
        question.put("uri", downloadUri);

        if(modify){
            database.collection("board").document(docId).update(question);
            Intent in = new Intent(this, BoardActivity.class).putExtra("docId", docId);
            setResult(100, in);
            if(!isFinishing()) finish();
        }

        else database.collection("board").add(question);
        finish();
    }

    private final ActivityResultLauncher<Intent> selectImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null){
                        imageUri = result.getData().getData();
                        uploadImage(imageUri);
                    }
                }
            }
    );

    private void uploadImage(Uri imageUri){

        fileName = getFileName(imageUri);
        StorageReference storageReference = storage.getReference();

        if(fileName == null){
            Toast.makeText(this,"파일 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            photo.setImageBitmap(bitmap);
            photo.setVisibility(View.VISIBLE);
            delete_photo.setVisibility(View.VISIBLE);
        } catch (IOException e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.PNG,100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        storageReference.child(id).child(fileName).putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful()){
                        if(uriTask.isSuccessful()) break;
                    }
                    downloadUri = uriTask.getResult().toString();
                });
    }

    private String getFileName(Uri uri){
        String name = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if(cursor != null && cursor.moveToFirst()){
                    int c = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(c>=0) name = cursor.getString(c);
                }
            } finally {
                assert cursor != null;
                cursor.close();
            }
            if(name == null){
                name = uri.getPath();
                int last = name.lastIndexOf("/");
                return name.substring(last+1);

            }
            else{
                return name;
            }
        }
        return null;
    }
}