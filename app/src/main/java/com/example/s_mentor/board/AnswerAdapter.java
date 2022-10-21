package com.example.s_mentor.board;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.UserViewHolder> {

    private final ArrayList<Answer> mDataSet;
    Context context;
    FirebaseFirestore database = FirebaseFirestore.getInstance();


    static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView timeText;
        TextView answerText;
        ImageView imageView;
        Button menu;

        public UserViewHolder(View view){
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            timeText = itemView.findViewById(R.id.timeText);
            imageView = itemView.findViewById(R.id.imageView);
            answerText = itemView.findViewById(R.id.answerText);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    public AnswerAdapter(ArrayList<Answer> answer) {this.mDataSet = answer;}


    @NonNull
    @Override
    public AnswerAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer_view, parent, false);

        context = parent.getContext();

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerAdapter.UserViewHolder holder, int position) {
        Answer answer = mDataSet.get(position);

        holder.answerText.setText(answer.text);
        holder.imageView.setImageBitmap(answer.bitmap);
        holder.nameText.setText(answer.name);
        holder.timeText.setText(answer.time);

        if(answer.check) holder.menu.setVisibility(View.VISIBLE);
        else holder.menu.setVisibility(View.GONE);

        holder.menu.setOnClickListener(v -> {
            String[] array = {"삭제"};

            AlertDialog.Builder menu = new AlertDialog.Builder(context)
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            database.collection("board").document(answer.docId1)
                                    .collection("answer").document(answer.docId2)
                                    .delete();
                            mDataSet.remove(position);
                            holder.menu.setVisibility(View.GONE);
                        }
                    });

            AlertDialog alertDialog = menu.create();
            alertDialog.show();

        });

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
