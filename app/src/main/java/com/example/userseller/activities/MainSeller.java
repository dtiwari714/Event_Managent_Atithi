package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userseller.adapters.AdapterProductSeller;
import com.example.userseller.Constants;
import com.example.userseller.models.ModelProduct;
import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainSeller extends AppCompatActivity {

    private TextView nameTv,regEmail,shopNameTv,tabProductTv,tabOrdersTv,filteredProductTv;
    private ImageButton logoutBtn,editProfileBtn ,addProductbtn,filterProductBtn;
    private EditText searchProductEt;
    private CircleImageView profilePic;
    private RecyclerView productsRv;
    private RelativeLayout productsRl,ordersRl;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        nameTv=findViewById(R.id.nameTv);
        ordersRl=findViewById(R.id.ordersRl);
        productsRl=findViewById(R.id.productsRl);
        productsRv=findViewById(R.id.productsRv);
        logoutBtn=findViewById(R.id.logoutbtn);
        filteredProductTv=findViewById(R.id.filteredProductTv);
        searchProductEt=findViewById(R.id.searchProductEt);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        tabProductTv=findViewById(R.id.tabProductTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        regEmail=findViewById(R.id.regEmail);
        shopNameTv=findViewById(R.id.shopNameTv);
        addProductbtn=findViewById(R.id.addProductbtn);
        profilePic=findViewById(R.id.profilePic);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();
        showProductsUI();
        showOrdersUI();
        checkUser();
        loadAllProducts();
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                //sign out
                //goto login activity
                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                //startActivity(new Intent(MainSeller.this,ProfileEditSeller.class));
                Intent intent=new Intent(MainSeller.this, ProfileEditSeller.class);
                startActivity(intent);
            }
        });
        addProductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit add product activity
                startActivity(new Intent(MainSeller.this, AddProduct.class ));
            }
        });
        tabProductTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load product
                showProductsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load order
                showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainSeller.this);
                builder.setTitle("Filter Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get select item
                                String selected=Constants.productCategories1[which];
                                filteredProductTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else{
                                    //load filtered
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void loadFilteredProducts(String selected){
        productList=new ArrayList<>();
        //get all products
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){

                            String productCategory=""+ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }
                        //setup adapter
                        adapterProductSeller=new AdapterProductSeller(MainSeller.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList=new ArrayList<>();
        //get all products
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller=new AdapterProductSeller(MainSeller.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductTv.setTextColor(getResources().getColor(R.color.black));
        tabProductTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductTv.setTextColor(getResources().getColor(R.color.white));
        tabProductTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

    }


    private void makeMeOffline() {
        //after loggin in ,make user online
        progressDialog.setMessage("Logging Out...");
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainSeller.this, EmailLogin.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String name=""+ds.child("Name").getValue();
                            String accountType=""+ds.child("AccountType").getValue();
                            String email=""+ds.child("Email").getValue();
                            String shopName=""+ds.child("ShopName").getValue();
                            String profileImage=""+ds.child("ProfileImg").getValue();
                            //nameTv.setText(name+"("+accountType+")");
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            regEmail.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_store_24).into(profilePic);
                            }
                            catch (Exception e)
                            {
                                profilePic.setImageResource(R.drawable.ic_baseline_store_24);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}