package com.example.s_mentor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

public class ImageActivity extends AppCompatActivity {
    String image, fileName;
    ImageView dnImage;
    Button btDown;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Objects.requireNonNull(getSupportActionBar()).hide();

        image = getIntent().getStringExtra("image");
        fileName = getIntent().getStringExtra("fileName");
        dnImage = (ImageView) findViewById(R.id.dnImage);
        btDown = (Button) findViewById(R.id.btDown);
        storage = FirebaseStorage.getInstance();

        Picasso.get().load(image).into(dnImage);

        StorageReference storageReference = storage.getReferenceFromUrl(image);

        btDown.setOnClickListener(v -> {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, fileName);
            storageReference.getFile(file).addOnSuccessListener(taskSnapshot -> Toast.makeText(ImageActivity.this, "파일을 다운로드 하였습니다.", Toast.LENGTH_SHORT).show());

        });

    }
}