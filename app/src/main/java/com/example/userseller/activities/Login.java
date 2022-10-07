package com.example.userseller.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.userseller.R;

public class Login extends AppCompatActivity {
    private Button login,signup;
    //private Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        login=findViewById(R.id.gotologin);
        signup=findViewById(R.id.gotoregister);
        //signup=findViewById(R.id.btnregister);
        login.setOnClickListener(v -> {
            Intent i=new Intent(Login.this, EmailLogin.class);
            startActivity(i);
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register=new Intent(Login.this, Register_Seller.class);
                startActivity(register);
            }
        });
    }
}