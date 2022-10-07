package com.example.userseller.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register_Seller extends AppCompatActivity implements LocationListener {

    private ImageButton selbackBtn, selGps;
    private CircleImageView selPrfilePic;
    private EditText selfullname, selShop, selphone, selshipfree, selcountry;
    private EditText selstate, selcity, selfulladdress, selEmail, selpassword;
    private EditText selConfirmpassword;
    private Button selregister;

    private LocationManager locationManager;
    private double latitude, longitude;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] localPermissions;
    private String[] cameraPermissions;
    private String[] storagePermisson;

    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        selbackBtn = (ImageButton) findViewById(R.id.selregbackBtn);
        selGps = (ImageButton) findViewById(R.id.selgpsLocation);
        selPrfilePic = findViewById(R.id.selprofilePic);
        selfullname = (EditText) findViewById(R.id.selfullname);
        selShop = (EditText) findViewById(R.id.selShop);
        selphone = (EditText) findViewById(R.id.selPhone);
        selshipfree = (EditText) findViewById(R.id.selshipfee);
        selcountry = (EditText) findViewById(R.id.selcountry);
        selstate = (EditText) findViewById(R.id.selstate);
        selcity = (EditText) findViewById(R.id.selcity);
        selfulladdress = (EditText) findViewById(R.id.selfullAddress);
        selEmail = (EditText) findViewById(R.id.selemail);
        selpassword = (EditText) findViewById(R.id.selpassword);
        selConfirmpassword = (EditText) findViewById(R.id.selConfirmpassword);
        selregister = (Button) findViewById(R.id.selregisterButton);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init persmission array
        localPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        selbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        selGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get location of seller
                if (checkLocalPermission()) {
                    detectLocation();
                } else {
                    requestLocalPermission();
                }
            }
        });
        selPrfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set picture of seller
                showImagePickDialog();
            }
        });
        selregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //register user
                inputData();

            }
        });

