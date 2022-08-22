package com.example.s_mentor.ui.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    Button  setting;
    String id;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        id = getActivity().getIntent().getStringExtra("email");

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setting = (Button) root.findViewById(R.id.btLogOut);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}