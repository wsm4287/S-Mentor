package com.example.s_mentor.ui.mentoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.apply.ApplyListActivity;
import com.example.s_mentor.databinding.FragmentMentoringBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MentoringFragment extends Fragment {

    private FragmentMentoringBinding binding;
    View root;

    TextView proNm, proMj, proPn, title, name_empty, major_empty, phone_empty, pro_date;
    String id, id2, name, mentoring_date;
    Button setting;
    ImageView proPh;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    String encodedImage;

    String[] items = {"취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};
    GridView gridView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMentoringBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        InitMentoring();
        GetMentoring();

        return root;
    }

    private void InitMentoring(){
        id = requireActivity().getIntent().getStringExtra("email");
        name = requireActivity().getIntent().getStringExtra("name");

        setting = root.findViewById(R.id.btLogOut);
        proNm = root.findViewById(R.id.mentoring_userName);
        proMj = root.findViewById(R.id.mentoring_userMajor);
        proPh = root.findViewById(R.id.mentoring_photo);
        proPn = root.findViewById(R.id.mentoring_userPhone);
        title = root.findViewById(R.id.mentoring_title);
        name_empty = root.findViewById(R.id.mentoring_name);
        major_empty = root.findViewById(R.id.mentoring_major);
        phone_empty =  root.findViewById(R.id.mentoring_phone);
        pro_date = root.findViewById(R.id.mentoring_date);

        gridView = root.findViewById(R.id.mentoring_list);

        setting.setOnClickListener(v -> {
            String[] array = {"프로필", "멘토링 신청 목록", "로그아웃"};

            AlertDialog.Builder setting = new AlertDialog.Builder(requireContext())
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            Intent in = new Intent(root.getContext(), ProfileActivity.class);
                            in.putExtra("email", id);
                            startActivity(in);
                        }
                        if(which == 1){
                            Intent in = new Intent(getActivity(), ApplyListActivity.class);
                            in.putExtra("email", id);
                            in.putExtra("name", name);
                            startActivity(in);
                        }
                        if(which == 2){
                            HashMap<String, Object> user = new HashMap<>();
                            user.put("token", "");
                            database.collection("users").document(id)
                                    .update(user).addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            Intent in= new Intent(root.getContext(), MainActivity.class);
                                            in.putExtra("Logout", true);
                                            startActivity(in);
                                        }
                                    });

                        }
                    });

            AlertDialog alertDialog = setting.create();
            alertDialog.show();
        });
    }

    private void GetMentoring(){
        database.collection("users").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                   id2 = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("mentoring")).toString();
                   if(id2.contains("@")){
                       ViewMentoring();
                   }
                   else{
                       title.setText("멘토링 상대가 없습니다.");
                       proNm.setVisibility(View.GONE);
                       proMj.setVisibility(View.GONE);
                       proPh.setVisibility(View.GONE);
                       proPn.setVisibility(View.GONE);
                       gridView.setVisibility(View.GONE);
                       pro_date.setVisibility(View.GONE);
                       name_empty.setVisibility(View.GONE);
                       major_empty.setVisibility(View.GONE);
                       phone_empty.setVisibility(View.GONE);
                   }
                });
    }

    private void ViewMentoring(){
        database.collection("users").document(id2)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    proNm.setText(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("name")).toString());
                    proMj.setText(Objects.requireNonNull(documentSnapshot.getData().get("major")).toString());
                    encodedImage = Objects.requireNonNull(documentSnapshot.getData().get("image")).toString();
                    proPh.setImageBitmap(DecodeImage(encodedImage));
                    proPn.setText(Objects.requireNonNull(documentSnapshot.getData().get("phone")).toString());
                    mentoring_date = Objects.requireNonNull(documentSnapshot.getData().get("mentoring_date")).toString();

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = dateFormat.parse(mentoring_date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Calendar c = Calendar.getInstance();

                    assert date != null;
                    long diffSec = (c.getTime().getTime() -date.getTime()) / 1000;
                    long diffDays = diffSec / (24*60*60) +1;

                    pro_date.setText("D+ " + diffDays);

                    String[] x = {"", "", "", "", "", "", ""};
                    int count = 0;

                    for(int i=0; i<7; i++){
                        if(((Objects.requireNonNull(documentSnapshot.getData().get(Integer.toString(i)))).toString()).equals("o")){
                            x[count++] = items[i];
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.field_view, x);
                    gridView.setAdapter(adapter);

                });
    }

    private Bitmap DecodeImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}