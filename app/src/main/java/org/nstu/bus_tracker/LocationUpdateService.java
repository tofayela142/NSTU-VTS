package org.nstu.bus_tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import static android.content.Intent.getIntent;

public class LocationUpdateService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myOwnBusDatabaseReference, allBusDatabaseReference, myOwnPresenceRef;
    private PowerManager.WakeLock wakeLock;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String LICENSE;
    private static final String CHANNEL_ID = "LocationServiceChannel";


    public LocationUpdateService() {
    }

    public void createLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                sendLocationToFirebase(location.getLatitude(), location.getLongitude());
            }
        };
    }
    
    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // Update interval in milliseconds (e.g., 10 seconds)
        locationRequest.setFastestInterval(3000); // Fastest update interval in milliseconds (e.g., 5 seconds)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        createLocationCallback();
        createLocationRequest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString("LICENSE_NO");
                LICENSE = value;
            }
        }
        if(LICENSE==null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        allBusDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE);
        myOwnPresenceRef = mFirebaseDatabase.getReference("users").child(LICENSE).child(Helper.LAST_SEEN);
        myOwnBusDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE).child(LICENSE);
        myOwnBusDatabaseReference.onDisconnect().removeValue();
        requestLocationUpdates();
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NSTU-VTS")
                .setContentText("Tracking location...")
                .setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();
        startForeground(1, notification);
        return START_STICKY;
    }

    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Location Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void sendLocationToFirebase(double latitude,double longitude)
    {
        Log.e("pushing", "to firebase");
        if(myOwnPresenceRef != null) {
            myOwnPresenceRef.setValue(ServerValue.TIMESTAMP);
            myOwnPresenceRef.onDisconnect().removeValue();
        }
        if(myOwnBusDatabaseReference !=null) {
            HashMap map = new HashMap();
            map.put(LICENSE, Helper.formatLatLang(latitude, longitude));
            allBusDatabaseReference.updateChildren(map);
            myOwnBusDatabaseReference.onDisconnect().removeValue();
            Log.e("push service","lat..."+latitude+"......long"+longitude);
        }
        else
            Log.e("is null","myOwnBusDatabaseReference");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        myOwnBusDatabaseReference.removeValue();
        super.onTaskRemoved(rootIntent);
        stopSelf(); // Stop the service when the app is removed from recent tasks
    }

    @Override
    public void onDestroy() {
        if(myOwnBusDatabaseReference!=null) {
            myOwnBusDatabaseReference.removeValue();
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
        wakeLock.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}