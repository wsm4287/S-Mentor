package com.example.s_mentor.ui.list;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.HomeActivity;
import com.example.s_mentor.InformActivity;
import com.example.s_mentor.MainActivity;
import com.example.s_mentor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.UserViewHolder> implements OnItemClickListener {

    private final ArrayList<User> mDataSet;
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


    class UserViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView majorText;
        ImageView imageView;
        GridView gridView;
        Button favoriteView;

        public UserViewHolder(View view, OnItemClickListener listener) {
            super(view);
            nameText = itemView.findViewById(R.id.nameText);
            majorText = itemView.findViewById(R.id.majorText);
            imageView = itemView.findViewById(R.id.imageView);
            gridView = (GridView) itemView.findViewById(R.id.user_list);
            favoriteView = itemView.findViewById(R.id.bookMark);

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

        context = viewGroup.getContext();

        id = ((Activity) context).getIntent().getStringExtra("email");

        database = FirebaseFirestore.getInstance();

        collection = database.collection("users").document(id).collection("favorite");


        drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_24);
        drawable2 = ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_border_24);

        return new UserViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.UserViewHolder holder, int position) {
        User user = mDataSet.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.nameText.setText(user.getName());
        holder.majorText.setText(user.getMajor());
        holder.imageView.setImageBitmap(user.getBitmap());


 /*       collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        if((""+documentSnapshot.getData().get(user.getEmail())).equals("1")){
                            holder.favoriteView.setBackground(drawable);
                        }
                    }
                }
            }
        });

        holder.favoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot documentSnapshot : task.getResult()){

                                if((""+documentSnapshot.getData().get(user.getEmail())).equals("1")){
                                    documentSnapshot.getReference().delete();
                                }
                                else{

                                    documentSnapshot.getReference().delete();
                                }
                            }
                            HashMap<String, Integer> mark = new HashMap<>();
                            if(holder.favoriteView.getBackground().equals(drawable)){
                                holder.favoriteView.setBackground(drawable2);
                                mark.put(user.getEmail(), 0);
                                collection.add(mark);
                            }
                            else{
                                holder.favoriteView.setBackground(drawable);
                                mark.put(user.getEmail(), 1);
                                collection.add(mark);
                            }


                        }

                    }
                });
            }
        });

*/


        collection.document(user.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if((""+documentSnapshot.getData()).equals("null")){
                            holder.favoriteView.setBackground(drawable2);
                            return;
                        }

                        else if((""+documentSnapshot.getData().get("mark")).equals("1")){
                            holder.favoriteView.setBackground(drawable);
                            return;
                        }
                    }
                });

        holder.favoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection.document(user.getEmail()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    HashMap<String, Object> mark = new HashMap<>();
                                    if(holder.favoriteView.getBackground().equals(drawable)){
                                        collection.document(user.getEmail()).delete();
                                        mark.put("mark", 0);
                                        holder.favoriteView.setBackground(drawable2);
                                        collection.document(user.getEmail()).set(mark);
                                    }
                                    else{
                                        database.collection("users").document(user.getEmail())
                                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                Toast.makeText(context, task.getResult().getData().get("name").toString(),Toast.LENGTH_SHORT).show();

                                                name = task.getResult().getData().get("name").toString();
                                                major = task.getResult().getData().get("major").toString();
                                                encodedImage = task.getResult().getData().get("image").toString();
                                                introduction = task.getResult().getData().get("introduction").toString();
                                                phone = task.getResult().getData().get("phone").toString();
                                                token = task.getResult().getData().get("token").toString();
                                                mentoring = task.getResult().getData().get("mentoring").toString();
                                                type = task.getResult().getData().get("type").toString();

                                                String t;
                                                for(int i=0; i<7; i++){
                                                    t = Integer.toString(i);
                                                    mark.put(t, task.getResult().getData().get(Integer.toString(i)).toString());
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
                                            }
                                        });

                                    }
                                }
                            }
                        });
            }
        });




        int count = user.getField().size();

        String[] x = new String[count];

        for(int i=0; i<count; i++){
            x[i] = items[user.field.get(i)];
        }

        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.field_view, x);

        holder.gridView.setAdapter(adapter);


    }

    @Override
    public int getItemCount() {
            return mDataSet.size();
        }

    public User getUser(int position){
        return mDataSet.get(position);
    }


}
