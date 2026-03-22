package com.example.android.HomelyFood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Model.User;
import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    FButton btnSignIn,btnSignUp;
    TextView txtSlogan;
    FirebaseDatabase database;
    DatabaseReference table_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        printKeyHash();

        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUp);
        txtSlogan=findViewById(R.id.txtSlogan);

        //init paper
        Paper.init(this);

        //init firebase
        database=FirebaseDatabase.getInstance();
        table_user=database.getReference("users");

        Typeface face= Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignIn.class);
                startActivity(intent);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);
            }
        });

        String user= Paper.book().read(Common.USR_KEY);
        String pwd=Paper.book().read(Common.PWD_KEY);

        if(user!=null && pwd !=null){
            if(!user.isEmpty()&& !pwd.isEmpty()){
                login(user,pwd);
            }
        }

    }

    private void printKeyHash() {
        try {
            PackageInfo info=getPackageManager().getPackageInfo("com.example.android.HomelyFood",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures){
                MessageDigest messageDigest=MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void login(final String phone, final String password){
        final ProgressDialog myDialog=new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Please wait...");
        myDialog.show();

        table_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //check if user not exist in database
                if (dataSnapshot.child(phone).exists()) {
                    //get user information
                    myDialog.dismiss();
                    User user=dataSnapshot.child(phone).getValue(User.class);
                    user.setPhone(phone);
                    if(user.getPass().equals(password)){
                        //  Toast.makeText(SignIn.this, "Sign In Successfully!", Toast.LENGTH_SHORT).show();
                        Intent homeIntent=new Intent(MainActivity.this,Home.class);
                        Common.currentUser=user;
                        startActivity(homeIntent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Wrong password...!!!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    myDialog.dismiss();
                    Toast.makeText(MainActivity.this, "User not exist in Database...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
