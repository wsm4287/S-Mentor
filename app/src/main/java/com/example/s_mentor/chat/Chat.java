package com.example.s_mentor.chat;

public class Chat {
    public String datetime;
    public String text;
    public String email;

    public String getDatetime() {
        return datetime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
