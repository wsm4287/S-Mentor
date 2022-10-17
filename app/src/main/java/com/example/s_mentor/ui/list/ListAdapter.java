package com.example.s_mentor.ui.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.UserViewHolder> implements OnItemClickListener {

    private ArrayList<User> mDataSet;
    OnItemClickListener userListener;
    FirebaseFirestore database;
    String id, encodedImage, type, name, major, phone, introduction, token, mentoring;
    CollectionReference collection;
    Context context;

    Drawable drawable, drawable2;

    final String[] items = {"취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};


    @Override
    public void onItemClick(UserViewHolder holder, View view, int position) {
        if(userListener != null){
            userListener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.userListener = listener;
    }


    static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView majorText;
        CircleImageView imageView;
        //GridView gridView;
        Button favoriteView;

        public UserViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            majorText = itemView.findViewById(R.id.majorText);
            imageView = itemView.findViewById(R.id.imageView);
            //gridView = itemView.findViewById(R.id.user_list);
            favoriteView = itemView.findViewById(R.id.bookMark);

            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener != null) {
                        listener.onItemClick(UserViewHolder.this, v, pos);
                    }
                }
            });
        }

    }

    public ListAdapter(ArrayList<User> dataSet) {
        mDataSet = dataSet;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_view, viewGroup, false);

        context = viewGroup.getContext();

        id = ((Activity) context).getIntent().getStringExtra("email");

        database = FirebaseFirestore.getInstance();

        collection = database.collection("users").document(id).collection("favorite");


        drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24);
        drawable2 = ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_border_24);

        return new UserViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.UserViewHolder holder, int position) {
        User user = mDataSet.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.nameText.setText(user.getName());
        holder.majorText.setText(user.getMajor());
        holder.imageView.setImageBitmap(user.getBitmap());


        collection.document(user.getEmail())
                .addSnapshotListener((value, error) -> {
                    assert value != null;
                    if((""+value.getData()).equals("null")){
                        holder.favoriteView.setBackground(drawable2);
                    }
                    else if((""+ Objects.requireNonNull(value.getData()).get("mark")).equals("1")){
                        holder.favoriteView.setBackground(drawable);
                    }

                });

        holder.favoriteView.setOnClickListener(v -> collection.document(user.getEmail()).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        HashMap<String, Object> mark = new HashMap<>();
                        if(holder.favoriteView.getBackground().equals(drawable)){
                            collection.document(user.getEmail()).delete();
                            holder.favoriteView.setBackground(drawable2);
                        }
                        else{
                            database.collection("users").document(user.getEmail())
                                    .get().addOnCompleteListener(task1 -> {
                                        name = Objects.requireNonNull(Objects.requireNonNull(task1.getResult().getData()).get("name")).toString();
                                        major = Objects.requireNonNull(task1.getResult().getData().get("major")).toString();
                                        encodedImage = Objects.requireNonNull(task1.getResult().getData().get("image")).toString();
                                        introduction = Objects.requireNonNull(task1.getResult().getData().get("introduction")).toString();
                                        phone = Objects.requireNonNull(task1.getResult().getData().get("phone")).toString();
                                        token = Objects.requireNonNull(task1.getResult().getData().get("token")).toString();
                                        mentoring = Objects.requireNonNull(task1.getResult().getData().get("mentoring")).toString();
                                        type = Objects.requireNonNull(task1.getResult().getData().get("type")).toString();

                                        String t;
                                        for(int i=0; i<7; i++){
                                            t = Integer.toString(i);
                                            mark.put(t, Objects.requireNonNull(task1.getResult().getData().get(Integer.toString(i))).toString());
                                        }
                                        mark.put("name", name);
                                        mark.put("major", major);
                                        mark.put("image", encodedImage);
                                        mark.put("introduction", introduction);
                                        mark.put("phone", phone);
                                        mark.put("mentoring", mentoring);
                                        mark.put("type", type);
                                        mark.put("mark", 1);
                                        mark.put("token", token);
                                        holder.favoriteView.setBackground(drawable);
                                        collection.document(user.getEmail()).set(mark);
                                    });

                        }
                    }
                }));


        /*int count = user.getField().size();

        String[] x = new String[count];

        for(int i=0; i<count; i++){
            x[i] = items[user.field.get(i)];
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.field_view, x);

        holder.gridView.setAdapter(adapter);*/


    }

    @Override
    public int getItemCount() {
            return mDataSet.size();
        }


    public User getUser(int position){
        return mDataSet.get(position);
    }

    public void filterList(ArrayList<User> filterList){
        mDataSet = filterList;
        notifyDataSetChanged();
    }

    public ArrayList<User> getList(){
        return mDataSet;
    }

}