//        firebaseAuth=FirebaseAuth.getInstance();
//        progressDialog=new ProgressDialog(this);
//        progressDialog.setTitle("Please wait....");
//        progressDialog.setCanceledOnTouchOutside(false);
    }

    private String fullName, ShopName, PhoneNumber, DeliveryFee, Country, State, City, Address, Email, Password, ConfirmPassword;

    private void inputData() {
        fullName = selfullname.getText().toString().trim();
        ShopName = selShop.getText().toString();
        PhoneNumber = selphone.getText().toString().trim();
        DeliveryFee = selshipfree.getText().toString().trim();
        Country = selcountry.getText().toString().trim();
        State = selstate.getText().toString().trim();
        City = selcity.getText().toString().trim();
        Address = selfulladdress.getText().toString().trim();
        Email = selEmail.getText().toString().trim();
        Password = selpassword.getText().toString().trim();
        ConfirmPassword = selConfirmpassword.getText().toString().trim();
        //validate data
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ShopName)) {
            Toast.makeText(this, "Enter shop name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(PhoneNumber)) {
            Toast.makeText(this, "Enter phone number...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(DeliveryFee)) {
            Toast.makeText(this, "Enter delivery charge...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Country)) {
            Toast.makeText(this, "Enter country...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(State)) {
            Toast.makeText(this, "Enter State...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(City)) {
            Toast.makeText(this, "Enter City...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Address)) {
            Toast.makeText(this, "Enter full address...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Password.length() < 6) {
            Toast.makeText(this, "Password must be atleast 6 characters long ...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Password.equals(ConfirmPassword)) {
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();

    }

    private void createAccount() {
        progressDialog.setTitle("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(Email, Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account created
                saverFireBase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failing in creating account
                progressDialog.dismiss();
                Toast.makeText(Register_Seller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

    private void saverFireBase() {
        progressDialog.setTitle("Saving Profile Info");
        if (image_uri == null) {
            //save info without profile pic


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("Email", "" + Email);
            hashMap.put("Name", "" + fullName);
            hashMap.put("ShopName", "" + ShopName);
            hashMap.put("Phone", "" + PhoneNumber);
            hashMap.put("DeliveryFee", "" + DeliveryFee);
            hashMap.put("Country", "" + Country);
            hashMap.put("State", "" + State);
            hashMap.put("City", "" + City);
            hashMap.put("Address", "" + Address);
            hashMap.put("Latitude", "" + latitude);
            hashMap.put("Longitude", "" + longitude);
            hashMap.put("Timestamp", "" + timeStamp);
            hashMap.put("AccountType", "Seller");
            hashMap.put("Online", "true");
            hashMap.put("ShopOpen", "true");
            hashMap.put("ProfileImg", "");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(Register_Seller.this, MainSeller.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(Register_Seller.this, MainSeller.class));
                            finish();
                        }
                    });
        } else {

            //update with profile pic

//            upload image
            String filePathAndName = "profile_images/" + firebaseAuth.getUid();
            //get storage reference
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()) {
                                //image received, now update db
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("Email", "" + Email);
                                hashMap.put("Name", "" + fullName);
                                hashMap.put("ShopName", "" + ShopName);
                                hashMap.put("Phone", "" + PhoneNumber);
                                hashMap.put("DeliveryFee", "" + DeliveryFee);
                                hashMap.put("Country", "" + Country);
                                hashMap.put("State", "" + State);
                                hashMap.put("City", "" + City);
                                hashMap.put("Address", "" + Address);
                                hashMap.put("Latitude", "" + latitude);
                                hashMap.put("Longitude", "" + longitude);
                                hashMap.put("Timestamp", "" + timeStamp);
                                hashMap.put("AccountType", "Seller");
                                hashMap.put("Online", "true");
                                hashMap.put("ShopOpen", "true");
                                hashMap.put("ProfileImg", "" + downloadImageUri);

                                //update to database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(Register_Seller.this, "User Registered successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(Register_Seller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to upload image
                            progressDialog.dismiss();
                            Toast.makeText(Register_Seller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] option={"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image:")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //allowed,open camera
                                pickFromCamera();
                            }
                            else{
                                //not allowed,request
                                requestPermissionCamera();
                            }
                        }
                        else{
                            //gallery clicked
                            if (checkStoragePermission()){
                                //allowed,open gallery
                                pickFromGallery();
                            }
                            else{
                                //not allowed,request
                                requestPermissionStorage();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromCamera() {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image Description");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            String state = addresses.get(0).getAdminArea();

            selcity.setText(city);
            selcountry.setText(country);
            selstate.setText(state);
            selfulladdress.setText(address);
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkLocalPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocalPermission(){
        ActivityCompat.requestPermissions(this,localPermissions,LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestPermissionStorage(){
        ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return (result && result1);
    }

    private void requestPermissionCamera(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please Turn on location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean localAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(localAccepted){
                        detectLocation();
                    }
                    else{
                        Toast.makeText(this, "Please allow location", Toast.LENGTH_SHORT).show();
                    }
                }
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //pickFromCamera()
                        pickFromCamera();
                    }
                    else{
                        //permissoion denied
                        Toast.makeText(this, "Camera permission are necessary..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //pickFromCamera()
                        pickFromGallery();
                    }
                    else{
                        //permissoion denied
                        Toast.makeText(this, "Storage permission is necessary..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle image pick result
//        if (resultCode==RESULT_OK){
//            if (requestCode==IMAGE_PICK_CAMERA_CODE){
//                //picked from gallery
//                image_uri=data.getData();
//                //set to image view
//                selPrfilePic.setImageURI(image_uri);
//            }
//            else if (requestCode==IMAGE_PICK_CAMERA_CODE){
//                selPrfilePic.setImageURI(image_uri);
//            }
//        }
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                selPrfilePic.setImageURI(image_uri);
            }
        }
    }

}