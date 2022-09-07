package com.example.s_mentor.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.s_mentor.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final ArrayList<Chat> message;
    String id;

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public TextView timeView;

        public ChatViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.tvChat);
            timeView = itemView.findViewById(R.id.timeChat);
        }
    }

    public ChatAdapter(ArrayList<Chat> message, String id){
        this.message = message;
        this.id = id;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view, parent, false);
        }

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.textView.setText(message.get(position).getText());
        holder.timeView.setText(message.get(position).getDatetime());

    }

    @Override
    public int getItemCount() {
        return message.size();
    }


    public int getItemViewType(int position) {
        if(message.get(position).email.equals(id)) return 0;

        return 1;
    }



}
