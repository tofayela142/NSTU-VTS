/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.nstu.bus_tracker;

 import android.Manifest;
 import android.content.pm.PackageManager;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.annotation.NonNull;
 import android.support.v4.app.ActivityCompat;
 import android.support.v4.content.ContextCompat;
 import android.support.v7.app.AppCompatActivity;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
 import com.google.android.gms.maps.OnMapReadyCallback;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.firebase.database.ChildEventListener;
 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.ValueEventListener;
 
 
 
 
 public class MyLocationDemoActivity extends AppCompatActivity
         implements
         OnMyLocationButtonClickListener,
         OnMyLocationClickListener,
         OnMapReadyCallback,
         ActivityCompat.OnRequestPermissionsResultCallback {
 
     /**
      * Request code for location permission request.
      *
      * @see #onRequestPermissionsResult(int, String[], int[])
      */
     private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
 
     /**
      * Flag indicating whether a requested permission has been denied after returning in
      * {@link #onRequestPermissionsResult(int, String[], int[])}.
      */
     private boolean mPermissionDenied = false;
 
     private GoogleMap mMap;
 
     private Marker mBrisbane;
     private static  LatLng BUS = null;
     Location mlocation;
 
 
 
     // Firebase instance variables
     private FirebaseDatabase mFirebaseDatabase;
     private DatabaseReference mMessagesDatabaseReference;
     private ChildEventListener mChildEventListener;
 
     static int plus=0;
     void initFirebase()
     {
 
         // Initialize Firebase components
         mFirebaseDatabase = FirebaseDatabase.getInstance();
 
         mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE);
 
 
         ValueEventListener valueEventListener=new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
 
                 //Pair<Double,Double>pos=Helper.extractLatLang(dataSnapshot.getValue(String.class));
                 //updateBusMarker(pos.first,pos.second);
                 Log.e("onDataChange",dataSnapshot.toString());
             }
 
             @Override
             public void onCancelled(DatabaseError databaseError) {
 
             }
         } ;
         mMessagesDatabaseReference.addValueEventListener(valueEventListener);
         //mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
     }
 
 
     void sendLocationToFirebase(double latitude,double longitude)
     {
         if(mMessagesDatabaseReference!=null) {
            //String val=latitude + "," + longitude;
            mMessagesDatabaseReference.setValue(Helper.formatLatLang(latitude,longitude));
            Log.e("push","lat..."+latitude+"......long"+longitude);
 
         }
         else
             Log.e("is null","mMessagesDatabaseReference");
     }
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.my_location_demo);
 
         initFirebase();
 
 
         SupportMapFragment mapFragment =
                 (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
         mapFragment.getMapAsync(this);
 
 
     }
 
     @Override
     public void onMapReady(GoogleMap map) {
         mMap = map;
 
         mMap.setOnMyLocationButtonClickListener(this);
         mMap.setOnMyLocationClickListener(this);
         enableMyLocation();
     }
 
     /**
      * Enables the My Location layer if the fine location permission has been granted.
      */
     private void enableMyLocation() {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                 != PackageManager.PERMISSION_GRANTED) {
             // Permission to access the location is missing.
             PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                     Manifest.permission.ACCESS_FINE_LOCATION, true);
         } else if (mMap != null) {
             // Access to the location has been granted to the app.
             mMap.setMyLocationEnabled(true);
         }
     }
 
     @Override
     public boolean onMyLocationButtonClick() {
         Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
         // Return false so that we don't consume the event and the default behavior still occurs
         // (the camera animates to the user's current position).
 
 
        // updatePackageLoc();
         return false;
     }
 
     private void updateBusMarker(double lat,double lang)
     {
         if(mMap==null)
             return;
         BUS=new LatLng( lat,lang);
         if(mBrisbane!=null)
             mBrisbane.setPosition(BUS);
         else
             mBrisbane = mMap.addMarker(new MarkerOptions().position(BUS)
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker)));
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(BUS,17f));
 
     }
 
 
     @Override
     public void onMyLocationClick(@NonNull Location location) {
 
         mlocation=location;
         Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
     }
 
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
             @NonNull int[] grantResults) {
         if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
             return;
         }
 
         if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                 Manifest.permission.ACCESS_FINE_LOCATION)) {
             // Enable the my location layer if the permission has been granted.
             enableMyLocation();
         } else {
             // Display the missing permission error dialog when the fragments resume.
             mPermissionDenied = true;
         }
     }
 
     @Override
     protected void onResumeFragments() {
         super.onResumeFragments();
         if (mPermissionDenied) {
             // Permission was not granted, display error dialog.
             showMissingPermissionError();
             mPermissionDenied = false;
         }
     }
 
     /**
      * Displays a dialog with error message explaining that the location permission is missing.
      */
     private void showMissingPermissionError() {
         PermissionUtils.PermissionDeniedDialog
                 .newInstance(true).show(getSupportFragmentManager(), "dialog");
     }
 
 }
 