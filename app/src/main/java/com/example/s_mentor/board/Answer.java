package com.example.s_mentor.board;

import android.graphics.Bitmap;

public class Answer {
    public String name;
    public String email;
    public boolean check;
    public String text;
    public Bitmap bitmap;
    public String docId1;
    public String docId2;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String time;

    public String getDocId1() {
        return docId1;
    }

    public void setDocId1(String docId1) {
        this.docId1 = docId1;
    }

    public String getDocId2() {
        return docId2;
    }

    public void setDocId2(String docId2) {
        this.docId2 = docId2;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
