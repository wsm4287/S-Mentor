package com.example.s_mentor.ui.list;

import android.graphics.Bitmap;

import java.util.List;

public class User {
    public String name;
    public String email;
    public String major;
    public Bitmap bitmap;
    public List<Integer> field;

    public String introduction;

    public List<Integer> getField() {
        return field;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }


}
