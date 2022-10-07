package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EmailLogin extends AppCompatActivity {
    private EditText email,password;
    private TextView forgot;
    private Button btnlogin;
    LottieAnimationView login;
    private Button register;
    private ImageButton back;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        back=findViewById(R.id.back);
        forgot=findViewById(R.id.forgot);
        btnlogin=findViewById(R.id.gotologin);
        register=findViewById(R.id.gotoregister);
        login=findViewById(R.id.loginanimation);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.playAnimation();
            }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgot=new Intent(EmailLogin.this, ForgotPassword.class);
                startActivity(forgot);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register=new Intent(EmailLogin.this, Register_User.class);
                startActivity(register);
            }
        });
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        }
    private String emailet,passwordet;
    private void loginUser() {
        emailet=email.getText().toString().trim();
        passwordet=password.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(emailet).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordet)){
            Toast.makeText(this, "Enter password..", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Log in....");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(emailet,passwordet)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                            //logged succesfully
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed login
                        progressDialog.dismiss();
                        Toast.makeText(EmailLogin.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void makeMeOnline() {
        //after loggin in ,make user online
        progressDialog.setMessage("Checking User...");
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("online","true");
        
        //update value to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EmailLogin.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUserType() {
        //if user is seller,start seller main screen
        //if user is buyer,start user main screen
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String accountType=""+ds.child("AccountType").getValue();
                            if (accountType.equals("Seller")){
                                progressDialog.dismiss();
                                //user is seller
                                startActivity(new Intent(EmailLogin.this, MainSeller.class));
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                //user is buyer
                                startActivity(new Intent(EmailLogin.this, MainUser.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}