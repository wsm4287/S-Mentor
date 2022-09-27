package com.example.s_mentor.ui.recentChat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;

import java.util.ArrayList;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder> implements OnItemClickListener {

    private final ArrayList<RecentChat> lastChat;
    OnItemClickListener reListener;
    OnItemLongClickListener longListener;

    @Override
    public void onItemClick(RecentViewHolder holder, View view, int position) {
        if(reListener != null){
            reListener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.reListener = listener;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener listener) { this.longListener = listener;}

    static class RecentViewHolder extends RecyclerView.ViewHolder{
        TextView nameText, lastText;
        ImageView imageView;
        public RecentViewHolder(View view, OnItemClickListener listener, OnItemLongClickListener Listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            lastText = itemView.findViewById(R.id.lastText);
            imageView = itemView.findViewById(R.id.imageView);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener != null) {
                        listener.onItemClick(RecentViewHolder.this, v, pos);
                    }
                }
            });

            view.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (Listener != null) {
                        Listener.onItemLongClick(RecentViewHolder.this, v, pos);
                    }
                }
                return false;
            });
        }


    }

    public RecentAdapter(ArrayList<RecentChat> lastChat) {
        this.lastChat = lastChat;
    }


    @NonNull
    @Override
    public RecentAdapter.RecentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recentchat_view, viewGroup, false);

        return new RecentViewHolder(view, reListener, longListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentAdapter.RecentViewHolder holder, int position) {
        RecentChat recentChat = lastChat.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.nameText.setText(recentChat.getName());
        holder.lastText.setText(recentChat.getLastText());
        holder.imageView.setImageBitmap(recentChat.getBitmap());
    }

    @Override
    public int getItemCount() {
        return lastChat.size();
    }

    public RecentChat getRecentChat(int position){
        return lastChat.get(position);
    }


}
