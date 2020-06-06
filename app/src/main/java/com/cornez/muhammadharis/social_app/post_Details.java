package com.cornez.muhammadharis.social_app;

public class post_Details {
    public String date;
    public String description;
    public String fullname;
    public String postimage;
    public String profileimage;
    public String time;
    public String uid;
    public String update;

    public post_Details( ) {
    }

    public post_Details(String date, String description, String fullname, String postimage, String profileimage, String time, String uid,String update) {
        this.date = date;
        this.description = description;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.time = time;
        this.uid = uid;
        this.update =update;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getupdate() {
        return update;
    }

    public void setupdate(String update) {
        this.update = update;
    }
}
