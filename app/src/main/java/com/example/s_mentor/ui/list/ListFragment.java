package com.example.s_mentor.ui.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s_mentor.MainActivity;
import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.chat.ChatActivity;
import com.example.s_mentor.databinding.FragmentListBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    View root;

    String[] field_items = {"전체", "취업", "면접", "학업", "창업", "석사", "봉사", "동아리"};
    String[] major_items = {"전체", "유학대학", "문과대학", "사회과학대학", "경제대학", "경영대학", "사범대학", "예술대학", "자연과학대학",
            "정보통신대학", "소프트웨어융합대학", "공과대학", "약학대학", "생명공학대학", "스포츠과학대학", "의과대학", "성균융합원"};
    Button setting, search;
    String id, id2, type, major, encodedImage;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    ListAdapter listAdapter;
    ArrayList<User> userArrayList, userMarkList, filterList, beforeList;
    List<String> favoriteList;
    TextView listTitle, filterMajor, filterField;
    Boolean mark;
    Spinner majorSpinner, fieldSpinner;
    int field;

    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        InitUserList();

        SelectMajor();
        SelectField();
        SearchFilter();

        FavoriteCreate();
        SelectUser();

        return root;
    }

    private void ListCreate(){

        database.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            if(document.getId().equals(id)) continue;
                            if(Objects.requireNonNull(Objects.requireNonNull(document.getData()).get("type")).toString().equals(type)) continue;
                            if(favoriteList.contains(document.getId())) continue;

                            User user = new User();
                            user.name = Objects.requireNonNull(document.getData().get("name")).toString();
                            user.major = Objects.requireNonNull(document.getData().get("major")).toString();
                            user.email = document.getId();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            user.bitmap = DecodeImage(encodedImage);
                            user.introduction = Objects.requireNonNull(document.getData().get("introduction")).toString();

                            List<Integer> field = new ArrayList<>();

                            for(int i=0; i<7; i++){
                                if(((Objects.requireNonNull(document.getData().get(Integer.toString(i)))).toString()).equals("o")){
                                    field.add(i);
                                }
                            }
                            user.field = field;
                            userArrayList.add(user);
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                });
    }


    private void FavoriteCreate(){

        database.collection("users").document(id).collection("favorite")
                .addSnapshotListener((value, error) -> {

                    userMarkList.clear();
                    favoriteList.clear();
                    userArrayList.clear();
                    assert value != null;
                    for (QueryDocumentSnapshot document : value) {
                        if((""+document.getData()).equals("null")) continue;
                        if((""+document.getData().get("mark")).equals("1")){
                            User user = new User();
                            user.name = Objects.requireNonNull(document.getData().get("name")).toString();
                            user.major = Objects.requireNonNull(document.getData().get("major")).toString();
                            user.email = document.getId();
                            encodedImage = Objects.requireNonNull(document.getData().get("image")).toString();
                            user.bitmap = DecodeImage(encodedImage);
                            user.introduction = Objects.requireNonNull(document.getData().get("introduction")).toString();

                            List<Integer> field = new ArrayList<>();

                            for(int i=0; i<7; i++){
                                if(((Objects.requireNonNull(document.getData().get(Integer.toString(i)))).toString()).equals("o")){
                                    field.add(i);
                                }
                            }
                            user.field = field;

                            userMarkList.add(user);
                            userArrayList.add(user);
                            favoriteList.add(user.email);
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                    ListCreate();
                });

    }

    public void SelectUser(){
        listAdapter.setOnItemClickListener((holder, view, position) -> {
            User u = listAdapter.getUser(position);
            id2 = u.email;

            Drawable drawable = new BitmapDrawable(getResources(),u.bitmap);
            final ImageView iv = new ImageView(getContext());
            iv.setImageBitmap(u.bitmap);
            final TextView et = new TextView(getContext());
            et.setText(u.introduction);
            et.setTextSize(20);
            AlertDialog.Builder ad = new AlertDialog.Builder(requireContext(), R.style.MyDialog)
                    .setView(iv)
                    .setIcon(drawable)
                    .setTitle(u.name)
                    .setMessage(id2).setView(et)
                    .setPositiveButton("종료", (dialog, which) -> {
                    })
                    .setNeutralButton("채팅", (dialog, which) -> {
                        Intent in = new Intent(getActivity(), ChatActivity.class);
                        in.putExtra("email", id);
                        in.putExtra("email2", id2);
                        in.putExtra("type", type);
                        startActivity(in);
                    });
            ad.show();
        });

    }

    public void InitUserList(){

        id = requireActivity().getIntent().getStringExtra("email");
        type = requireActivity().getIntent().getStringExtra("type");

        context = this.getContext();

        setting = root.findViewById(R.id.btLogOut);
        listTitle = root.findViewById(R.id.listTitle);
        filterMajor = root.findViewById(R.id.search_major);
        filterField = root.findViewById(R.id.search_field);
        majorSpinner = root.findViewById(R.id.major_spinner);
        fieldSpinner = root.findViewById(R.id.field_spinner);
        search = root.findViewById(R.id.btSearch);
        mark = false;

        if(type.equals("Mentor")){
            listTitle.setText("멘티 목록");
        }
        else{
            listTitle.setText("멘토 목록");
        }

        userArrayList = new ArrayList<>();
        userMarkList = new ArrayList<>();
        filterList = new ArrayList<>();
        beforeList = new ArrayList<>();

        RecyclerView recyclerView = root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        listAdapter = new ListAdapter(userArrayList);
        recyclerView.setAdapter(listAdapter);
        favoriteList = new ArrayList<>();

        ArrayAdapter<String> majorAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, major_items);
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        majorSpinner.setAdapter(majorAdapter);

        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, field_items);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);


        setting.setOnClickListener(v -> {
            String[] array = {"프로필", "즐겨찾기 목록", "로그아웃"};

            if(mark) array[1] = "전체 목록";

            AlertDialog.Builder setting = new AlertDialog.Builder(requireContext())
                    .setItems(array, (dialog, which) -> {
                        if(which == 0){
                            Intent in = new Intent(root.getContext(), ProfileActivity.class);
                            in.putExtra("email", id);
                            startActivity(in);
                        }
                        if(which == 1){
                            if(!mark) {
                                listAdapter.filterList(userMarkList);
                                mark = true;
                            }
                            else{
                                listAdapter.filterList(userArrayList);
                                mark = false;
                            }
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


    public void SelectMajor(){
        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    major="";
                    return;
                }
                if(position == 1){
                    String[] a = {"유학.동양학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>0) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 2){
                    String[] a = {"국어국문학과", "영어영문학과", "프랑스어문학과", "중어중문학과", "독어독문학과",
                            "러시아어문학과", "한문학과", "사학과", "철학과", "문헌정보학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>9) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 3){
                    String[] a = {"행정학과", "정치외교학과", "미디어커뮤니케이션학과", "사회학과", "사회복지학과",
                            "심리학과", "소비자학과", "아동·청소년학과", "글로벌리더학부", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>8) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 4){
                    String[] a = {"경제학과", "통계학과", "글로벌경제학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>2) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 5){
                    String[] a = {"경영학과", "글로벌경영학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>1) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 6){
                    String[] a = {"교육학과", "한문교육과", "수학교육과", "컴퓨터교육과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>3) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 7){
                    String[] a = {"미술학과", "디자인학과", "무용학과", "영상학과", "연기예술학과",
                            "의상학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>5) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 8){
                    String[] a = {"생명과학과", "수학과", "물리학과", "화학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>3) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 9){
                    String[] a = {"전자전기공학부", "반도체시스템공학과", "소재부품융합공학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>2) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 10){
                    String[] a = {"소프트웨어학과", "글로벌융합학부", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>1) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 11){
                    String[] a = {"화학공학/고분자공학부", "신소재공학부", "기계공학부", "건설환경공학부",
                            "시스템경영공학과", "건축학과", "나노공학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>6) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 12){
                    String[] a = {"약학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>0) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 13){
                    String[] a = {"식품생명공학과", "바이오메카트로닉스학과", "융합생명공학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>2) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 14){
                    String[] a = {"스포츠과학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>0) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 15){
                    String[] a = {"의학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>0) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else if(position == 16){
                    String[] a = {"글로벌바이오메디컬공학과", "돌아가기"};

                    AlertDialog.Builder setting = new AlertDialog.Builder(context)
                            .setItems(a, (dialog, which) -> {
                                if(which>0) return;
                                major = a[which];
                            });

                    AlertDialog alertDialog = setting.create();
                    alertDialog.show();
                }
                else{
                    major="";
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                major = "";

            }
        });
    }

    public void SelectField(){
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                field = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                field = 0;
            }
        });

    }

    public void SearchFilter(){
        search.setOnClickListener(v -> {
            filterList.clear();
            if(field == 0){
                if(!mark){
                    for(int i=0; i<userArrayList.size(); i++){
                        if(userArrayList.get(i).getMajor().toLowerCase().contains(major.toLowerCase())){
                            filterList.add(userArrayList.get(i));
                        }
                    }
                }

                else{
                    for(int i=0; i<userMarkList.size(); i++){
                        if(userMarkList.get(i).getMajor().toLowerCase().contains(major.toLowerCase())){
                            filterList.add(userMarkList.get(i));
                        }
                    }
                }
                listAdapter.filterList(filterList);
                return;
            }

            if(!mark){
                for(int i=0; i<userArrayList.size(); i++){
                    if((userArrayList.get(i).getField().contains(field-1)) && (userArrayList.get(i).getMajor().toLowerCase().contains(major.toLowerCase()))){
                        filterList.add(userArrayList.get(i));
                    }
                }
            }

            else{
                for(int i=0; i<userMarkList.size(); i++){
                    if((userMarkList.get(i).getField().contains(field-1)) && (userMarkList.get(i).getMajor().toLowerCase().contains(major.toLowerCase()))){
                        filterList.add(userMarkList.get(i));
                    }
                }
            }

            listAdapter.filterList(filterList);
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