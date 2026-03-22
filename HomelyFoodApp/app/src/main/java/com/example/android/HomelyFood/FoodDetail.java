package com.example.android.HomelyFood;

import androidx.annotation.NonNull;

import com.andremion.counterfab.CounterFab;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Database.Database;
import com.example.android.HomelyFood.Model.Food;
import com.example.android.HomelyFood.Model.Order;
import com.example.android.HomelyFood.Model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;


import java.util.Arrays;

import info.hoang8f.widget.FButton;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;
    FButton btnShowComment;

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference rating_table;

    String foodId="";
    Food currentFood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase
        database=FirebaseDatabase.getInstance();
        foods=database.getReference("Foods");
        rating_table=database.getReference("Rating");

        //init views
        numberButton=findViewById(R.id.number_button);
        btnCart=(CounterFab) findViewById(R.id.btnCart);
        btnRating=findViewById(R.id.btn_rating);
        ratingBar=findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));
                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount(new Database(this).getCountCart());

        btnShowComment=findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        });

        food_name=findViewById(R.id.food_name);
        food_description=findViewById(R.id.food_description);
        food_price=findViewById(R.id.food_price);
        food_image=findViewById(R.id.img_food);

        collapsingToolbarLayout=findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);

        if(getIntent()!=null)
            foodId=getIntent().getStringExtra("FoodId");
        if(!foodId.isEmpty() && foodId!=null){
            if(Common.isConnectedToInternet(this)){
                getDetailFood(foodId);
                getRatingFood(foodId);
            } else
                Toast.makeText(this, "Please check your Internet connection!!!", Toast.LENGTH_SHORT).show();
        }

    }

    private void getRatingFood(String foodId) {
        Query foodRating=rating_table.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item=postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRsteValue());
                    count++;
                }
                if(count!=0){
                    float average=sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Enter your comment here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }

    private void getDetailFood(String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentFood=dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_description.setText(currentFood.getDescription());
                food_name.setText(currentFood.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comment) {
        //get rating and upload it to firebase
        final Rating rating=new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comment);

        rating_table.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(FoodDetail.this, "Thank you for rating this food..!!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


//        rating_table.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.child(Common.currentUser.getPhone()).exists()){
//                    //remove the old value
//                    rating_table.child(Common.currentUser.getPhone()).removeValue();
//                    //update the value
//                    rating_table.child(Common.currentUser.getPhone()).setValue(rating);
//                }else {
//                    //update the value
//                    rating_table.child(Common.currentUser.getPhone()).setValue(rating);
//                }
//                Toast.makeText(FoodDetail.this, "Thank you for rating this food..!!!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCart.setCount(new Database(this).getCountCart());
    }

    @Override
    public void onNeutralButtonClicked() {

    }
}
