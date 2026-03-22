package com.example.android.HomelyFood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    MaterialEditText edtPhone,edtPassword;
    FButton btnSignIn;
    CheckBox ckbRemember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone=(MaterialEditText)findViewById(R.id.edtPhone);
        edtPassword=(MaterialEditText)findViewById(R.id.edtPassword);
        btnSignIn=findViewById(R.id.btnSignIn);
        ckbRemember=(CheckBox)findViewById(R.id.ckbRemember);

        //init paper
        Paper.init(this);
        //init firebase
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=database.getReference("users");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate
                if(Common.isConnectedToInternet(getBaseContext())){
                    if(!edtPhone.getText().toString().isEmpty() && edtPhone.getText().toString()!=null) {
                        //save username and password
                        if(ckbRemember.isChecked()){
                            Paper.book().write(Common.USR_KEY,edtPhone.getText().toString());
                            Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                        }
                        final ProgressDialog myDialog=new ProgressDialog(SignIn.this);
                        myDialog.setMessage("Please wait...");
                        myDialog.show();

                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //check if user not exist in database
                                if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                    //get user information
                                    myDialog.dismiss();
                                    User user=dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                    user.setPhone(edtPhone.getText().toString());
                                    if(user.getPass().equals(edtPassword.getText().toString())){
                                        //  Toast.makeText(SignIn.this, "Sign In Successfully!", Toast.LENGTH_SHORT).show();
                                        Intent homeIntent=new Intent(SignIn.this,Home.class);
                                        Common.currentUser=user;
                                        startActivity(homeIntent);
                                        finish();

                                        table_user.removeEventListener(this);
                                    }else {

                                        Toast.makeText(SignIn.this, "Wrong password...!!!", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    myDialog.dismiss();
                                    Toast.makeText(SignIn.this, "User not exist in Database...", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else {
                        Toast.makeText(SignIn.this, "Enter Phone Number First!!!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignIn.this, "Please check your Internet connection!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
