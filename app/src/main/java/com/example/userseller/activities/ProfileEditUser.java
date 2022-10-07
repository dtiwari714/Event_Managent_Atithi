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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.userseller.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

 public class ProfileEditUser extends AppCompatActivity implements LocationListener {
    private ImageButton selregbackBtn,selgpsLocation;
    private CircleImageView selprofilePic;
    private EditText fullname,regphonecall,selcountry,selcity,selstate,selfullAddress;
    private Button updateButton;

     //permission constants
     private static final int LOCATION_REQUEST_CODE = 100;
     private static final int CAMERA_REQUEST_CODE = 200;
     private static final int STORAGE_REQUEST_CODE = 300;
     //image pick constant
     private static final int IMAGE_PICK_GALLERY_CODE = 400;
     private static final int IMAGE_PICK_CAMERA_CODE = 500;

     //permission arrays
     private String[] localPermissions;
     private String[] cameraPermissions;
     private String[] storagePermisson;

     //image uri
     private Uri image_uri;

     private double latitude=0.0;
     private double longitude=0.0;

     //progress dialog
     private ProgressDialog progressDialog;
     //firebase auth
     private FirebaseAuth firebaseAuth;
     private LocationManager locationManager;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_user);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        selregbackBtn=findViewById(R.id.regbackBtn);
        selgpsLocation=findViewById(R.id.gpsLocation);
        selprofilePic=findViewById(R.id.profilePic);
        fullname=findViewById(R.id.fullname);
        regphonecall=findViewById(R.id.regphonecall);
        selcountry=findViewById(R.id.country);
        selcity=findViewById(R.id.city);
        selstate=findViewById(R.id.state);
        selfullAddress=findViewById(R.id.fullAddress);
        updateButton=findViewById(R.id.updateButton);

         //init permission array
         localPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
         cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
         storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

         //setup progress dialog
         progressDialog = new ProgressDialog(this);
         progressDialog.setTitle("Please Wait");
         progressDialog.setCanceledOnTouchOutside(false);

         firebaseAuth = FirebaseAuth.getInstance();
         checkUser();

         selregbackBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 onBackPressed();
             }
         });
         selgpsLocation.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //detect loaction
                 if (checkLocationPermission()) {
                     //aready allowed
                     detectLocation();
                 } else {
                     //not allowed,request
                     requestLocationPermission();
                 }
             }
         });
         updateButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //begin update
                 inputData();
             }
         });
         selprofilePic.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //pick image
                 showImagePickDialog();
             }
         });
    }
     private String fullName, PhoneNumber, Country, State, City, Address;
     //boolean shopOpen;
     private void inputData() {
         fullName = fullname.getText().toString().trim();
         PhoneNumber = regphonecall.getText().toString().trim();
         Country = selcountry.getText().toString().trim();
         State = selstate.getText().toString().trim();
         City = selcity.getText().toString().trim();
         Address = selfullAddress.getText().toString().trim();
         //shopOpen=shopOpenSwitch.isChecked();//true or false
         updateProfile();
     }

     private void updateProfile() {
         progressDialog.setMessage("Updating Profile....");
         progressDialog.show();
         if (image_uri==null){
             //update without image
             //setup data to update
             HashMap<String,Object> hashMap=new HashMap<>();
             hashMap.put("Name",""+fullName);
             //hashMap.put("shopName",""+selShop);
             hashMap.put("Phone",""+regphonecall);
             //hashMap.put("DeliveryFee",""+selshipfee);
             hashMap.put("Country",""+selcountry);
             hashMap.put("State",""+selstate);
             hashMap.put("City",""+selcity);
             hashMap.put("Address",""+selfullAddress);
             hashMap.put("Latitude",""+latitude);
             hashMap.put("Longitude",""+longitude);
             //hashMap.put("ShopOpen",""+shopOpen);
             //update to db
             DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
             ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void unused) {
                             //updated
                             progressDialog.dismiss();
                             Toast.makeText(ProfileEditUser.this, "Profile Updated....", Toast.LENGTH_SHORT).show();

                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             //failed to update
                             progressDialog.dismiss();
                             Toast.makeText(ProfileEditUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });
         }
         else{
             //update with image
             /*--------------Upload image First----------------*/
             String filePathAndName="profile_image/"+""+firebaseAuth.getUid();
             //get storage reference
             StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
             storageReference.putFile(image_uri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             //image upload,get url of uploaded image
                             Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                             while (!uriTask.isSuccessful());
                             Uri downloadImageUri = uriTask.getResult();
                             //image uri recieved ,now update db
                                 //setup data to update
                                 if (uriTask.isSuccessful()){
                                 HashMap<String,Object>hashMap=new HashMap<>();
                                 hashMap.put("Name",""+fullName);
                                 //hashMap.put("shopName",""+selShop);
                                 hashMap.put("Phone",""+regphonecall);
                                 //hashMap.put("DeliveryFee",""+selshipfee);
                                 hashMap.put("Country",""+selcountry);
                                 hashMap.put("State",""+selstate);
                                 hashMap.put("City",""+selcity);
                                 hashMap.put("Address",""+selfullAddress);
                                 hashMap.put("Latitude",""+latitude);
                                 hashMap.put("Longitude",""+longitude);
                                 //hashMap.put("ShopOpen",""+shopOpen);
                                 hashMap.put("ProfileImg",""+downloadImageUri);
                                 //update to db
                                 DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                                 ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                             @Override
                                             public void onSuccess(Void unused) {
                                                 //updated
                                                 progressDialog.dismiss();
                                                 Toast.makeText(ProfileEditUser.this, "Profile Updated....", Toast.LENGTH_SHORT).show();

                                             }
                                         })
                                         .addOnFailureListener(new OnFailureListener() {
                                             @Override
                                             public void onFailure(@NonNull Exception e) {
                                                 //failed to update
                                                 progressDialog.dismiss();
                                                 Toast.makeText(ProfileEditUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                             }
                                         });
                             }
                         }

                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             progressDialog.dismiss();
