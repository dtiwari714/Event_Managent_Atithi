package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userseller.Constants;
import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProduct extends AppCompatActivity {

    private String productId;
    private ImageButton backbtn;
    private CircleImageView productIconIv;
    private EditText titleEt,descriptionEt;
    private TextView categoryEt,discountedNoteEt,discountedPriceEt,quntityEt,priceEt;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;

    //permissin contants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constansts
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        //get id of the product from intent
        productId=getIntent().getStringExtra("productId");
        backbtn=findViewById(R.id.backbtn);
        productIconIv=findViewById(R.id.productIconIv);
        titleEt=findViewById(R.id.titleEt);
        descriptionEt=findViewById(R.id.descriptionEt);
        categoryEt=findViewById(R.id.categoryEt);
        quntityEt=findViewById(R.id.quntityEt);
        priceEt=findViewById(R.id.priceEt);
        discountedPriceEt=findViewById(R.id.discountedPriceEt);
        discountedNoteEt=findViewById(R.id.discountedNoteEt);
        discountSwitch=findViewById(R.id.discountSwitch);
        updateProductBtn=findViewById(R.id.updateProductBtn);

        //on start is unchecked,so hide discountpriceEt,discoutnoteEt
        discountedNoteEt.setVisibility(View.GONE);
        discountedPriceEt.setVisibility(View.GONE);

        firebaseAuth=FirebaseAuth.getInstance();
        loadProductDetails();//to set on views

        //setup progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });
        categoryEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }
        });
        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                //1)Input Data
                //2)Validate data
                //3)Update data to db
                inputData();
            }
        });
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //checked show discountprice ,discountNoteet
                    discountedNoteEt.setVisibility(View.VISIBLE);
                    discountedPriceEt.setVisibility(View.VISIBLE);
                }
                else{
                    //unchecked ,hide discountPriceEt,discountNoteEt
                    discountedNoteEt.setVisibility(View.GONE);
                    discountedPriceEt.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String productId=""+snapshot.child("productId").getValue();
                        String productTitle=""+snapshot.child("productTitle").getValue();
                        String productDescription=""+snapshot.child("productDescription").getValue();
                        String productCategory=""+snapshot.child("productCategory").getValue();
                        String productQuantity=""+snapshot.child("productQuantity").getValue();
                        String productIcon=""+snapshot.child("productIcon").getValue();
                        String originalPrice=""+snapshot.child("originalPrice").getValue();
                        String discountPrice=""+snapshot.child("discountPrice").getValue();
                        String discountNote=""+snapshot.child("discountNote").getValue();
                        String discountAvailable=""+snapshot.child("discountAvailable").getValue();
                        String timestamp=""+snapshot.child("timestamp").getValue();
                        String uid=""+snapshot.child("uid").getValue();

                        //set data to view
                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);
                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        }
                        else{
                            discountSwitch.setChecked(false);
                            discountedPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);
                        }
                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        categoryEt.setText(productCategory);
                        discountedNoteEt.setText(discountNote);
                        quntityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountPrice);

                        try{
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(productIconIv);

                        }
                        catch (Exception e){
                            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private boolean discountAvailable=false;

    private void inputData() {
        //1)Input Data
        productTitle=titleEt.getText().toString().trim();
        productDescription=descriptionEt.getText().toString().trim();
        productCategory=categoryEt.getText().toString().trim();
        productQuantity=quntityEt.getText().toString().trim();
        originalPrice=priceEt.getText().toString().trim();
        discountAvailable=discountSwitch.isChecked();

        //validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            //product is with discount
            discountPrice=discountedPriceEt.getText().toString().trim();
            discountNote=discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount Price is required...", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            //product is without discount
            discountPrice="0";
            discountNote="";
        }
        updateProduct();
    }

    private void updateProduct() {
        //show progress
        progressDialog.setMessage("Updating product...");
        progressDialog.show();
        if (image_uri==null){
            //update without image
            //setup data in hashmap to update
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);

            //update to db
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProduct.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else{
            //update with image
            //first upload image
            //image name and paath on firebase storage
            String filePathAndName="product_images/"+""+productId;//overide previous image using same id
            //uploaded image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded ,get url of uploaded image
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri=uriTask.getResult();
                            if (uriTask.isSuccessful()){
                                //setup data in hashmap to update
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("productIcon",""+downloadImageUri);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailable);

                                //update to db
                                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //update failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProduct.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProduct.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category=Constants.productCategories[which];
                        //set picked category
                        categoryEt.setText(category);
                    }
                })
                .show();
    }
    private void showImagePickDialog() {
        //option to display in dialog
        String[] options={"camera","Gallery"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()) {
                                //permission granted
                                pickFromCamera();
                            }
                            else{
                                requestCameraPermission();
                            }
                        }
                        else{
                            //gallery clicked
                            if (checkStoragePermission()){
                                //permission grantaed
                                pickFromGallery();
                            }
                            else{
                                //permission not granted
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();

    }
    private void pickFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        //intent to pick image from camera
        //using media store to pick high/original quality image
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }
    //handle permission results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        //both or one of permisson denied
                        Toast.makeText(this, "Camera & Storage permission are required....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage permissions is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        //super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode,int resultCode,@NonNull Intent data){
//        if (resultCode==RESULT_OK){
//            if (resultCode==IMAGE_PICK_GALLERY_CODE){
//                //image picked from gallery
//                //save picked image url
//                image_uri=data.getData();
//                //set image
//                productIconIv.setImageURI(image_uri);
//            }
//            else if (resultCode==IMAGE_PICK_CAMERA_CODE){
//                //image picked from camera
//                productIconIv.setImageURI(image_uri);
//            }
//        }
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}