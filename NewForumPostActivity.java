package com.example.ikoala.ui.social;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ikoala.R;
import com.example.ikoala.database.BaseActivity;
import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.database.ForumItem;
import com.example.ikoala.ui.ParentActivity;
import com.example.ikoala.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewForumPostActivity extends ParentActivity {

    private BaseActivity mActivity;
    private String forumName;

    private Button uploadPostBtn;
    private EditText editTextTitle;
    private EditText editTextDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_forum_post);

        Intent intent = getIntent();
        mActivity = intent.getParcelableExtra("activity");
        forumName = intent.getStringExtra("forumPage");

        //action bar
        ActionBar bar = getSupportActionBar();
        if (bar != null)
        {
            bar.show();
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            int color = ColorUtils.getColor(this, R.attr.themeColorHighlight);
            String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & color));
            bar.setTitle(HtmlCompat.fromHtml("<font color=\"" + htmlColor + "\"> Create Post </font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            bar.setBackgroundDrawable(new ColorDrawable(ColorUtils.getColor(this, R.attr.themeColorSecondary)));
        }
        setupViewUI();


        uploadPostBtn = findViewById(R.id.upload_post);
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);

        uploadPostBtn.setOnClickListener(v -> {
            uploadPost();
        });

    }

    @Override
    protected void setupViewUI() {

    }

    protected void uploadPost() {

        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String userId = getFirebaseAuth().getUid();

        //TODO: convert hardcoded user location to actual location of user
        String userAdminDistrict = "Bath";

        if(title.trim().isEmpty() || description.trim().isEmpty()){
            Toast.makeText(this, "Please insert title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        //creating a empty structure to forfil comments section of a new post
        List<Map<String, Object>> emptyComments = new ArrayList<Map<String, Object>>();
        ForumItem upload;
        upload = new ForumItem(title, description, userId, new Date(System.currentTimeMillis()), userAdminDistrict, emptyComments);

        if(mActivity == null | forumName == null){
            return;
        }
        DatabaseManager.setForum(mActivity.getName().toLowerCase(), forumName, upload);
        Toast.makeText(this, "Post uploaded", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed()
    {
        super.onDefaultBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

}
