package com.example.s_mentor.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.example.s_mentor.ImageActivity;
import com.example.s_mentor.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final ArrayList<Chat> message;
    String id;
    Context context;
    FirebaseStorage storage;

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        public TextView rightChat;
        public TextView leftChat;

        public ConstraintLayout rightFrame;
        public ImageView rightImage;
        public TextView rightFileName;
        public TextView rightFileChat;

        public ConstraintLayout leftFrame;
        public ImageView leftImage;
        public TextView leftFileName;
        public TextView leftFileChat;
        public Button btDown;

        public TextView timeView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            rightChat = itemView.findViewById(R.id.rightChat);
            leftChat = itemView.findViewById(R.id.leftChat);

            rightFrame = itemView.findViewById(R.id.rightFrame);
            rightImage = itemView.findViewById(R.id.rightImage);
            rightFileName = itemView.findViewById(R.id.rightFileName);
            rightFileChat = itemView.findViewById(R.id.rightFileChat);

            leftFrame = itemView.findViewById(R.id.leftFrame);
            leftImage = itemView.findViewById(R.id.leftImage);
            leftFileName = itemView.findViewById(R.id.leftFileName);
            leftFileChat = itemView.findViewById(R.id.leftFileChat);

            btDown = itemView.findViewById(R.id.btDown);
            timeView = itemView.findViewById(R.id.timeChat);
        }
    }

    public ChatAdapter(ArrayList<Chat> message, String id){
        this.message = message;
        this.id = id;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        context = parent.getContext();
        storage = FirebaseStorage.getInstance();

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
        else if(viewType == 2 || viewType == 4){
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        if(getItemViewType(position) == 0) holder.rightChat.setText(message.get(position).getText());
        else if(getItemViewType(position) == 1) holder.leftChat.setText(message.get(position).getText());
        else if(getItemViewType(position) == 2){
            holder.rightImage.setVisibility(View.VISIBLE);
            holder.rightFrame.setVisibility(View.GONE);
            String image = message.get(position).getUri();
            String fileName = message.get(position).getFileName();
            Picasso.get().load(image).resize(200,200).into(holder.rightImage);
            holder.rightImage.setOnClickListener(v -> {
                Intent in = new Intent(context, ImageActivity.class);
                in.putExtra("image", image);
                in.putExtra("fileName", fileName);
                context.startActivity(in);
            });
        }
        else if(getItemViewType(position) == 3){
            holder.leftImage.setVisibility(View.VISIBLE);
            holder.leftFrame.setVisibility(View.GONE);
            String image = message.get(position).getUri();
            String fileName = message.get(position).getFileName();
            Picasso.get().load(image).resize(200,200).into(holder.leftImage);
            holder.leftImage.setOnClickListener(v -> {
                Intent in = new Intent(context, ImageActivity.class);
                in.putExtra("image", image);
                in.putExtra("fileName", fileName);
                context.startActivity(in);
            });
        }
        else if(getItemViewType(position) == 4){
            String fileName = message.get(position).getFileName();
            String uri = message.get(position).getUri();

            holder.rightImage.setVisibility(View.GONE);
            holder.rightFrame.setVisibility(View.VISIBLE);
            holder.rightFileName.setText(fileName);
            holder.rightFileChat.setText(message.get(position).getText());

        }
        else if(getItemViewType(position) == 5){
            String fileName = message.get(position).getFileName();
            String uri = message.get(position).getUri();

            holder.leftImage.setVisibility(View.GONE);
            holder.leftFrame.setVisibility(View.VISIBLE);
            holder.leftFileName.setText(fileName);
            holder.leftFileChat.setText(message.get(position).getText());
            holder.btDown.setOnClickListener(v -> {
                StorageReference storageReference = storage.getReferenceFromUrl(uri);
                File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, fileName);
                storageReference.getFile(file).addOnSuccessListener(taskSnapshot -> Toast.makeText(context, "파일을 다운로드 하였습니다.", Toast.LENGTH_SHORT).show());

            });
        }

        holder.timeView.setText(message.get(position).getDatetime());

    }

    @Override
    public int getItemCount() {
        return message.size();
    }


    public int getItemViewType(int position) {
        return Integer.parseInt(message.get(position).type);
    }


}
