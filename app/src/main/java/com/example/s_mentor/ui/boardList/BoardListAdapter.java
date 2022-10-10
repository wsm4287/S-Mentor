package com.example.s_mentor.ui.boardList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;

import java.util.ArrayList;

public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.UserViewHolder> implements OnItemClickListener {

    private ArrayList<BoardList> mDataSet;
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
        TextView titleText;
        ImageView imageView;

        public UserViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            timeText = itemView.findViewById(R.id.timeText);
            imageView = itemView.findViewById(R.id.imageView);
            boardText = itemView.findViewById(R.id.boardText);
            titleText = itemView.findViewById(R.id.titleText);

            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener != null) {
                        listener.onItemClick(BoardListAdapter.UserViewHolder.this, v, pos);
                    }
                }
            });
        }

    }

    public BoardListAdapter(ArrayList<BoardList> dataSet) {mDataSet = dataSet;}

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.boardlist_view, viewGroup, false);


        return new UserViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardListAdapter.UserViewHolder holder, int position) {
        BoardList boardList = mDataSet.get(position);

        holder.nameText.setText(boardList.getName());
        holder.boardText.setText(boardList.getText());
        holder.imageView.setImageBitmap(boardList.bitmap);
        holder.timeText.setText(boardList.getTime());
        holder.titleText.setText(boardList.getTitle());

    }

    public void filterList(ArrayList<BoardList> filterList){
        mDataSet = filterList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public BoardList getBoard(int position) { return mDataSet.get(position); }


}
