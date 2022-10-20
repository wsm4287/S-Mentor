package com.example.s_mentor.apply;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s_mentor.R;
import com.example.s_mentor.notification.SendMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class ApplyListActivity extends AppCompatActivity {

    String id, id2, name;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    ApplyListAdapter applyListAdapter;
    ArrayList<User> userArrayList;
    String encodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applylist);
        Objects.requireNonNull(getSupportActionBar()).hide();

        InitApplyList();
        GetApplyList();
        ViewApplyList();
    }

    private void InitApplyList(){
        id = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");

        userArrayList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        applyListAdapter = new ApplyListAdapter(userArrayList);
        recyclerView.setAdapter(applyListAdapter);

    }

    private void GetApplyList(){
        database.collection("apply")
                .whereEqualTo("to", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = new User();
                            user.name = Objects.requireNonNull(document.getData().get("name")).toString();
                            user.major = Objects.requireNonNull(document.getData().get("major")).toString();
                            user.email = Objects.requireNonNull(document.getData().get("from")).toString();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            user.bitmap = DecodeImage(encodedImage);
                            user.docName = document.getId();
                            user.token = Objects.requireNonNull(document.getData().get("token")).toString();
                            userArrayList.add(user);
                            applyListAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void ViewApplyList(){
        applyListAdapter.setOnItemClickListener((holder, view, position) -> {
            User u = applyListAdapter.getUser(position);
            id2 = u.email;

            Calendar c = Calendar.getInstance();
            TimeZone tz;
            tz = TimeZone.getTimeZone("Asia/Seoul");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            dateFormat.setTimeZone(tz);
            String datetime = dateFormat.format(c.getTime());

            Drawable drawable = new BitmapDrawable(getResources(),u.bitmap);
            final ImageView iv = new ImageView(this);
            iv.setImageBitmap(u.bitmap);
            final TextView et = new TextView(this);
            et.setText(u.major);
            et.setTextSize(20);
            et.setPadding(40,0,40,0);
            HashMap<String, Object> m = new HashMap<>();
            AlertDialog.Builder ad = new AlertDialog.Builder(this, R.style.MyDialog)
                    .setView(iv)
                    .setIcon(drawable)
                    .setTitle(u.name)
                    .setMessage(id2).setView(et)
                    .setPositiveButton("거절", (dialog, which) -> {
                        database.collection("apply").document(u.docName)
                                .delete();
                        userArrayList.remove(position);
                        applyListAdapter.notifyDataSetChanged();
                    })
                    .setNeutralButton("수락", (dialog, which) -> {
                        SendMessage.notification(
                                this,
                                u.token,
                                id,
                                name + "님이 멘토링 신청을 수락하였습니다.",
                                name,
                                id2,
                                "XX"
                        );

                        database.collection("apply")
                                .whereEqualTo("from", id2)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            database.collection("apply").document(document.getId())
                                                    .delete();                                        }
                                    }
                                });
                        database.collection("apply")
                                .whereEqualTo("to", id)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            database.collection("apply").document(document.getId())
                                                    .delete();                                        }
                                    }
                                });


                        m.put("mentoring", id2);
                        m.put("mentoring_date", datetime);
                        database.collection("users").document(id)
                                .update(m);
                        m.replace("mentoring", id);
                        database.collection("users").document(id2)
                                .update(m);
                        userArrayList.remove(position);
                        applyListAdapter.notifyDataSetChanged();

                    });
            ad.show();
        });
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}