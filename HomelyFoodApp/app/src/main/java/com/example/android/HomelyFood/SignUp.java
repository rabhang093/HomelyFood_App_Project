package com.example.android.HomelyFood;

import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class SignUp extends AppCompatActivity {
    MaterialEditText edtPhone,edtPassword,edtName;
    FButton btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtPhone=findViewById(R.id.edtPhone);
        edtName=findViewById(R.id.edtName);
        edtPassword=findViewById(R.id.edtPassword);
        btnSignUp=findViewById(R.id.btnSignUp);

        //init firebase
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=database.getReference("users");
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate
                if(Common.isConnectedToInternet(getBaseContext())){
                    if(!edtPhone.getText().toString().isEmpty() && edtPhone.getText().toString()!=null && !edtName.getText().toString().isEmpty() && edtName.getText().toString()!=null) {
                        final ProgressDialog myDialog=new ProgressDialog(SignUp.this);
                        myDialog.setMessage("Please wait...");
                        myDialog.show();

                        table_user.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //check if phone number already exist in Database
                                if(!dataSnapshot.child(edtPhone.getText().toString()).exists()){
                                    myDialog.dismiss();
                                    User user=new User(edtName.getText().toString(),edtPassword.getText().toString());
                                    table_user.child(edtPhone.getText().toString()).setValue(user);
                                    Toast.makeText(SignUp.this, "Sign Up successfully...!!!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    if(!edtPhone.getText().toString().isEmpty() && edtPhone.getText().toString()!=null && !edtName.getText().toString().isEmpty() && edtName.getText().toString()!=null) {
                                        myDialog.dismiss();
                                        Toast.makeText(SignUp.this, "Phone number already Registered...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }else {
                        Toast.makeText(SignUp.this, "Enter Details to proceed...", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignUp.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }
}
