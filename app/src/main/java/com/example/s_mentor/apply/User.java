package com.example.s_mentor.apply;

import android.graphics.Bitmap;

public class User {
    public String name;
    public String email;
    public String major;
    public Bitmap bitmap;
    public String token;

    public String docName;

    public Bitmap getBitmap() {
        return bitmap;
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
