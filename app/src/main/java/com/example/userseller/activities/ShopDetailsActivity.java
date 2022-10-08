package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userseller.Constants;
import com.example.userseller.R;
import com.example.userseller.adapters.AdapterCartItem;
import com.example.userseller.adapters.AdapterProductUser;
import com.example.userseller.models.ModelCartItem;
import com.example.userseller.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv,filterProductBtn;
    private TextView shopNameTv,phoneTv,emailTv,openClosedTv,deliveryFeeTv,
            addressTv,filteredProductTv;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    public String deliveryFee;

    private String shopUid;
    private String myLatitude,myLongitude;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;


    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        //init ui views
        shopIv=findViewById(R.id.shopIv);
        shopNameTv=findViewById(R.id.shopNameTv);
        phoneTv=findViewById(R.id.phoneTv);
        emailTv=findViewById(R.id.emailTv);
        openClosedTv=findViewById(R.id.openClosedTv);
        deliveryFeeTv=findViewById(R.id.deliveryFeeTv);
        addressTv=findViewById(R.id.addressTv);
        callBtn=findViewById(R.id.callBtn);
        mapBtn=findViewById(R.id.mapBtn);
        cartBtn=findViewById(R.id.cartBtn);
        backBtn=findViewById(R.id.backBtn);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        searchProductEt=findViewById(R.id.searchProductEt);
        productsRv=findViewById(R.id.productsRv);

        //get uid of the shop from intent
        shopUid=getIntent().getStringExtra("shopUid");
        firebaseAuth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        /*each shop have its own producct and their and order so if user add items to cart and go back
        * and open cart in differnt shop should be differnt*/
        //so delte cart whenever user open this activity
        deleteCartData();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get select item
                                String selected=Constants.productCategories1[which];
                                filteredProductTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }
                                else{
                                    //load filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"} ))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"} ))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();//delte all record from cart
    }

    public double allTotalPrice=0.00;
    //need to access these cies in adapter so making
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;

    private void showCartDialog() {

        //init list
        cartItemList=new ArrayList<>();
        final String timestamp=""+System.currentTimeMillis();


        //infalte cart layout
        View view= LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init value
        TextView shopNameTv=view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv=view.findViewById(R.id.cartItemsRv);
        sTotalTv=view.findViewById(R.id.sTotalTv);
        dFeeTv=view.findViewById(R.id.dFeeTv);
        allTotalPriceTv=view.findViewById(R.id.totalTv);
        shopName=shopNameTv.getText().toString();
        Button checkoutBtn=view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"} ))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"} ))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"} ))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem(""+id,""+pId,""+name,""+price,""+cost,""+quantity);
            cartItemList.add(modelCartItem);
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set to recyclerview
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));

        //reset total price on dialog dismiss
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    allTotalPrice=0.00;
                }
            });


    }

    private void openMap() {
//        String address="https//:maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
//        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(address));
//        startActivity(intent);
        String address = "https//:maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr"+shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_PICK, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadShopProducts() {
        //init list
        productsList=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            //clear list before adding items
                            ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser=new AdapterProductUser(ShopDetailsActivity.this,productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                    String Name=""+snapshot.child("Name").getValue();
                    shopName=""+snapshot.child("shopName").getValue();
                    shopEmail=""+snapshot.child("Email").getValue();
                    shopPhone=""+snapshot.child("Phone").getValue();
                    shopLatitude=""+snapshot.child("Latitude").getValue();
                    shopLongitude=""+snapshot.child("Longitude").getValue();
                    shopAddress=""+snapshot.child("Address").getValue();
                    deliveryFee=""+snapshot.child("DeliveryFee").getValue();
                    String profileImage=""+snapshot.child("ProfileImg").getValue();
                    String shopOpen=""+snapshot.child("ShopOpen").getValue();
                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("DeliveryFee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")){
                    openClosedTv.setText("Open");
                }
                else{
                    openClosedTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){

                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

        }
        });
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
                            myLatitude=""+ds.child("Latitude").getValue();
                            myLongitude=""+ds.child("Longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}