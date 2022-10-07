package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private ImageButton back;
    private Button Recover;
    private EditText Email;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();


        back=findViewById(R.id.back);
        Recover=findViewById(R.id.gotorecover);
        Email=findViewById(R.id.email);
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back=new Intent(ForgotPassword.this, EmailLogin.class);
                startActivity(back);
            }
        });
        Recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
    }
    private String emailet;
    private void recoverPassword() {
        emailet=Email.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(emailet).matches()){
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Sending instruction to reset password");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(emailet)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //instruction sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this, "Password Reset instruction sent to your email", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed sending instruction
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}