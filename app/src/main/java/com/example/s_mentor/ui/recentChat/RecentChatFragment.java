package com.example.s_mentor.ui.recentChat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.chat.ChatActivity;
import com.example.s_mentor.databinding.FragmentRecentchatBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RecentChatFragment extends Fragment {

    private FragmentRecentchatBinding binding;
    Button  setting;
    String id, type;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    RecentAdapter recentAdapter;
    ArrayList<RecentChat> recentArrayList;
    String otherUser;
    String encodedImage;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecentchatBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        InitRecentChat();
        GetRecentChat();
        ShortTouch();
        LongTouch();

        return root;
    }

    private void InitRecentChat(){
        id = requireActivity().getIntent().getStringExtra("email");
        type = requireActivity().getIntent().getStringExtra("type");

        setting = (Button) root.findViewById(R.id.btLogOut);

        recentArrayList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recentAdapter = new RecentAdapter(recentArrayList);
        recyclerView.setAdapter(recentAdapter);

        setting.setOnClickListener(v -> {
            String[] array = {"프로필", "b", "로그아웃"};

            AlertDialog.Builder setting = new AlertDialog.Builder(requireContext())
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            Intent in = new Intent(root.getContext(), ProfileActivity.class);
                            in.putExtra("email", id);
                            startActivity(in);
                        }
                        else if(which == 2){
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

    private void GetRecentChat(){
        database.collection("message").document(id).collection("Last")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document : task.getResult()){
                            RecentChat recentChat = new RecentChat();
                            recentChat.lastText = Objects.requireNonNull(document.getData().get("text")).toString();
                            recentChat.email = document.getId();
                            otherUser = recentChat.email;
                            database.collection("users").document(otherUser)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        recentChat.name = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString();
                                        encodedImage = Objects.requireNonNull(documentSnapshot.getData().get("image")).toString();
                                        recentChat.bitmap = DecodeImage(encodedImage);
                                        recentArrayList.add(recentChat);
                                        recentAdapter.notifyDataSetChanged();
                                    });
                        }
                    }
                });
    }

    private void ShortTouch(){
        recentAdapter.setOnItemClickListener((holder, view, position) -> {
            RecentChat recentChat = recentAdapter.getRecentChat(position);
            String id2 = recentChat.email;
            Intent in = new Intent(getContext(), ChatActivity.class);
            in.putExtra("email", id);
            in.putExtra("email2", id2);
            in.putExtra("type", type);
            startActivity(in);

        });
    }

    private void LongTouch(){
        recentAdapter.setOnItemLongClickListener((holder, view, position) -> {
            RecentChat recentChat = recentAdapter.getRecentChat(position);
            String id2 = recentChat.email;
            String name = recentChat.name;

            final TextView et = new TextView(getContext());
            AlertDialog.Builder ad = new AlertDialog.Builder(requireContext())
                    .setTitle(name)
                    .setMessage("메시지 기록을 삭제하시겠습니까?").setView(et)
                    .setPositiveButton("아니요", (dialog, which) -> {
                    })
                    .setNeutralButton("예", (dialog, which) -> {
                        database.collection("message").document(id).collection("Last").document(id2)
                                .delete();
                        database.collection("message").document(id).collection(id2)
                                .get().addOnCompleteListener(task -> {
                                    if(task.getResult().size() > 0){
                                        for(QueryDocumentSnapshot dc : task.getResult()){
                                            dc.getReference().delete();
                                        }
                                    }
                                });
                        recentArrayList.remove(position);
                        recentAdapter.notifyDataSetChanged();
                    });
            ad.show();
            return true;
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