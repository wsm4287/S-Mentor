package com.example.s_mentor.ui.boardList;

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

import com.example.s_mentor.board.BoardActivity;
import com.example.s_mentor.board.BoardAddActivity;
import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.databinding.FragmentBoardlistBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class BoardListFragment extends Fragment {

    private FragmentBoardlistBinding binding;
    View root;

    String id, name, type, encodedImage, text, title, id2;
    EditText filter;
    Button setting, search, add;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    BoardListAdapter boardListAdapter;
    ArrayList<BoardList> boardList, filterList;
    Boolean mark = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBoardlistBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        InitBoard();
        GetBoardList();
        SearchBoard();
        AddBoard();
        SelectBoard();

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

        boardListAdapter = new BoardListAdapter(boardList);
        recyclerView.setAdapter(boardListAdapter);

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
                                    boardListAdapter.filterList(filterList);
                                    mark = false;
                                }
                                else{
                                    boardListAdapter.filterList(boardList);
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
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        boardList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BoardList boardList = new BoardList();
                            boardList.name = Objects.requireNonNull(document.getData().get("name")).toString();
                            boardList.email = Objects.requireNonNull(document.getData().get("email")).toString();
                            boardList.time = Objects.requireNonNull(document.getData().get("time")).toString();
                            boardList.text = Objects.requireNonNull(document.getData().get("text")).toString();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            boardList.bitmap = DecodeImage(encodedImage);
                            boardList.title = Objects.requireNonNull(document.getData().get("title")).toString();

                            this.boardList.add(boardList);
                        }
                        boardListAdapter.notifyDataSetChanged();
                    }
                });

        database.collection("board")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    assert value != null;
                    for (QueryDocumentSnapshot document : value) {
                        BoardList boardList = new BoardList();
                        boardList.name = Objects.requireNonNull(document.getData().get("name")).toString();
                        boardList.email = Objects.requireNonNull(document.getData().get("email")).toString();
                        boardList.time = Objects.requireNonNull(document.getData().get("time")).toString();
                        boardList.text = Objects.requireNonNull(document.getData().get("text")).toString();
                        encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                        boardList.bitmap = DecodeImage(encodedImage);
                        boardList.title = Objects.requireNonNull(document.getData().get("title")).toString();

                        this.boardList.add(boardList);
                    }
                    boardListAdapter.notifyDataSetChanged();
                });
    }

    private void AddBoard(){
        add.setOnClickListener(v -> {
            Intent in = new Intent(root.getContext(), BoardAddActivity.class);
            in.putExtra("email", id);
            in.putExtra("name", name);
            startActivity(in);
        });
    }

    private void SearchBoard(){
        search.setOnClickListener(v -> {
            text = filter.getText().toString().toLowerCase();
            if(text.equals("")){
                boardListAdapter.filterList(boardList);
                return;
            }
            filterList.clear();
            for(int i=0; i<boardList.size(); i++){
                if(boardList.get(i).title.toLowerCase().contains(text) || boardList.get(i).text.toLowerCase().contains(text)){
                    filterList.add(boardList.get(i));
                }
            }
            boardListAdapter.filterList(filterList);
        });
    }

    private void SelectBoard(){
        boardListAdapter.setOnItemClickListener(((holder, view, position) -> {
            id2 = boardListAdapter.getBoard(position).email;
            title = boardListAdapter.getBoard(position).title;
            Intent in = new Intent(getActivity(), BoardActivity.class);
            in.putExtra("email1", id);
            in.putExtra("email2", id2);
            in.putExtra("title", title);
            in.putExtra("name", name);
            startActivity(in);
        }));
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
