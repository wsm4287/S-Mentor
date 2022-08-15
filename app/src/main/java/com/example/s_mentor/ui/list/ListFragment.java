package com.example.s_mentor.ui.list;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.User;
import com.example.s_mentor.databinding.FragmentListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    private ListViewModel listViewModel;
    private FragmentListBinding binding;
    Button setting;
    String id;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    UserAdapter userAdapter;
    ArrayList<User> userArrayList;
    String encodedImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listViewModel =
                new ViewModelProvider(this).get(ListViewModel.class);

        id = getActivity().getIntent().getStringExtra("email");

        binding = FragmentListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setting = (Button) root.findViewById(R.id.btLogOut);

        userArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(userArrayList);
        recyclerView.setAdapter(userAdapter);

        database.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        if(document.getId().equals(id)) continue;
                        User user = new User();
                        user.name = document.getData().get("name").toString();
                        user.major = document.getData().get("major").toString();
                        user.email = document.getId();
                        encodedImage = document.getData().get("image").toString();
                        user.bitmap = DecodeImage(encodedImage);
                        userArrayList.add(user);
                        userAdapter.notifyDataSetChanged();
                    }
                }

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
                                if(which == 2){
                                    Intent in= new Intent(root.getContext(), MainActivity.class);
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