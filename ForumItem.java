package com.example.ikoala.database;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ForumItem implements Parcelable {

    private String title;
    private String description;
    private String userId;
    private Date datePosted;
    private String location;
    private List<Map<String, Object>> comments;

    public ForumItem(){}

    public ForumItem(String title, String description, String userId, Date datePosted, String location, List<Map<String, Object>> comments){
        this.description = description;
        this.title = title;
        this.userId = userId;
        this.datePosted = datePosted;
        this.comments = comments;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }

    public Date getDatePosted() {return datePosted;}

    public String getLocation() {return location;}

    public List<Map<String, Object>> getComments() { return comments; }

    @SuppressWarnings("unchecked") //prevents complaining of comments assignment
    public ForumItem(Parcel in){
        title = in.readString();
        description = in.readString();
        datePosted = (java.util.Date) in.readSerializable();
        userId = in.readString();
        location = in.readString();
        //this might break
        comments = in.readArrayList(getClass().getClassLoader());
    }

    public static final Creator<ForumItem> CREATOR = new Creator<ForumItem>() {
        @Override
        public ForumItem createFromParcel(Parcel in) {
            return new ForumItem(in);
        }

        @Override
        public ForumItem[] newArray(int size) {
            return new ForumItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeSerializable(datePosted);
        dest.writeString(userId);
        dest.writeString(location);
        dest.writeList(comments);
    }
}
