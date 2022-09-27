package com.example.s_mentor.chat;

public class Chat {
    public String datetime;
    public String text;
    public String email;
    public String type;
    public String uri;
    public String fileName;

    public String getUri() {
        return uri;
    }


    public String getFileName() {
        return fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatetime() {
        return datetime;
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
}
