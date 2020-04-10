package com.example.ikoala.ui.social;

import android.content.Intent;
import android.content.Context;

import com.example.ikoala.database.BaseActivity;

public class ShareActivity {

    public static void openShareOptions(Context mContext, BaseActivity activity){
        //use activity to get the name in share message
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBody = "Check this out! I've just used the " + activity.getName() +
                " activity on the iKOALA app. The app is very useful and I thought it might be of interest to you. It's even got a great list of activities for people with knee osteoarthritis!";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,  activity.getName() + " on the iKOALA app");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mContext.startActivity(Intent.createChooser(sharingIntent, "Share with friends"));
    }
}
