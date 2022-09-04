package com.example.s_mentor.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.BoringLayout;
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
    String id, id2, token, token2, name, name2, type, type2, encodedImage, major;
    Bitmap bitmap;
    FirebaseFirestore database;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    ChatAdapter chatAdapter;
    ArrayList<Chat> chatArrayList;
    boolean check;

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
        id = getIntent().getStringExtra("email");
        id2 = getIntent().getStringExtra("email2");
        type = getIntent().getStringExtra("type");
        chatArrayList = new ArrayList<>();
        check = true;


        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name = documentSnapshot.getData().get("name").toString();
                        encodedImage = documentSnapshot.getData().get("image").toString();
                        major = documentSnapshot.getData().get("major").toString();
                        token = documentSnapshot.getData().get("token").toString();
                        if(!(documentSnapshot.getData().get("mentoring").toString().equals(" "))) check = false;
                    }
                });


        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name2 = documentSnapshot.getData().get("name").toString();
                        opNm.setText(name2);
                        bitmap = DecodeImage(documentSnapshot.getData().get("image").toString());
                        opPh.setImageBitmap(bitmap);
                        token2 = documentSnapshot.getData().get("token").toString();
                        type2 = documentSnapshot.getData().get("type").toString();
                        if(!(documentSnapshot.getData().get("mentoring").toString().equals(" "))) check = false;
                    }
                });

        opNm.setText(id2);

        chBk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Finish chat", Toast.LENGTH_LONG).show();
                Intent in = new Intent(ChatActivity.this, HomeActivity.class);
                in.putExtra("email", id);
                in.putExtra("type", type);
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
                        token2,
                        id,
                        stText,
                        name,
                        id2,
                        type2
                );

            }
        });



        opPh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check) {
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    final ImageView iv = new ImageView(ChatActivity.this);
                    iv.setImageBitmap(bitmap);
                    final TextView et = new TextView(ChatActivity.this);
                    et.setText("멘토링을 신청하시겠습니까?");
                    AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this)
                            .setView(iv)
                            .setIcon(drawable)
                            .setTitle(name2)
                            .setMessage(id2).setView(et)
                            .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNeutralButton("신청", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String, Object> apply = new HashMap<>();
                                    apply.put("from", id);
                                    apply.put("to", id2);
                                    apply.put("name", name);
                                    apply.put("major", major);
                                    apply.put("image", encodedImage);
                                    apply.put("token", token);

                                    database.collection("apply")
                                            .add(apply);

                                    SendMessage.notification(
                                            ChatActivity.this,
                                            token2,
                                            id,
                                            name + "님이 멘토링을 신청하였습니다.",
                                            name,
                                            id2,
                                            "XX"
                                    );
                                }
                            });

                    ad.show();
                }
                else{
                    Toast.makeText(ChatActivity.this,"이미 멘토링 상대가 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}