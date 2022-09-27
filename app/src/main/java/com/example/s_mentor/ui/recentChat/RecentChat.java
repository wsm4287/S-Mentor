package com.example.s_mentor.ui.recentChat;

import android.graphics.Bitmap;

public class RecentChat {
    public String name;
    public Bitmap bitmap;
    public String lastText;
    public String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getLastText() {
        return lastText;
    }

}
