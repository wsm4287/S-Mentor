package com.example.s_mentor.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;


import com.example.s_mentor.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final ArrayList<Chat> message;
    String id;
    Context context;
    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        public TextView textView1;
        public TextView textView2;
        public ImageView imageView1;
        public ImageView imageView2;
        public TextView timeView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.rightChat);
            textView2 = itemView.findViewById(R.id.leftChat);
            imageView1 = itemView.findViewById(R.id.rightFile);
            imageView2 = itemView.findViewById(R.id.leftFile);
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
        context = parent.getContext();

        if(viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
            return new ChatViewHolder(view);
        }
        else if(viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view, parent, false);
            return new ChatViewHolder(view);
        }
        else if(viewType == 2){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_file_view, parent, false);
            return new ChatViewHolder(view);
        }
        else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_file_view, parent, false);
            return new ChatViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        if(getItemViewType(position) == 0) holder.textView1.setText(message.get(position).getText());
        else if(getItemViewType(position) == 1) holder.textView2.setText(message.get(position).getText());
        else if(getItemViewType(position) == 2){
            Picasso.get().load(message.get(position).getUpImage()).resize(200,200).into(holder.imageView1);
            //Toast.makeText(context, message.get(position).getUpImage(), Toast.LENGTH_SHORT).show();
            //holder.imageView1.setImageURI(Uri.parse(message.get(position).getUpImage()));
        }
        else if(getItemViewType(position) == 3){
            //Toast.makeText(context, message.get(position).getUpImage(), Toast.LENGTH_SHORT).show();
            //holder.imageView2.setImageURI(Uri.parse(message.get(position).getUpImage()));
            Picasso.get().load(message.get(position).getUpImage()).resize(200,200).into(holder.imageView2);
        }
        holder.timeView.setText(message.get(position).getDatetime());

    }

    @Override
    public int getItemCount() {
        return message.size();
    }


    public int getItemViewType(int position) {
        return Integer.valueOf(message.get(position).type);
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
