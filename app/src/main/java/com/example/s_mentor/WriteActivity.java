package com.example.s_mentor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class WriteActivity extends AppCompatActivity {

    Button upload;
    EditText write;
    String id, name, text, encodedImage;

    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitBoard();
    }

    private void InitBoard(){
        id = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        upload = findViewById(R.id.add_board);
        write = findViewById(R.id.write_board);

        database.collection("users").document(id)
                .get().addOnCompleteListener(task -> {
                    encodedImage = Objects.requireNonNull(task.getResult().getData().get("image")).toString();
                });

        upload.setOnClickListener(v -> {
            UploadBoard();
        });

    }

    private void UploadBoard(){
        text = write.getText().toString();

        if(text.equals("")){
            Toast.makeText(this, "아무 내용도 작성하지 않았습니다.", Toast.LENGTH_SHORT).show();
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

        database.collection("board").add(question);

        finish();
    }
}