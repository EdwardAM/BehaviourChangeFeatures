package com.example.ikoala.ui.social;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.example.ikoala.adapters.ForumItemAdapter;
import com.example.ikoala.database.BaseActivity;
import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.database.ForumItem;
import com.example.ikoala.ui.ParentActivity;
import com.example.ikoala.utils.ColorUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ikoala.R;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class ForumContent extends ParentActivity {

    private BaseActivity mActivity;
    private String forumPage;
    private ForumItemAdapter adapter;
    private String disclaimerDescription = "Please be encouraging towards others! Positive feedback can significantly influence others behaviour.  ";
    private TextView disclaimerView;

    //TODO change adming district to actual admin distirct of user
    String userAdminDistrict = "Bath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_content);

        Intent intentRec = getIntent();
        mActivity = intentRec.getParcelableExtra("activity");
        forumPage = intentRec.getStringExtra("forumPage");
        disclaimerView = findViewById(R.id.disclaimer_description_subforum);
        disclaimerView.setText(disclaimerDescription);

        //action bar
        ActionBar bar = getSupportActionBar();
        if (bar != null)
        {
            bar.show();
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            int color = ColorUtils.getColor(this, R.attr.themeColorHighlight);
            String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & color));
            bar.setTitle(HtmlCompat.fromHtml("<font color=\"" + htmlColor + "\">" + mActivity.getName() + " " + forumPage + " </font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            bar.setBackgroundDrawable(new ColorDrawable(ColorUtils.getColor(this, R.attr.themeColorSecondary)));
        }

        //floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(ForumContent.this, NewForumPostActivity.class);
            intent.putExtra("activity", mActivity);
            intent.putExtra("forumPage", forumPage);
            startActivity(intent);
        });

        setUpRecyclerView();
    }

    @Override
    protected void setupViewUI() { }

    private void setUpRecyclerView() {

        Query desiredQuery = DatabaseManager.getForum(mActivity.getName().toLowerCase(), forumPage)
                .orderBy("datePosted", Query.Direction.DESCENDING);

        //local opportunities is an exception as data in the database requires filtering by location
        if(forumPage.equals("local_opportunities")){
            desiredQuery = DatabaseManager.getForum(mActivity.getName().toLowerCase(), forumPage)
                    .whereEqualTo("location", userAdminDistrict).orderBy("datePosted", Query.Direction.DESCENDING);
        }

        FirestoreRecyclerOptions<ForumItem> options = new FirestoreRecyclerOptions.Builder<ForumItem>()
                .setQuery(desiredQuery, ForumItem.class)
                .build();
        adapter = new ForumItemAdapter(options, this);

        RecyclerView recyclerView = findViewById(R.id.forum_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Setting onClickListener for when each post is clicked
        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            ForumItem item = documentSnapshot.toObject(ForumItem.class);
            String id = documentSnapshot.getId();
            String path = documentSnapshot.getReference().getPath();
            Intent intent = new Intent(ForumContent.this, ExpandedPostActivity.class);
            intent.putExtra("post", item);
            intent.putExtra("forumPage", forumPage);
            intent.putExtra("postId", documentSnapshot.getId());
            intent.putExtra("activity", mActivity);
            startActivity(intent);
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop(){
        super.onStop();
        adapter.stopListening();
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
