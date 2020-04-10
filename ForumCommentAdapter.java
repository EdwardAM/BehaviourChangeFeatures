package com.example.ikoala.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.example.ikoala.R;
import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.ui.ParentActivity;

import java.util.Map;

public class ForumCommentAdapter extends RecyclerView.Adapter<ForumCommentAdapter.ViewHolder> {

    private List<Map<String, Object>> list;
    private ParentActivity activity; //used to translate userId to "You" for current user

    public ForumCommentAdapter(List<Map<String, Object>> list, ParentActivity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ForumCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_expanded_forum_post_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumCommentAdapter.ViewHolder holder, int position) {

        String datePosted = list.get(position).get("datePosted").toString();
        Date date = new Date();
        if(!datePosted.isEmpty()){
            datePosted = datePosted.substring(datePosted.indexOf("seconds=") + 8);
            datePosted = datePosted.substring(0, datePosted.indexOf(","));
            long seconds = Long.parseLong(datePosted);
            seconds *= 1000;
            date = new Date(seconds);
        }
        holder.commentDate.setText(date.toString());
        holder.commentDescription.setText((list.get(position)).get("comment").toString());

        String userId = list.get(position).get("name").toString();
        if(userId.equals(activity.getFirebaseAuth().getUid())){
            holder.postedBy.setText("You");
        }else{
            DatabaseManager.getUserName(list.get(position).get("name").toString(),
                    userName -> holder.postedBy.setText(userName));
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView commentDescription;
        TextView commentDate;
        TextView postedBy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentDate = itemView.findViewById(R.id.text_view_comment_date);
            commentDescription = itemView.findViewById(R.id.text_view_comment_description);
            postedBy = itemView.findViewById(R.id.text_view_user);
        }
    }
}
