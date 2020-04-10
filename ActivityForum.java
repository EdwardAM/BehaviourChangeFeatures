package com.example.ikoala.ui.social;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;

import com.example.ikoala.R;
import com.example.ikoala.database.BaseActivity;
import com.example.ikoala.ui.ParentActivity;
import com.example.ikoala.utils.ColorUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ActivityForum extends ParentActivity {

    private BaseActivity mActivity;

    private Button successStoriesBtn;
    private Button queriesBtn;
    private Button localOpportunitiesBtn;
    private TextView forumDescription;

    private TextView disclaimerView;
    private String disclaimer = "iKOALA does not manage the content  of the external forums that are referenced below. You are encouraged to determine the reliability of the content of these pages before acting on it.";
    private String description = "The buttons below will direct you to each sub category for the <activity> forum.";

    private TextView genericForum1View_Link;
    private TextView genericForum2View_Link;

    private TextView genericForum1View_Label;
    private TextView genericForum2View_Label;

    private String genericForum1_Label = "Versus Arthritis:";
    private String genericForum1_Link = "https://community.versusarthritis.org/categories";
    private String genericForum2_Label = "Health Unlocked:";
    private String genericForum2_Link = "https://healthunlocked.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        mActivity = getIntent().getParcelableExtra("activity");

        ActionBar bar = getSupportActionBar();
        if (bar != null)
        {
            bar.show();
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            int color = ColorUtils.getColor(this, R.attr.themeColorHighlight);
            String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & color));
            bar.setTitle(HtmlCompat.fromHtml("<font color=\"" + htmlColor + "\">" + mActivity.getName() + " Forum</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            bar.setBackgroundDrawable(new ColorDrawable(ColorUtils.getColor(this, R.attr.themeColorSecondary)));
        }

        description = description.replace("<activity>", mActivity.getName());

        successStoriesBtn = findViewById(R.id.success_stories_btn);
        queriesBtn = findViewById(R.id.queries_btn);
        localOpportunitiesBtn = findViewById(R.id.local_opportunities_btn);
        forumDescription = findViewById(R.id.forumDescription);
        disclaimerView = findViewById(R.id.disclaimer_description_forum);
        disclaimerView.setText(disclaimer);

        genericForum1View_Label = findViewById(R.id.generic_forum_label1);
        genericForum1View_Label.setText(genericForum1_Label);
        genericForum1View_Link = findViewById(R.id.generic_forum_link1);
        genericForum1View_Link.setText(genericForum1_Link);

        genericForum2View_Label = findViewById(R.id.generic_forum_label2);
        genericForum2View_Label.setText(genericForum2_Label);
        genericForum2View_Link = findViewById(R.id.generic_forum_link2);
        genericForum2View_Link.setText(genericForum2_Link);

        genericForum1View_Link.setOnClickListener(v -> {
            LinkAlert.show(genericForum1_Link, this);
        });
        genericForum2View_Link.setOnClickListener(v -> {
            LinkAlert.show(genericForum2_Link, this);
        });

        forumDescription.setText(description);

        successStoriesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityForum.this, ForumContent.class);
            intent.putExtra("activity", mActivity);
            intent.putExtra("forumPage", "success_stories");
            startActivity(intent);
        });
        queriesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityForum.this, ForumContent.class);
            intent.putExtra("activity", mActivity);
            intent.putExtra("forumPage", "queries_and_concerns");
            startActivity(intent);
        });
        localOpportunitiesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityForum.this, ForumContent.class);
            intent.putExtra("activity", mActivity);
            intent.putExtra("forumPage", "local_opportunities");
            startActivity(intent);
        });

    }

    @Override
    public void onBackPressed()
    {
        super.onDefaultBackPressed();
    }

    @Override
    protected void setupViewUI() {

    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
