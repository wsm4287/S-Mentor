package com.example.s_mentor.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.HomeActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.notification.SendMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    TextView opNm;
    ImageView chBk, opPh;
    Button btSend, btFile, btImage;
    EditText etText;
    String id, id2, token, token2, name, name2, type, type2, encodedImage, major, mentoring;
    Bitmap bitmap;
    FirebaseFirestore database;
    private RecyclerView.LayoutManager layoutManager;
    ChatAdapter chatAdapter;
    ArrayList<Chat> chatArrayList;
    boolean check;
    boolean match = false;
    Uri fileUri, imageUri;
    FirebaseStorage storage;
    ProgressBar progressBar;
    int limit = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ChatInitial();
        LoadUserData();
        ViewChat();
        SendChat();
        ApplyMentoring();

    }

    private void ChatInitial(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        opNm = findViewById(R.id.opName);
        chBk = findViewById(R.id.chBack);
        opPh = findViewById(R.id.opPhoto);
        etText = findViewById(R.id.etText);
        btSend = findViewById(R.id.btSend);
        btFile = findViewById(R.id.btFile);
        btImage = findViewById(R.id.btImage);
        progressBar = findViewById(R.id.progress_file);

        id = getIntent().getStringExtra("email");
        id2 = getIntent().getStringExtra("email2");
        type = getIntent().getStringExtra("type");
        chatArrayList = new ArrayList<>();
        check = true;
        opNm.setText(id2);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(chatArrayList, id);
        recyclerView.setAdapter(chatAdapter);

        chBk.setOnClickListener(v -> {
            Toast.makeText(ChatActivity.this, "Finish chat", Toast.LENGTH_LONG).show();
            Intent in = new Intent(ChatActivity.this, HomeActivity.class);
            in.putExtra("email", id);
            in.putExtra("type", type);
            startActivity(in);
        });

        btFile.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                selectFile.launch(intent);
            }
            else{
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
            }

        });

        btImage.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                selectImage.launch(intent);
            }
            else{
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
            }
        });
    }


    private void LoadUserData(){
        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    name = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString();
                    encodedImage = Objects.requireNonNull(documentSnapshot.getData().get("image")).toString();
                    major = Objects.requireNonNull(documentSnapshot.getData().get("major")).toString();
                    token = Objects.requireNonNull(documentSnapshot.getData().get("token")).toString();
                    mentoring = Objects.requireNonNull(documentSnapshot.getData().get("mentoring")) + "";
                    if(!(mentoring.equals(" "))) check = false;
                    match = mentoring.equals(id2);
                });


        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    name2 = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString();
                    opNm.setText(name2);
                    bitmap = DecodeImage(Objects.requireNonNull(documentSnapshot.getData().get("image")).toString());
                    opPh.setImageBitmap(bitmap);
                    token2 = Objects.requireNonNull(documentSnapshot.getData().get("token")).toString();
                    type2 = Objects.requireNonNull(documentSnapshot.getData().get("type")).toString();
                    if(!(Objects.requireNonNull(documentSnapshot.getData().get("mentoring")).toString().equals(" "))) check = false;
                });
    }

    private void ViewChat(){

        database.collection("message").document(id).collection(id2)
                .orderBy("time")
                .limitToLast(50)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatArrayList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chat chat = new Chat();
                            String t = Objects.requireNonNull(document.getData().get("time")).toString();
                            chat.datetime = t.substring(5,16);
                            chat.text = Objects.requireNonNull(document.getData().get("text")).toString();
                            chat.email = Objects.requireNonNull(document.getData().get("email")).toString();
                            chat.type = Objects.requireNonNull(document.getData().get("type")).toString();

                            if(!(chat.type.equals("0") || chat.type.equals("1"))){
                                chat.uri = Objects.requireNonNull(document.getData().get("uri")).toString();
                                chat.fileName = Objects.requireNonNull(document.getData().get("fileName")).toString();
                            }
                            chatArrayList.add(chat);
                            limit--;
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    layoutManager.scrollToPosition(chatArrayList.size()-1);
                });



        database.collection("message").document(id).collection(id2)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    assert value != null;
                    for (QueryDocumentSnapshot document : value) {
                        Chat chat = new Chat();
                        String t = Objects.requireNonNull(document.getData().get("time")).toString();
                        chat.datetime = t.substring(5,16);
                        chat.text = Objects.requireNonNull(document.getData().get("text")).toString();
                        chat.email = Objects.requireNonNull(document.getData().get("email")).toString();
                        chat.type = Objects.requireNonNull(document.getData().get("type")).toString();

                        if(!(chat.type.equals("0") || chat.type.equals("1"))){
                            chat.uri = Objects.requireNonNull(document.getData().get("uri")).toString();
                            chat.fileName = Objects.requireNonNull(document.getData().get("fileName")).toString();
                        }
                        chatArrayList.add(chat);
                        limit--;
                    }
                    chatAdapter.notifyDataSetChanged();
                    layoutManager.scrollToPosition(chatArrayList.size()-1);
                });


    }


    private void SendChat(){

        btSend.setOnClickListener(v -> {
            if(!match && limit<0){
                Toast.makeText(this, "사용 가능한 메시지를 모두 사용하셨습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String stText = etText.getText().toString();

            Calendar c = Calendar.getInstance();
            TimeZone tz;
            tz = TimeZone.getTimeZone("Asia/Seoul");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
            dateFormat.setTimeZone(tz);
            String datetime = dateFormat.format(c.getTime());


            Map<String, Object> message = new HashMap<>();
            message.put("time", datetime);
            message.put("text", stText);
            message.put("email", id);
            message.put("type", 0);

            if(!match) Toast.makeText(this, "사용할 수 있는 메시지가 " + limit+ "만큼 남았습니다.", Toast.LENGTH_SHORT).show();

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

        });
    }


    private void ApplyMentoring(){

        opPh.setOnClickListener(v -> {
            if(check) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                final ImageView iv = new ImageView(ChatActivity.this);
                iv.setImageBitmap(bitmap);
                final TextView et = new TextView(ChatActivity.this);
                et.setText("멘토링을 신청하시겠습니까?");
                AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this, R.style.MyDialog)
                        .setView(iv)
                        .setIcon(drawable)
                        .setTitle(name2)
                        .setMessage(id2).setView(et)
                        .setPositiveButton("종료", (dialog, which) -> {
                        })
                        .setNeutralButton("신청", (dialog, which) -> {
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
                        });

                ad.show();
            }
            else{
                Toast.makeText(ChatActivity.this,"이미 멘토링 상대가 있습니다.", Toast.LENGTH_SHORT).show();
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

        if(!match && limit<0){
            Toast.makeText(this, "사용 가능한 메시지를 모두 사용하셨습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = getFileName(imageUri);
        StorageReference storageReference = storage.getReference();

        if(fileName == null){
            Toast.makeText(this,"파일 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.PNG,100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();


        progressBar.setVisibility(View.VISIBLE);

        storageReference.child(id).child(fileName).putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful()){
                        if(uriTask.isSuccessful()) break;
                    }
                    String downloadUri = uriTask.getResult().toString();

                    Calendar c = Calendar.getInstance();
                    TimeZone tz;
                    tz = TimeZone.getTimeZone("Asia/Seoul");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
                    dateFormat.setTimeZone(tz);
                    String datetime = dateFormat.format(c.getTime());


                    Map<String, Object> message = new HashMap<>();
                    message.put("time", datetime);
                    message.put("text", "사진");
                    message.put("uri", downloadUri);
                    message.put("email", id);
                    message.put("fileName", fileName);
                    message.put("type", 2);

                    if(!match) Toast.makeText(this, "사용할 수 있는 메시지가 " + limit+ "만큼 남았습니다.", Toast.LENGTH_SHORT).show();

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
        if(!match && limit<0){
            Toast.makeText(this, "사용 가능한 메시지를 모두 사용하셨습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = getFileName(FileUri);
        StorageReference storageReference = storage.getReference();

        progressBar.setVisibility(View.VISIBLE);

        if(fileName == null){
            Toast.makeText(this,"파일 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        storageReference.child(id).child(fileName).putFile(FileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.child(id).child(fileName).getDownloadUrl().addOnSuccessListener(uri -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful()){
                        if(uriTask.isSuccessful()) break;
                    }
                    String downloadUri = uriTask.getResult().toString();

                    Calendar c = Calendar.getInstance();
                    TimeZone tz;
                    tz = TimeZone.getTimeZone("Asia/Seoul");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
                    dateFormat.setTimeZone(tz);
                    String datetime = dateFormat.format(c.getTime());

                    Map<String, Object> message = new HashMap<>();
                    message.put("time", datetime);
                    message.put("text", "파일을 전송하였습니다.");
                    message.put("uri", downloadUri);
                    message.put("email", id);
                    message.put("fileName", fileName);
                    message.put("type", 4);

                    if(!match) Toast.makeText(this, "사용할 수 있는 메시지가 " + limit+ "만큼 남았습니다.", Toast.LENGTH_SHORT).show();

                    database.collection("message").document(id).collection(id2)
                            .add(message);

                    message.replace("type", 5);
                    message.replace("text", name + "님이 파일을 전송하였습니다.");

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
                })).addOnProgressListener(snapshot -> {

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


    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}