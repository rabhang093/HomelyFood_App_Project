package com.example.android.HomelyFood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.android.HomelyFood.Database.Database;
import com.example.android.HomelyFood.Model.Banner;
import com.example.android.HomelyFood.Service.ListenOrder;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Interface.ItemClickListener;
import com.example.android.HomelyFood.Model.Category;
import com.example.android.HomelyFood.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    //Slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //init paper
        Paper.init(this);

        //init Firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");

        FirebaseRecyclerOptions<Category> options=new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int i, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(Home.this, ""+clickItem.getName(), Toast.LENGTH_SHORT).show();

                        //get category id & send it to next Activity
                        Intent foodlist=new Intent(Home.this,FoodList.class);
                        //because categoryId is key so we have to get key of this item
                        foodlist.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodlist);

                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(view);
            }
        };


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent= new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database(this).getCountCart());

        //views
        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext())){
                    loadMenu();
                }else {
                    Toast.makeText(getBaseContext(), "Please check your Internet Connection!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getBaseContext())){
                    loadMenu();
                }else {
                    Toast.makeText(getBaseContext(), "Please check your Internet Connection!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //set user name
        View headerView= navigationView.getHeaderView(0);
        txtFullName=headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());


        //set menu
        recycler_menu=findViewById(R.id.recycler_menu);
        //recycler_menu.setHasFixedSize(true);
        //layoutManager=new LinearLayoutManager(this);
        //recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller= AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);


        //setup slider
        setupSlider();

        //start service
        Intent intent=new Intent(Home.this, ListenOrder.class);
        startService(intent);




    }

    private void setupSlider() {
        mSlider=findViewById(R.id.slider);
        image_list=new HashMap<>();

        final DatabaseReference banners=database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Banner banner=postSnapshot.getValue(Banner.class);
                    assert banner != null;
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for(String key:image_list.keySet()){
                    String[] keySplit=key.split("@@@");
                    String nameOfFood=keySplit[0];
                    final String idOfFood=keySplit[1];

                    //Create slider
                    final TextSliderView textSliderView=new TextSliderView(getBaseContext());
                    textSliderView.description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent=new Intent(Home.this,FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idOfFood);

                    mSlider.addSlider(textSliderView);
                    //Remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(3000);
    }

    public void loadMenu() {

        recycler_menu.setAdapter(adapter);
        adapter.startListening();
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {
            loadMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {

            Intent cartIntent= new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_orders) {

            Intent orderIntent= new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_sign_out) {
            //delete saved password nd phone
            Paper.book().destroy();

            Intent signIn= new Intent(Home.this,SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }else if(id==R.id.nav_change_password){
            showChangePasswordDialog();
        }else if(id==R.id.nav_home_address){
            showHomeAddressDialog();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Home Address");
        alertDialog.setMessage("Please fill all information");
        alertDialog.setIcon(R.drawable.ic_home_black_24dp);

        LayoutInflater inflater =LayoutInflater.from(this);
        View layout_home=inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress=(MaterialEditText) layout_home.findViewById(R.id.edtHomeaddress);

        alertDialog.setView(layout_home);
        //Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //set new address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                //in firebase
                FirebaseDatabase.getInstance().getReference("users")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Update Address Successful", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please fill all information");
        alertDialog.setIcon(R.drawable.ic_lock_black_24dp);

        LayoutInflater inflater =LayoutInflater.from(this);
        View layout_pwd=inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword=(MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtReenterPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtReenterPassword);

        alertDialog.setView(layout_pwd);
        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //change password here
                final ProgressDialog waitingDialog;
                waitingDialog = new ProgressDialog(Home.this);
                waitingDialog.setMessage("Please wait...");
                waitingDialog.show();

                //check password
                if(!edtPassword.getText().toString().isEmpty() && !edtNewPassword.getText().toString().isEmpty() && !edtReenterPassword.getText().toString().isEmpty()
                && edtPassword.getText().toString()!=null && edtNewPassword.getText().toString()!=null && edtReenterPassword.getText().toString()!=null ){
                    if(edtPassword.getText().toString().equals(Common.currentUser.getPass())){
                        //check new password n reenter password
                        if(edtNewPassword.getText().toString().equals(edtReenterPassword.getText().toString())){

                            Map<String,Object> passwordUpdate=new HashMap<>();
                            passwordUpdate.put("pass",edtNewPassword.getText().toString());

                            DatabaseReference users=FirebaseDatabase.getInstance().getReference("users");
                            users.child(Common.currentUser.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitingDialog.dismiss();
                                            Common.currentUser.setPass(edtNewPassword.getText().toString());
                                            Toast.makeText(Home.this, "Password updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                   // Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }else {
                            waitingDialog.dismiss();
                            Toast.makeText(Home.this, "enter same new & reenter password", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "Enter correct old password!!!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "please enter details", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());
        if(adapter!=null)
            adapter.startListening();
    }
}
