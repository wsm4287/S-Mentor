package com.example.s_mentor.ui.board;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.WriteActivity;
import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.databinding.FragmentBoardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class BoardFragment extends Fragment {

    private FragmentBoardBinding binding;
    View root;

    String id, name, type, encodedImage, text;
    EditText filter;
    Button setting, search, add;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    BoardAdapter boardAdapter;
    ArrayList<Board> boardList, filterList;
    Boolean mark = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBoardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        InitBoard();
        GetBoardList();
        SearchBoard();
        AddBoard();

        return root;
    }

    private void InitBoard(){

        id = requireActivity().getIntent().getStringExtra("email");
        name = requireActivity().getIntent().getStringExtra("name");
        type = requireActivity().getIntent().getStringExtra("type");

        filter = root.findViewById(R.id.board_search);
        setting = root.findViewById(R.id.btLogOut);
        search = root.findViewById(R.id.btSearch);
        add = root.findViewById(R.id.btAdd);

        if(type.equals("Mentor")){
            add.setVisibility(View.GONE);
        }

        boardList = new ArrayList<>();
        filterList = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        boardAdapter = new BoardAdapter(boardList);
        recyclerView.setAdapter(boardAdapter);

        setting.setOnClickListener(v -> {
            String[] array = {"프로필", "내가 작성한 게시글", "로그아웃"};

            if(type.equals("Mentor")) array[1] = "내가 답한 게시글";
            else if(!mark) array[1] = "전체 게시글";

            AlertDialog.Builder setting = new AlertDialog.Builder(requireContext())
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            Intent in = new Intent(root.getContext(), ProfileActivity.class);
                            in.putExtra("email", id);
                            startActivity(in);
                        }
                        if(which == 1){
                            if(type.equals("Mentee")){
                                if(mark){
                                    filterList.clear();
                                    for(int i=0; i<boardList.size(); i++){
                                        if(boardList.get(i).email.equals(id)){
                                            filterList.add(boardList.get(i));
                                        }
                                    }
                                    boardAdapter.filterList(filterList);
                                    mark = false;
                                }
                                else{
                                    boardAdapter.filterList(boardList);
                                    mark = true;
                                }
                            }
                        }
                        if(which == 2){
                            HashMap<String, Object> user = new HashMap<>();
                            user.put("token", "");
                            database.collection("users").document(id)
                                    .update(user).addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            Intent in = new Intent(root.getContext(), MainActivity.class);
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

    private void GetBoardList(){
        database.collection("board")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Board board = new Board();
                            board.name = Objects.requireNonNull(document.getData().get("name")).toString();
                            board.email = Objects.requireNonNull(document.getData().get("email")).toString();
                            board.time = Objects.requireNonNull(document.getData().get("time")).toString();
                            board.text = Objects.requireNonNull(document.getData().get("text")).toString();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            board.bitmap = DecodeImage(encodedImage);

                            boardList.add(board);
                        }
                        boardAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void AddBoard(){
        add.setOnClickListener(v -> {
            Intent in = new Intent(root.getContext(), WriteActivity.class);
            in.putExtra("email", id);
            in.putExtra("name", name);
            startActivity(in);
        });
    }

    private void SearchBoard(){
        search.setOnClickListener(v -> {
            text = filter.getText().toString().toLowerCase();
            if(text.equals("")){
                boardAdapter.filterList(boardList);
                return;
            }
            filterList.clear();
            for(int i=0; i<boardList.size(); i++){
                if(boardList.get(i).text.toLowerCase().contains(text)){
                    filterList.add(boardList.get(i));
                }
            }
            boardAdapter.filterList(filterList);
        });
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
