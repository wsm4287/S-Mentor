package com.example.s_mentor.board;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.s_mentor.ImageActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.notification.SendMessage;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class BoardActivity extends AppCompatActivity {
    String id, id2, title, text, time, encodedImage, name, name2, image, fileName, docId, encodedImage2, token, type;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    ImageView imageView, photoView;
    TextView nameView, timeView, titleView, boardView;
    EditText writeAnswer;
    Button input, menu;
    Bitmap bitmap, bitmap2;
    ArrayList<Answer> answerArrayList;
    AnswerAdapter answerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitAnswer();
        GetBoard();
        InputAnswer();

    }

    private void InitAnswer(){
        id = getIntent().getStringExtra("email1");
        id2 = getIntent().getStringExtra("email2");
        name = getIntent().getStringExtra("name");
        title = getIntent().getStringExtra("title");
        imageView = findViewById(R.id.imageView);
        nameView = findViewById(R.id.nameText);
        timeView = findViewById(R.id.timeText);
        titleView = findViewById(R.id.titleText);
        boardView = findViewById(R.id.boardText);
        photoView = findViewById(R.id.boardPhoto);
        photoView.setVisibility(View.GONE);
        writeAnswer = findViewById(R.id.writeAnswer);
        input = findViewById(R.id.inputAnswer);
        menu = findViewById(R.id.menu);

        if(!id.equals(id2)) menu.setVisibility(View.GONE);

        menu.setOnClickListener(v -> {
            String[] array = {"??????", "??????"};

            AlertDialog.Builder menu = new AlertDialog.Builder(this)
                    .setItems(array, (dialog, which) -> {
                       if(which == 0){
                           Intent in = new Intent(this, BoardAddActivity.class);
                           in.putExtra("modify", true);
                           in.putExtra("email", id);
                           in.putExtra("name", name);
                           in.putExtra("docId", docId);
                           getResult.launch(in);
                       }
                       else{
                           database.collection("board").document(docId).delete();
                           finish();
                       }
                    });

            AlertDialog alertDialog = menu.create();
            alertDialog.show();
        });

        answerArrayList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        answerAdapter = new AnswerAdapter(answerArrayList);
        recyclerView.setAdapter(answerAdapter);
    }

    private void GetBoard(){

        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    token = Objects.requireNonNull(documentSnapshot.getData().get("token")).toString();
                    type = Objects.requireNonNull(documentSnapshot.getData().get("type")).toString();
                });

        database.collection("board")
                .whereEqualTo("email", id2)
                .whereEqualTo("title", title)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            docId = document.getId() + "";
                            GetAnswer();

                            name2 = Objects.requireNonNull(document.getData().get("name")).toString();
                            text = Objects.requireNonNull(document.getData().get("text")).toString();
                            time = Objects.requireNonNull(document.getData().get("time")).toString();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            bitmap = DecodeImage(encodedImage);
                            image = Objects.requireNonNull(document.getData().get("uri")).toString();
                            fileName = Objects.requireNonNull(document.getData().get("fileName")).toString();

                            imageView.setImageBitmap(bitmap);
                            nameView.setText(name2);
                            timeView.setText(time);
                            titleView.setText(title);
                            boardView.setText(text);

                            if(!image.equals("")){
                                Picasso.get().load(image).into(photoView);
                                photoView.setVisibility(View.VISIBLE);
                                photoView.setOnClickListener(v -> {
                                    Intent in = new Intent(this, ImageActivity.class);
                                    in.putExtra("image", image);
                                    in.putExtra("fileName", fileName);
                                    startActivity(in);
                                });
                            }

                        }

                    }
                });

    }

    private void GetAnswer(){

        database.collection("board").document(docId).collection("answer")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    assert value != null;
                    answerArrayList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Answer answer = new Answer();

                        answer.email = Objects.requireNonNull(document.getData().get("email")).toString();
                        if(id.equals(answer.email)) answer.check = true;
                        else answer.check = false;
                        answer.name = Objects.requireNonNull(document.getData().get("name")).toString();
                        answer.text = Objects.requireNonNull(document.getData().get("text")).toString();
                        answer.time = Objects.requireNonNull(document.getData().get("time")).toString();
                        encodedImage2 = Objects.requireNonNull(document.getData().get("image")).toString();
                        bitmap2 = DecodeImage(encodedImage2);
                        answer.bitmap = bitmap2;
                        answer.docId1 = docId;
                        answer.docId2 = document.getId();

                        answerArrayList.add(answer);
                    }
                    answerAdapter.notifyDataSetChanged();
                });



    }

    private void InputAnswer(){
        input.setOnClickListener(v -> {
            String ans = writeAnswer.getText().toString();
            if(ans.equals("")) return;

            Calendar c = Calendar.getInstance();
            TimeZone tz;
            tz = TimeZone.getTimeZone("Asia/Seoul");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
            dateFormat.setTimeZone(tz);
            String datetime = dateFormat.format(c.getTime());

            HashMap<String, Object> map = new HashMap<>();
            map.put("email", id);
            map.put("text", ans);
            map.put("image", encodedImage);
            map.put("name", name);
            map.put("time", datetime);

            database.collection("board").document(docId).collection("answer")
                    .add(map);

            writeAnswer.setText("\0");

            if(id.equals(id2)) return;

            SendMessage.notification(
                    this,
                    token,
                    id,
                    ans,
                    name,
                    id2,
                    type
            );

        });
    }

    private final ActivityResultLauncher<Intent> getResult
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == 100) {
                    InitAnswer();

                    database.collection("users").document(id2)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                token = Objects.requireNonNull(documentSnapshot.getData().get("token")).toString();
                                type = Objects.requireNonNull(documentSnapshot.getData().get("type")).toString();
                            });


                    docId = result.getData().getStringExtra("docId");

                    database.collection("board").document(docId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                name2 = Objects.requireNonNull(documentSnapshot.getData().get("name")).toString();
                                text = Objects.requireNonNull(documentSnapshot.getData().get("text")).toString();
                                time = Objects.requireNonNull(documentSnapshot.getData().get("time")).toString();
                                encodedImage = Objects.requireNonNull(documentSnapshot.getData().get("image")).toString();
                                bitmap = DecodeImage(encodedImage);
                                image = Objects.requireNonNull(documentSnapshot.getData().get("uri")).toString();
                                fileName = Objects.requireNonNull(documentSnapshot.getData().get("fileName")).toString();
                                title = Objects.requireNonNull(documentSnapshot.getData().get("title")).toString();


                                imageView.setImageBitmap(bitmap);
                                nameView.setText(name2);
                                timeView.setText(time);
                                titleView.setText(title);
                                boardView.setText(text);

                                if(!image.equals("")){
                                    Picasso.get().load(image).into(photoView);
                                    photoView.setVisibility(View.VISIBLE);
                                    photoView.setOnClickListener(v -> {
                                        Intent in = new Intent(this, ImageActivity.class);
                                        in.putExtra("image", image);
                                        in.putExtra("fileName", fileName);
                                        startActivity(in);
                                    });
                                }
                            });


                    GetAnswer();
                }

            });



    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}