package com.example.s_mentor.ui.list;

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

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.UserViewHolder> implements OnItemClickListener {

    private final ArrayList<User> mDataSet;
    OnItemClickListener userListener;

    @Override
    public void onItemClick(UserViewHolder holder, View view, int position) {
        if(userListener != null){
            userListener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.userListener = listener;
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView emailText;
        ImageView imageView;
        public UserViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            imageView = itemView.findViewById(R.id.imageView);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onItemClick(UserViewHolder.this, v, pos);
                        }
                    }
                }
            });
        }

    }

    public ListAdapter(ArrayList<User> dataSet) {
        mDataSet = dataSet;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_view, viewGroup, false);

        return new UserViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.UserViewHolder holder, int position) {
        User user = mDataSet.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.nameText.setText(user.getName());
        holder.emailText.setText(user.getEmail());
        holder.imageView.setImageBitmap(user.getBitmap());
    }

    @Override
    public int getItemCount() {
            return mDataSet.size();
        }

    public User getUser(int position){
        return mDataSet.get(position);
    }


}
