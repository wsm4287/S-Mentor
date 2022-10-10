package com.example.s_mentor.board;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;

import java.util.ArrayList;


public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.UserViewHolder> {

    private final ArrayList<Answer> mDataSet;


    static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView timeText;
        TextView answerText;
        ImageView imageView;

        public UserViewHolder(View view){
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            timeText = itemView.findViewById(R.id.timeText);
            imageView = itemView.findViewById(R.id.imageView);
            answerText = itemView.findViewById(R.id.answerText);
        }
    }

    public AnswerAdapter(ArrayList<Answer> answer) {this.mDataSet = answer;}


    @NonNull
    @Override
    public AnswerAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer_view, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerAdapter.UserViewHolder holder, int position) {
        Answer answer = mDataSet.get(position);

        holder.answerText.setText(answer.text);
        holder.imageView.setImageBitmap(answer.bitmap);
        holder.nameText.setText(answer.name);
        holder.timeText.setText(answer.time);

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
