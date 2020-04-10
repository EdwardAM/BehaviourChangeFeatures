package com.example.ikoala.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.database.ForumItem;
import com.example.ikoala.ui.ParentActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.example.ikoala.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class ForumItemAdapter extends FirestoreRecyclerAdapter<ForumItem, ForumItemAdapter.ForumItemHolder> {

    private OnItemClickListener listener;
    private ParentActivity activity; //used to retrieve context

    public ForumItemAdapter(@NonNull FirestoreRecyclerOptions<ForumItem> options, ParentActivity activity){
        super(options);
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull ForumItemHolder holder, int position, @NonNull ForumItem entry) {
        holder.title.setText(entry.getTitle());
        holder.description.setText(entry.getDescription());

        String userId = getItem(position).getUserId();
        if(userId.equals(activity.getFirebaseAuth().getUid())){
            holder.userId.setText("You");
        }else{
            DatabaseManager.getUserName(userId,
                    userName -> {
                        holder.userId.setText(userName);
                    });
        }

        holder.datePosted.setText(entry.getDatePosted().toString());
    }

    @NonNull
    @Override
    public ForumItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_forum_entry,
                parent, false);
        return new ForumItemHolder(v);
    }

    public void deleteItem(int position) {
        //get the firestore document
        getSnapshots().getSnapshot(position).getReference().delete();

    }

    class ForumItemHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView description;
        TextView userId;
        TextView datePosted;

        public ForumItemHolder(@NonNull View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            description = itemView.findViewById(R.id.text_view_description);
            userId = itemView.findViewById(R.id.text_view_priority);
            datePosted = itemView.findViewById(R.id.text_view_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION && listener != null){
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){ this.listener = listener;}
}
