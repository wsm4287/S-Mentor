package com.example.s_mentor.apply;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;

import java.util.ArrayList;

public class ApplyListAdapter extends RecyclerView.Adapter<ApplyListAdapter.ApplyViewHolder> implements OnItemClickListener {

    private final ArrayList<User> mDataSet;
    OnItemClickListener userListener;

    @Override
    public void onItemClick(ApplyViewHolder holder, View view, int position) {
        if(userListener != null){
            userListener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.userListener = listener;
    }


    static class ApplyViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView majorText;
        ImageView imageView;
        public ApplyViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            majorText = itemView.findViewById(R.id.majorText);
            imageView = itemView.findViewById(R.id.imageView);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener != null) {
                        listener.onItemClick(ApplyViewHolder.this, v, pos);
                    }
                }
            });
        }

    }

    public ApplyListAdapter(ArrayList<User> dataSet) {
        mDataSet = dataSet;
    }


    @NonNull
    @Override
    public ApplyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_view, viewGroup, false);

        return new ApplyViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplyViewHolder holder, int position) {
        User user = mDataSet.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.nameText.setText(user.getName());
        holder.majorText.setText(user.getMajor());
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
