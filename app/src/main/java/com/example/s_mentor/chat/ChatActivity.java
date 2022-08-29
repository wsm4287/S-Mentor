package com.example.s_mentor.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.example.s_mentor.HomeActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.notification.SendMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    TextView opNm;
    ImageView chBk, opPh;
    Button btSend;
    EditText etText;
    String id, id2, token, name, encodedImage;
    FirebaseFirestore database;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    ChatAdapter chatAdapter;
    ArrayList<Chat> chatArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        database = FirebaseFirestore.getInstance();

        opNm = (TextView) findViewById(R.id.opName);
        chBk = (ImageView) findViewById(R.id.chBack);
        opPh = (ImageView) findViewById(R.id.opPhoto);
        etText = (EditText) findViewById(R.id.etText);
        btSend = (Button) findViewById(R.id.btSend);
        id = getIntent().getStringExtra("email1");
        id2 = getIntent().getStringExtra("email2");
        chatArrayList = new ArrayList<>();

        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name = documentSnapshot.getData().get("name").toString();
                        encodedImage = documentSnapshot.getData().get("image").toString();
                    }
                });

        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        opNm.setText(documentSnapshot.getData().get("name").toString());
                        opPh.setImageBitmap(DecodeImage(documentSnapshot.getData().get("image").toString()));
                        token = documentSnapshot.getData().get("token").toString();
                    }
                });

        opNm.setText(id2);

        chBk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Finish chat", Toast.LENGTH_LONG).show();
                Intent in = new Intent(ChatActivity.this, HomeActivity.class);
                in.putExtra("email", id);
                startActivity(in);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(chatArrayList, id);
        recyclerView.setAdapter(chatAdapter);



        database.collection("message").document(id).collection(id2)
                .orderBy("time")
                .limitToLast(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            chatArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Chat chat = new Chat();
                                chat.datetime = document.getData().get("time").toString();
                                chat.text = document.getData().get("text").toString();
                                chat.email = document.getData().get("email").toString();
                                chatArrayList.add(chat);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        layoutManager.scrollToPosition(chatArrayList.size()-1);
                    }
                });



        database.collection("message").document(id).collection(id2)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (QueryDocumentSnapshot document : value) {
                            Chat chat = new Chat();
                            chat.datetime = document.getData().get("time").toString();
                            chat.text = document.getData().get("text").toString();
                            chat.email = document.getData().get("email").toString();
                            chatArrayList.add(chat);
                            chatAdapter.notifyDataSetChanged();
                            layoutManager.scrollToPosition(chatArrayList.size()-1);

                        }
                    }
                });


        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stText = etText.getText().toString();

                Calendar c = Calendar.getInstance();
                TimeZone tz;
                tz = TimeZone.getTimeZone("Asia/Seoul");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                dateFormat.setTimeZone(tz);
                String datetime = dateFormat.format(c.getTime());


                Map<String, Object> message = new HashMap<>();
                message.put("time", datetime);
                message.put("text", stText);
                message.put("email", id);

                database.collection("message").document(id).collection(id2)
                        .add(message);

                database.collection("message").document(id2).collection(id)
                        .add(message);

                Map<String, Object> recent = new HashMap<>();

                recent.put("text", stText);


                database.collection("message").document(id).collection("Last")
                        .document(id2).set(recent);

                database.collection("message").document(id2).collection("Last")
                        .document(id).set(recent);


                etText.setText("\0");

                SendMessage.notification(
                        ChatActivity.this,
                        token,
                        id,
                        stText,
                        name
                );

            }
        });
    }



    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}