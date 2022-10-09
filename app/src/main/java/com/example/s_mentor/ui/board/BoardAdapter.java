package com.example.s_mentor.ui.board;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.UserViewHolder> implements OnItemClickListener {

    private ArrayList<Board> mDataSet;
    OnItemClickListener boardListener;

    @Override
    public void onItemClick(UserViewHolder holder, View view, int position) {
        if(boardListener != null){
            boardListener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.boardListener = listener;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView timeText;
        TextView boardText;
        ImageView imageView;

        public UserViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            timeText = itemView.findViewById(R.id.timeText);
            imageView = itemView.findViewById(R.id.imageView);
            boardText = itemView.findViewById(R.id.boardText);

            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener != null) {
                        listener.onItemClick(BoardAdapter.UserViewHolder.this, v, pos);
                    }
                }
            });
        }

    }

    public BoardAdapter(ArrayList<Board> dataSet) {mDataSet = dataSet;}

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.board_view, viewGroup, false);


        return new UserViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardAdapter.UserViewHolder holder, int position) {
        Board board = mDataSet.get(position);

        holder.nameText.setText(board.getName());
        holder.boardText.setText(board.getText());
        holder.imageView.setImageBitmap(board.bitmap);
        holder.timeText.setText(board.getTime());

    }

    public void filterList(ArrayList<Board> filterList){
        mDataSet = filterList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


}
