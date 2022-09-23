package com.example.s_mentor.chat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.BoringLayout;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    TextView opNm;
    ImageView chBk, opPh;
    Button btSend, btFile, btImage;
    EditText etText;
    String id, id2, token, token2, name, name2, type, type2, encodedImage, major;
    Bitmap bitmap;
    FirebaseFirestore database;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    ChatAdapter chatAdapter;
    ArrayList<Chat> chatArrayList;
    boolean check;
    Uri fileUri, imageUri;
    FirebaseStorage storage;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        ChatInitial();
        LoadUserData();
        ViewChat();
        SendChat();
        ApplyMentoring();

    }

    private void ChatInitial(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        opNm = (TextView) findViewById(R.id.opName);
        chBk = (ImageView) findViewById(R.id.chBack);
        opPh = (ImageView) findViewById(R.id.opPhoto);
        etText = (EditText) findViewById(R.id.etText);
        btSend = (Button) findViewById(R.id.btSend);
        btFile = (Button) findViewById(R.id.btFile);
        btImage = (Button) findViewById(R.id.btImage);
        progressBar = (ProgressBar) findViewById(R.id.progress_file);

        id = getIntent().getStringExtra("email");
        id2 = getIntent().getStringExtra("email2");
        type = getIntent().getStringExtra("type");
        chatArrayList = new ArrayList<>();
        check = true;
        opNm.setText(id2);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(chatArrayList, id);
        recyclerView.setAdapter(chatAdapter);

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

        btFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    selectFile.launch(intent);
                }
                else{
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }

            }
        });

        btImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    selectImage.launch(intent);
                }
                else{
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }
            }
        });
    }

/*    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("");
            selectFile.launch(intent);
        }
        else{
            Toast.makeText(ChatActivity.this, "권한을 승인해주세요", Toast.LENGTH_SHORT).show();
        }
    }
*/

    private void LoadUserData(){
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
    }

    private void ViewChat(){

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
                                chat.type = document.getData().get("type").toString();
                                if(chat.type.equals("2") || chat.type.equals("3")){
                                    chat.upImage = document.getData().get("uri").toString();
                                }
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
                            chat.type = document.getData().get("type").toString();
                            if(chat.type.equals("2") || chat.type.equals("3")){
                                chat.upImage = document.getData().get("uri").toString();
                            }
                            chatArrayList.add(chat);
                        }
                        chatAdapter.notifyDataSetChanged();
                        layoutManager.scrollToPosition(chatArrayList.size()-1);
                    }
                });


    }


    private void SendChat(){

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
                message.put("type", 0);

                database.collection("message").document(id).collection(id2)
                        .add(message);

                message.replace("type", 1);

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
    }


    private void ApplyMentoring(){

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

    private void uploadImage(Uri imageUri) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_DD_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = format.format(now);
        StorageReference storageReference = storage.getReference();

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();


        progressBar.setVisibility(View.VISIBLE);

        storageReference.child(id).child(fileName).putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> image = new HashMap<>();
                            image.put("sender", id);
                            image.put("receiver", id2);
                            image.put("uri", downloadUri);
                            image.put("isSeen", "false");

                            database.collection("image").add(image);
                        }
                        Calendar c = Calendar.getInstance();
                        TimeZone tz;
                        tz = TimeZone.getTimeZone("Asia/Seoul");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        dateFormat.setTimeZone(tz);
                        String datetime = dateFormat.format(c.getTime());


                        Map<String, Object> message = new HashMap<>();
                        message.put("time", datetime);
                        message.put("text", "사진");
                        message.put("uri", downloadUri);
                        message.put("email", id);
                        message.put("type", 2);

                        database.collection("message").document(id).collection(id2)
                                .add(message);

                        message.replace("type", 3);

                        database.collection("message").document(id2).collection(id)
                                .add(message);

                        Map<String, Object> recent = new HashMap<>();

                        recent.put("text", "사진");


                        database.collection("message").document(id).collection("Last")
                                .document(id2).set(recent);

                        database.collection("message").document(id2).collection("Last")
                                .document(id).set(recent);


                        etText.setText("\0");

                        SendMessage.notification(
                                ChatActivity.this,
                                token2,
                                id,
                                name + "님이 파일을 전송하였습니다.",
                                name,
                                id2,
                                type2
                        );
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private final ActivityResultLauncher<Intent> selectFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null){
                       fileUri = result.getData().getData();
                       uploadFile(fileUri);
                    }
                }
    });

    private void uploadFile(Uri FileUri){

        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_DD_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = format.format(now);
        StorageReference storageReference = storage.getReference();

        progressBar.setVisibility(View.VISIBLE);

        storageReference.child(id).child(fileName).putFile(FileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child(id).child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();

                                Calendar c = Calendar.getInstance();
                                TimeZone tz;
                                tz = TimeZone.getTimeZone("Asia/Seoul");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                dateFormat.setTimeZone(tz);
                                String datetime = dateFormat.format(c.getTime());


                                Map<String, Object> message = new HashMap<>();
                                message.put("time", datetime);
                                message.put("text", name + "님이 파일을 전송하였습니다.");
                                message.put("email", id);
                                message.put("type", 2);

                                database.collection("message").document(id).collection(id2)
                                        .add(message);

                                message.replace("type", 3);

                                database.collection("message").document(id2).collection(id)
                                        .add(message);

                                Map<String, Object> recent = new HashMap<>();

                                recent.put("text", name + "님이 파일을 전송하였습니다.");


                                database.collection("message").document(id).collection("Last")
                                        .document(id2).set(recent);

                                database.collection("message").document(id2).collection("Last")
                                        .document(id).set(recent);


                                etText.setText("\0");

                                SendMessage.notification(
                                        ChatActivity.this,
                                        token2,
                                        id,
                                        name + "님이 파일을 전송하였습니다.",
                                        name,
                                        id2,
                                        type2
                                );
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                });
    }



    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}