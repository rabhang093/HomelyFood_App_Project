package com.example.android.HomelyFood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Model.Rating;
import com.example.android.HomelyFood.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout swipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);

        //Firebase
        database=FirebaseDatabase.getInstance();
        ratingTbl=database.getReference("Rating");

        recyclerView=findViewById(R.id.recycler_comment);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshlayout
        swipeRefreshLayout=findViewById(R.id.commentSwipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent()!=null){
                    foodId=getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if(!foodId.isEmpty() && foodId!=null){
                    Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);
                    FirebaseRecyclerOptions<Rating> options=new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();
                    adapter=new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {
                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRsteValue()));
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.show_comment_layout, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };
                    loadComment(foodId);
                }
            }
        });
        //Thread to load comment on first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                if(getIntent()!=null){
                    foodId=getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if(!foodId.isEmpty() && foodId!=null){
                    Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);
                    FirebaseRecyclerOptions<Rating> options=new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();
                    adapter=new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {
                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRsteValue()));
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.show_comment_layout, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };
                    loadComment(foodId);
                }
            }
        });


    }

    private void loadComment(String foodId) {
        adapter.startListening();

        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }
}
