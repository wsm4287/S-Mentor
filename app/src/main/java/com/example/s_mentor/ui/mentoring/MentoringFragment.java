package com.example.s_mentor.ui.mentoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.databinding.FragmentMentoringBinding;
import com.example.s_mentor.notification.SendMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MentoringFragment extends Fragment {

    private FragmentMentoringBinding binding;
    View root;

    String id, id2, name;
    Button setting;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    MentoringAdapter mentoringAdapter;
    ArrayList<User> userArrayList;
    String encodedImage;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMentoringBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        InitMentoringList();
        GetMentoringList();
        ViewMentoringList();

        return root;
    }

    private void InitMentoringList(){
        id = requireActivity().getIntent().getStringExtra("email");
        name = requireActivity().getIntent().getStringExtra("name");

        setting = (Button) root.findViewById(R.id.btLogOut);

        userArrayList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mentoringAdapter = new MentoringAdapter(userArrayList);
        recyclerView.setAdapter(mentoringAdapter);

        setting.setOnClickListener(v -> {
            String[] array = {"프로필", "b", "로그아웃"};

            AlertDialog.Builder setting = new AlertDialog.Builder(requireContext())
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            Intent in = new Intent(root.getContext(), ProfileActivity.class);
                            in.putExtra("email", id);
                            startActivity(in);
                        }
                        if(which == 2){
                            HashMap<String, Object> user = new HashMap<>();
                            user.put("token", "");
                            database.collection("users").document(id)
                                    .update(user).addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            Intent in= new Intent(root.getContext(), MainActivity.class);
                                            in.putExtra("Logout", true);
                                            startActivity(in);
                                        }
                                    });

                        }
                    });

            AlertDialog alertDialog = setting.create();
            alertDialog.show();
        });
    }

    private void GetMentoringList(){
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
                            mentoringAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void ViewMentoringList(){
        mentoringAdapter.setOnItemClickListener((holder, view, position) -> {
            User u = mentoringAdapter.getUser(position);
            id2 = u.email;

            Drawable drawable = new BitmapDrawable(getResources(),u.bitmap);
            final ImageView iv = new ImageView(getContext());
            iv.setImageBitmap(u.bitmap);
            final TextView et = new TextView(getContext());
            et.setText(u.major);
            HashMap<String, Object> m = new HashMap<>();
            AlertDialog.Builder ad = new AlertDialog.Builder(requireContext())
                    .setView(iv)
                    .setIcon(drawable)
                    .setTitle(u.name)
                    .setMessage(id2).setView(et)
                    .setPositiveButton("거절", (dialog, which) -> {
                        database.collection("apply").document(u.docName)
                                .delete();
                        userArrayList.remove(position);
                        mentoringAdapter.notifyDataSetChanged();
                    })
                    .setNeutralButton("수락", (dialog, which) -> {
                        SendMessage.notification(
                                getContext(),
                                u.token,
                                id,
                                name + "님이 멘토링 신청을 수락하였습니다.",
                                name,
                                id2,
                                "XX"
                        );
                        database.collection("apply").document(u.docName)
                                .delete();
                        m.put("mentoring", id2);
                        database.collection("users").document(id)
                                .update(m);
                        m.replace("mentoring", id);
                        database.collection("users").document(id2)
                                .update(m);
                        userArrayList.remove(position);
                        mentoringAdapter.notifyDataSetChanged();

                    });
            ad.show();
        });
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}