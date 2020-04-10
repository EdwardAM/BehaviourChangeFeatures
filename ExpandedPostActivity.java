package com.example.ikoala.ui.social;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ikoala.R;
import com.example.ikoala.adapters.ForumCommentAdapter;
import com.example.ikoala.database.BaseActivity;
import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.database.ForumItem;
import com.example.ikoala.ui.ParentActivity;
import com.example.ikoala.utils.ColorUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpandedPostActivity extends ParentActivity {

    private BaseActivity mActivity;
    private ForumItem postContent;
    private String forumName;
    private String postId;

    private TextView title;
    private TextView description;
    private TextView datePosted;
    private RecyclerView comments;
    private EditText writeComment;
    private RecyclerView.Adapter adapter;
    private List<Map<String, Object>> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_post);
        mActivity = getIntent().getParcelableExtra("activity");
        postContent = getIntent().getParcelableExtra("post");
        forumName = getIntent().getStringExtra("forumPage");
        commentsList = postContent.getComments();
        postId = getIntent().getStringExtra("postId");

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

        title = findViewById(R.id.expanded_post_title);
        description = findViewById(R.id.expanded_post_des);
        comments = findViewById(R.id.expanded_post_recyclerpost);
        comments.setNestedScrollingEnabled(false);
        comments.setHasFixedSize(true);

        title.setText(postContent.getTitle());
        description.setText(postContent.getDescription());
        writeComment = findViewById(R.id.add_post_comment);


        Button addComment = findViewById(R.id.expanded_post_addComment);
        addComment.setOnClickListener(v -> {
            createAComment();
        });

        showPostComments();
    }

    private void showPostComments() {

        RecyclerView recyclerView = findViewById(R.id.expanded_post_recyclerpost);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForumCommentAdapter(commentsList, this);
        comments.setAdapter(adapter);
    }

    private void createAComment() {
        String comment = writeComment.getText().toString();
        Date date = new Date(System.currentTimeMillis());
        String userName = getFirebaseAuth().getUid();

        if(comment.trim().isEmpty()){
            Toast.makeText(this, "Cannot submit blank comment", Toast.LENGTH_SHORT).show();
            return;
        }

        //creating a empty structure to forfil comments section of a new post
        List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
        comments = commentsList;
        Map<String, Object> structure = new HashMap<>();
        structure.put("comment", comment);
        structure.put("datePosted", date);
        structure.put("name", userName);
        comments.add(structure);

        DatabaseManager.setForumPostComments(mActivity.getName().toLowerCase(), forumName, postId, comments);
        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
        finish();
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
