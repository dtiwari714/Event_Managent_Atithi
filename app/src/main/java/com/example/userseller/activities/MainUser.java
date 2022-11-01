package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userseller.R;
import com.example.userseller.adapters.AdapterOrderUser;
import com.example.userseller.adapters.AdapterShop;
import com.example.userseller.models.ModelOrderUser;
import com.example.userseller.models.ModelShop;
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

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainUser extends AppCompatActivity {

    private TextView nameTv,emailTv,phoneTv,tabShopsTv,tabOrdersTv;
    private ImageButton logoutBtn,editProfileBtn;
    private CircleImageView profilePic;
    private RelativeLayout shopRl,ordersRl;
    private ImageCarousel carousel;
    private RecyclerView shopsRv,ordersRv;
    private CardView banner;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> orderList;
    private AdapterOrderUser adapterOrderUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        initSlider();

        nameTv=findViewById(R.id.nameTv);
        logoutBtn=findViewById(R.id.logoutbtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        profilePic=findViewById(R.id.profilePic);
        ordersRv=findViewById(R.id.ordesRv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        tabShopsTv=findViewById(R.id.tabShopsTv);
        banner=findViewById(R.id.banner);
        shopRl=findViewById(R.id.shopsRl);
        carousel=findViewById(R.id.carousel);
        ordersRl=findViewById(R.id.ordersRl);
        shopsRv=findViewById(R.id.shopsRv);


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth= FirebaseAuth.getInstance();
        checkUser();
        //at the show shops
        showShopsUI();
        showOrdersUI();

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
                //open
                startActivity(new Intent(MainUser.this, ProfileEditUser.class));
            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shop
                showShopsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show oreder
                showOrdersUI();
            }
        });
    }

    private void initSlider() {
        // Java
        ImageCarousel carousel = findViewById(R.id.carousel);

// Register lifecycle. For activity this will be lifecycle/getLifecycle() and for fragments it will be viewLifecycleOwner/getViewLifecycleOwner().
        carousel.registerLifecycle(getLifecycle());

        List<CarouselItem> list = new ArrayList<>();

// Image URL with caption
        list.add(
                new CarouselItem(
                        "https://img.freepik.com/premium-vector/luxury-ornamental-mandala-background-with-arabic-islamic-east-pattern-style-premium_118503-1940.jpg?w=1380",
                        "Photo by Aaron Wu on Unsplash"
                )
        );
        // Just image URL
        list.add(
                new CarouselItem(
                        "https://img.freepik.com/premium-vector/indian-wedding-invitation-card-template_1302-25555.jpg?w=900"
                )
        );
        carousel.setData(list);
    }

    private void showShopsUI() {
        //show shop ui hide the order ui
        shopRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        banner.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.black));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        //show orders ui and hide orders ui
        shopRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        banner.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.white));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                        Toast.makeText(MainUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainUser.this, EmailLogin.class));
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
                            //get user data
                            String Name=""+ds.child("Name").getValue();
                            String Email=""+ds.child("Email").getValue();
                            String Phone=""+ds.child("Phone").getValue();
                            String ProfileImg=""+ds.child("ProfileImg").getValue();
                            String AccountType=""+ds.child("AccountType").getValue();
                            String City=""+ds.child("City").getValue();

                            //set user data
                            nameTv.setText(Name);
                            emailTv.setText(Email);
                            phoneTv.setText(Phone);
                            try {
                                Picasso.get().load(ProfileImg).placeholder(R.drawable.ic_baseline_person_24).into(profilePic);

                            }
                            catch (Exception e){
                                profilePic.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                            //load only those shops that are in the city of user
                            loadShops(City);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        orderList=new ArrayList<>();
        //get orders
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    orderList.clear();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        String uid=""+ds.getRef().getKey();

                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                        ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot ds:snapshot.getChildren()){
                                                ModelOrderUser modelOrderUser=ds.getValue(ModelOrderUser.class);

                                                //add to list
                                                orderList.add(modelOrderUser);
                                            }
                                            //setup adapter
                                            adapterOrderUser=new AdapterOrderUser(MainUser.this,orderList);
                                            //set to recycler view
                                            ordersRv.setAdapter(adapterOrderUser);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String myCity) {
        //init list
        shopsList=new ArrayList<>();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("AccountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelShop modelShop=ds.getValue(ModelShop.class);
                            String shopCity=""+ds.child("City").getValue();
                            //show only user city shops
//                            if (shopCity.equals(myCity)){
//                                shopsList.add(modelShop);
//                            }
                            //if you wnat to display all shops ,skip the if statement and add this
                            shopsList.add(modelShop);
                        }
                        //setup adapter
                        adapterShop=new AdapterShop(MainUser.this,shopsList);
                        //set adapter to recyclerview
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