//                            Toast.makeText(ProfileEditSeller.this,""+e.getMessage().Toast.LENGTH_SHORT).show();
                             Toast.makeText(ProfileEditUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });
         }
     }

     private void checkUser() {
         FirebaseUser user=firebaseAuth.getCurrentUser();
         if (user==null){
             startActivity(new Intent(getApplicationContext(), EmailLogin.class));
             finish();
         }
         else{
             loadMyInfo();
         }
     }

     private void loadMyInfo() {
         //load user info,and set to views
         DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
         ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         for (DataSnapshot ds:snapshot.getChildren()){
                             String AccountType=""+ds.child("AccountType").getValue();
                             String Address=""+ds.child("Address").getValue();
                             String City=""+ds.child("City").getValue();
                             String State=""+ds.child("State").getValue();
                             String Country=""+ds.child("Country").getValue();
                             //String DeliveryFee=""+ds.child("DeliveryFee").getValue();
                             String Email=""+ds.child("Email").getValue();
                             double Latitude = Double.parseDouble("" + ds.child("Latitude").getValue());
                             double Longitude=Double.parseDouble(""+ds.child("Longitude").getValue());
                             String Name=""+ds.child("Name").getValue();
                             //String Online=""+ds.child("Online").getValue();
                             String Phone=""+ds.child("Phone").getValue();
                             String ProfileImg=""+ds.child("ProfileImg").getValue();
                             String Timestamp=""+ds.child("Timestamp").getValue();
                             String uid=""+ds.child("uid").getValue();

                             fullname.setText(Name);
                             selcountry.setText(Country);
                             regphonecall.setText(Phone);
                             selstate.setText(State);
                             selcity.setText(City);
                             selfullAddress.setText(Address);

                             try {
                                 Picasso.get().load(ProfileImg).placeholder(R.drawable.ic_baseline_person_24).into(selprofilePic);
                             }
                             catch (Exception e){
                                 selprofilePic.setImageResource(R.drawable.ic_baseline_person_24);
                             }

                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
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
                                 requestCameraPermission();
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
                                 requestStoragePermission();
                             }
                         }
                     }
                 })
                 .show();
     }
     private void requestStoragePermission() {
         ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);

     }
     private void requestCameraPermission() {
         ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);

     }
     private void pickFromGallery() {
         //intent to pick image gallery
         Intent intent=new Intent(Intent.ACTION_PICK);
         intent.setType("image/*");
         //intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
     }
     private void pickFromCamera() {
         //intent to pick image gallery
         ContentValues contentValues=new ContentValues();
         contentValues.put(MediaStore.Images.Media.TITLE,"Image Title");
         contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image Description");
         image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
         Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
         startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

     }
     private boolean checkStoragePermission() {
         boolean result= ContextCompat.checkSelfPermission(this,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                 (PackageManager.PERMISSION_GRANTED);
         return result;
     }
     private boolean checkCameraPermission() {
         boolean result=ContextCompat.checkSelfPermission(this,
                 Manifest.permission.CAMERA)==
                 (PackageManager.PERMISSION_GRANTED);
         boolean result1=ContextCompat.checkSelfPermission(this,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                 (PackageManager.PERMISSION_GRANTED);
         return result && result1;
     }
     private boolean checkLocationPermission() {
         boolean result = ContextCompat.checkSelfPermission(this,
                 Manifest.permission.ACCESS_FINE_LOCATION)==
                 (PackageManager.PERMISSION_GRANTED);
         return result;
     }
     private void requestLocationPermission() {
         ActivityCompat.requestPermissions(this,localPermissions,LOCATION_REQUEST_CODE);
     }
     private void findAddress() {
         //find address ,country state,city
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
             selfullAddress.setText(address);

         }
         catch (Exception e){
             Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
         }
     }
     private void detectLocation() {
         Toast.makeText(this, "Please Wait.....", Toast.LENGTH_SHORT).show();
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


     @Override
     public void onLocationChanged(@NonNull Location location) {
         latitude=location.getLatitude();
         longitude=location.getLongitude();
         findAddress();
     }

     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         LocationListener.super.onStatusChanged(provider, status, extras);
     }

     @Override
     public void onProviderEnabled(@NonNull String provider) {
         LocationListener.super.onProviderEnabled(provider);
     }

     @Override
     public void onProviderDisabled(@NonNull String provider) {
         Toast.makeText(this, "Location is disables.....", Toast.LENGTH_SHORT).show();
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
                         Toast.makeText(this, "Location permission is necessaary....", Toast.LENGTH_SHORT).show();
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
//         //handle image pick result
//         if (resultCode==RESULT_OK){
//             if (requestCode==IMAGE_PICK_CAMERA_CODE){
//                 //picked from gallery
//                 image_uri=data.getData();
//                 //set to image view
//                 selprofilePic.setImageURI(image_uri);
//             }
//             else if (requestCode==IMAGE_PICK_CAMERA_CODE){
//                 selprofilePic.setImageURI(image_uri);
//             }
//         }
         if (resultCode==RESULT_OK){
             if (requestCode==IMAGE_PICK_GALLERY_CODE){
                 image_uri=data.getData();
                 selprofilePic.setImageURI(image_uri);
             }
         }

         super.onActivityResult(requestCode, resultCode, data);
     }
}