package com.example.s_mentor.ui.recentchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecentChatFragment extends Fragment {

    private FragmentRecentchatBinding binding;
    Button  setting;
    String id, type;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    RecentAdapter recentAdapter;
    ArrayList<RecentChat> recentArrayList;
    String otherUser;
    String encodedImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        id = getActivity().getIntent().getStringExtra("email");
        type = getActivity().getIntent().getStringExtra("type");

        binding = FragmentRecentchatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setting = (Button) root.findViewById(R.id.btLogOut);

        recentArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recentAdapter = new RecentAdapter(recentArrayList);
        recyclerView.setAdapter(recentAdapter);

        database.collection("message").document(id).collection("Last")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                RecentChat recentChat = new RecentChat();
                                recentChat.lastText = document.getData().get("text").toString();
                                recentChat.email = document.getId();
                                otherUser = recentChat.email;
                                database.collection("users").document(otherUser)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                recentChat.name = documentSnapshot.getData().get("name").toString();
                                                encodedImage = documentSnapshot.getData().get("image").toString();
                                                recentChat.bitmap = DecodeImage(encodedImage);
                                                recentArrayList.add(recentChat);
                                                recentAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });

        recentAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RecentAdapter.RecentViewHolder holder, View view, int position) {
                RecentChat recentChat = recentAdapter.getRecentChat(position);
                String id2 = recentChat.email;
                Intent in = new Intent(getContext(), ChatActivity.class);
                in.putExtra("email1", id);
                in.putExtra("email2", id2);
                in.putExtra("type", type);
                startActivity(in);

            }
        });


        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String array[] = {"프로필", "b", "로그아웃"};

                AlertDialog.Builder setting = new AlertDialog.Builder(getContext())
                        .setItems(array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Intent in = new Intent(root.getContext(), ProfileActivity.class);
                                    in.putExtra("email", id);
                                    startActivity(in);
                                }
                                else if(which == 2){
                                    Intent in = new Intent(root.getContext(), MainActivity.class);
                                    in.putExtra("Logout", true);
                                    startActivity(in);
                                }

                            }
                        });

                AlertDialog alertDialog = setting.create();
                alertDialog.show();
            }
        });



        return root;
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