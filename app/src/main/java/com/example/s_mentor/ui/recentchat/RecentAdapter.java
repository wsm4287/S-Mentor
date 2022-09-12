package com.example.s_mentor.ui.recentchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

    public boolean onItemLongClick(RecentViewHolder holder, View view, int position){
        if(longListener != null){
            longListener.onItemLongClick(holder, view, position);
        }
        return true;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) { this.longListener = listener;}

    class RecentViewHolder extends RecyclerView.ViewHolder{
        TextView nameText, lastText;
        ImageView imageView;
        public RecentViewHolder(View view, OnItemClickListener listener, OnItemLongClickListener Listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            lastText = itemView.findViewById(R.id.lastText);
            imageView = itemView.findViewById(R.id.imageView);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onItemClick(RecentAdapter.RecentViewHolder.this, v, pos);
                        }
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (Listener != null) {
                            Listener.onItemLongClick(RecentAdapter.RecentViewHolder.this, v, pos);
                        }
                    }
                    return false;
                }
            });
        }


    }

    public RecentAdapter(ArrayList<RecentChat> lastChat) {
        this.lastChat = lastChat;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public RecentAdapter.RecentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recentchat_view, viewGroup, false);

        return new RecentAdapter.RecentViewHolder(view,reListener,longListener);
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
