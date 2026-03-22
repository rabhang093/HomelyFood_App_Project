package com.example.android.HomelyFood;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Database.Database;
import com.example.android.HomelyFood.Interface.ItemClickListener;
import com.example.android.HomelyFood.Model.Food;
import com.example.android.HomelyFood.Model.Order;
import com.example.android.HomelyFood.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    Database localDB;

    //Facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //create Target from picasso
    Target target=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto photo=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if(ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }


        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId="";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //init facebook
        callbackManager=CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);

        //init firebase
        database =FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");

        localDB=new Database(this);

        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null){
                    if(Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else
                        Toast.makeText(FoodList.this, "Please check your Internet connection!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null){
                    if(Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else
                        Toast.makeText(FoodList.this, "Please check your Internet connection!!!", Toast.LENGTH_SHORT).show();
                }
                //search
                materialSearchBar=findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your Food here");
                loadSuggest();
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        List<String> suggest=new ArrayList<>();
                        for (String search:suggestList){
                            if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                                suggest.add(search);
                            }
                        }
                        materialSearchBar.setLastSuggestions(suggest);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //when search bar is closed
                        //restore original adapter
                        if(!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //when search finish show result of search adapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });



        recyclerView=findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);




    }

    private void startSearch(CharSequence text) {
        Query query=foodList.orderByChild("name").equalTo(text.toString().toUpperCase());
        FirebaseRecyclerOptions<Food> options =new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query,Food.class)
                .build();
        searchAdapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int i, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);
                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                        Intent fooddetail=new Intent(FoodList.this,FoodDetail.class);
                        fooddetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(fooddetail);

                    }
                });
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);//set adapter for recyclerView is search result

    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Food item=postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName().toUpperCase());
                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood(String categoryId) {
        Query query=foodList.orderByChild("menuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Food> options=new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query,Food.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("₹ %s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //Add Favorites
                if(!localDB.isFavorite(adapter.getRef(position).getKey())){
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }else {
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                }
                //Click to change
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!localDB.isFavorite(adapter.getRef(position).getKey())){
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" is added to favorites", Toast.LENGTH_SHORT).show();

                        }else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+" is removed from favorites", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                //click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getBaseContext()).load(model.getImage()).into(target);
                    }
                });
                //Quick cart
                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getBaseContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model.getName(),
                               "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ));
                        Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });

                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                        Intent fooddetail=new Intent(FoodList.this,FoodDetail.class);
                        fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(fooddetail);

                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
        else if(searchAdapter!=null)
            searchAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }
}
