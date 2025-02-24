package org.nstu.bus_tracker;
....................................................................................................

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Text;


public class MapActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {


    private String DRIVER_ID;
    private String LICENSE;
    private boolean isFirstLocationUpdate = true;
    private boolean backPressed = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private Timer timer;
    HashMap<String, Marker> busMarkerTable;
    Location mlocation;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myOwnBusDatabaseReference, allBusDatabaseReference, myOwnPresenceRef, allPresenceRef;

    private static final String TAG = MapActivity.class.getSimpleName();


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;


    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";


    private FusedLocationProviderClient mFusedLocationClient;


    private SettingsClient mSettingsClient;


    private LocationRequest mLocationRequest;


    private LocationSettingsRequest mLocationSettingsRequest;


    private LocationCallback mLocationCallback;


    private Location mCurrentLocation;

    // UI Widgets.
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private ImageButton mReloadButton;

    // Labels.
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;


    private Boolean mRequestingLocationUpdates;


    private String mLastUpdateTime;


    void initFirebaseDB() {

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        allPresenceRef = mFirebaseDatabase.getReference("users");
        allBusDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE);
        try{
            myOwnPresenceRef = mFirebaseDatabase.getReference("users").child(LICENSE).child(Helper.LAST_SEEN);
        } catch (Exception e) {
            myOwnPresenceRef = null;
        }
        try {
            myOwnBusDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE).child(LICENSE);
        } catch (Exception e) {
            myOwnBusDatabaseReference = null;
        }

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Log.e("onDataChange", dataSnapshot.toString());

                if (key.equals(DRIVER_ID)) {
                    Log.e("equal", "driver key " + key);
                    return;
                }
                try {
                    Pair<Double, Double> pos = Helper.extractLatLang(dataSnapshot.getValue(String.class));
                    updateBusMarker(key, pos.first, pos.second);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Log.e("onChildAdded", dataSnapshot.toString());

                Pair<Double, Double> pos = Helper.extractLatLang(dataSnapshot.getValue(String.class));
                //updateBusMarker(pos.first,pos.second);
                if (key.equals(LICENSE))
                    Log.e("onChildAdded", "my self");

                else {
                    updateBusMarker(key, pos.first, pos.second);
                    Log.e("onChildAdded", "others");
                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.e("onChildChanged", dataSnapshot.toString());
                String key = dataSnapshot.getKey();

                Pair<Double, Double> pos = Helper.extractLatLang(dataSnapshot.getValue(String.class));
                //updateBusMarker(pos.first,pos.second);
                if (key.equals(LICENSE))
                    Log.e("onChildAdded", "my self");

                else {
                    updateBusMarker(key, pos.first, pos.second);
                    Log.e("onChildAdded", "others");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Log.e("onChildRemoved", dataSnapshot.toString());

                if (key.equals(DRIVER_ID))
                    Log.e("onChildRemoved", "my self");

                else {
                    if (busMarkerTable.containsKey(key)) {
                        busMarkerTable.get(key).remove();
                        busMarkerTable.remove(key);
                    }
                    Log.e("onChildRemoved", "others");
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (myOwnBusDatabaseReference != null) {
            myOwnBusDatabaseReference.addValueEventListener(valueEventListener);
        }
        allBusDatabaseReference.addChildEventListener(childEventListener);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                allPresenceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                Long lastSeenTimestamp = childSnapshot.child(Helper.LAST_SEEN).getValue(Long.class);
                                if (lastSeenTimestamp != null) {
                                    Long currentTime = System.currentTimeMillis();
                                    long offlineThreshold = 5000; // 5 minutes (adjust as needed)443,482
                                    if (currentTime - lastSeenTimestamp > offlineThreshold) {
                                        Log.e("currently offline", "offline");
                                        FirebaseDatabase.getInstance().getReference().child(Helper.CHILD_NAME_FIREBASE).child(childSnapshot.getKey().toString()).removeValue();
                                    } else {
                                        Log.e("currently offline", "online");
                                    }
                                }
                            }
                        }
                    }
        
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database error
                    }
                });
            }
        };
        timer.schedule(task, 0, 6000);
    }

    private Bitmap createStoreMarker(String id) {
        View markerLayout = getLayoutInflater().inflate(R.layout.bus_marker_layout, null);

        ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView markerRating = (TextView) markerLayout.findViewById(R.id.marker_text);
        markerImage.setImageResource(R.mipmap.ic_launcher);
        markerRating.setText("Bus Name: " + id);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    private void updateBusMarker(String id, double lat, double lang) {
        if (mMap == null)
            return;
        LatLng busLatlong = new LatLng(lat, lang);
        Marker mMarker = null;

        if (busMarkerTable.containsKey(id))
            mMarker = busMarkerTable.get(id);

        if (mMarker != null)
            mMarker.setPosition(busLatlong);

        else {
            mMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(busLatlong)
                            .icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(id)))
                            .title("Bus: " + id)
                            .anchor(0.5f, 0.5f)
            );
            busMarkerTable.put(id, mMarker);
        }
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busLatlong,17f));

    }




    boolean is_this_a_driver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity);

        LinearLayout debug_values = findViewById(R.id.debug_values);
        LinearLayout reload_layout = findViewById(R.id.reload_layout);

        busMarkerTable = new HashMap<>();
        DRIVER_ID = null;
        LICENSE = null;
        if (getIntent().getExtras() != null)
            DRIVER_ID = getIntent().getExtras().getString("DRIVER_ID");

        if (getIntent().getExtras() != null)
            LICENSE = getIntent().getExtras().getString("LICENSE_NO");

        if (getIntent().getExtras() != null)
            Log.e("driver", String.valueOf(getIntent().getExtras().getBoolean("is_this_a_driver", false)));
        is_this_a_driver = getIntent().getExtras().getBoolean("is_this_a_driver", false);

        if (!is_this_a_driver) {
            if (myOwnBusDatabaseReference != null) {
                myOwnBusDatabaseReference.removeValue();
            }
            debug_values.setVisibility(View.GONE);
            reload_layout.setVisibility(View.VISIBLE);
        } else {
            debug_values.setVisibility(View.VISIBLE);
            reload_layout.setVisibility(View.GONE);
        }
        initMAP();
        initFirebaseDB();


        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mReloadButton = (ImageButton) findViewById(R.id.reload);

        TextView bus_id = findViewById(R.id.bus_no);
        TextView license_no = findViewById(R.id.license_plate_no);
        bus_id.setText("Driver Username: " + DRIVER_ID);
        license_no.setText("License No: " + LICENSE);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.


        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRequestingLocationUpdates && checkPermissions()) {
                    initFirebaseDB();
                    Toast toast = Toast.makeText(MapActivity.this, "Bus Locations Reloaded", Toast.LENGTH_LONG);
                    View t_view = toast.getView();
                    t_view.getBackground().setColorFilter(getResources().getColor(R.color.submit_button), PorterDuff.Mode.SRC_IN);
                    TextView text = t_view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.colorSwitchColor));
                    toast.show();
                } else if (!checkPermissions()) {
                    requestPermissions();
                }

                updateUI();

            }
        });
    }

    void initMAP() {


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
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).


        // updatePackageLoc();
        return false;
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {

        mlocation = location;
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult" + Integer.toString(requestCode));
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                    enableMyLocation();
                }
            } else {

                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {


            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enableMyLocation();
            }
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

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed", "onBackPressed");
        if(is_this_a_driver) {
            backPressed = true;
        }
        if(myOwnPresenceRef != null) {
            myOwnPresenceRef.removeValue();
        }
        if(myOwnBusDatabaseReference!=null) {
            myOwnBusDatabaseReference.removeValue();
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Intent intent = new Intent(MapActivity.this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    void sendLocationToFirebase(double latitude, double longitude) {
        Log.e("sendLocation", Boolean.toString(getIntent().getExtras().getBoolean("is_this_a_driver", false)));
        Boolean is_this_a_driver = false;
        if (getIntent().getExtras() != null)
            is_this_a_driver = getIntent().getExtras().getBoolean("is_this_a_driver", false);
        if (!is_this_a_driver)
            return;
        if (myOwnPresenceRef != null) {
            myOwnPresenceRef.setValue(ServerValue.TIMESTAMP);
            myOwnPresenceRef.onDisconnect().removeValue();
        }
        if (myOwnBusDatabaseReference != null) {
            //String val=latitude + "," + longitude;
            HashMap map = new HashMap();
            map.put(LICENSE, Helper.formatLatLang(latitude, longitude));
            allBusDatabaseReference.updateChildren(map);
            myOwnBusDatabaseReference.onDisconnect().removeValue();
            Log.e("push", "lat..." + latitude + "......long" + longitude);
            updateBusMarker(LICENSE, latitude, longitude);
        } else
            Log.e("is null", "myOwnBusDatabaseReference");
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }



    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                if (isFirstLocationUpdate) {
                    isFirstLocationUpdate = false;
                    LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                updateLocationUI();
                sendLocationToFirebase(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
        };
    }


    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }


    public void startUpdatesButtonHandler(View view) {
        Log.e("on start click", "on click");
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }


    public void stopUpdatesButtonHandler(View view) {
        Log.e("on stop click", "on click");

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
    }


    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast toast = Toast.makeText(MapActivity.this, errorMessage, Toast.LENGTH_LONG);
                                View t_view = toast.getView();
                                t_view.getBackground().setColorFilter(getResources().getColor(R.color.submit_button), PorterDuff.Mode.SRC_IN);
                                TextView text = t_view.findViewById(android.R.id.message);
                                text.setTextColor(getResources().getColor(R.color.colorSwitchColor));
                                toast.show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void  updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                    mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));
            Log.e("last update on click",String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime));
        }
    }


    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }


        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        stopService(serviceIntent);
        mStartUpdatesButton.performClick();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    @Override
    public void onDestroy() {
        if(myOwnBusDatabaseReference!=null) {
            myOwnBusDatabaseReference.removeValue();
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "Pausing"+ is_this_a_driver);
        if(backPressed && is_this_a_driver) {
            backPressed = false;
            return;
        }
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        String lic = null;
        if (getIntent().getExtras() != null)
            lic = getIntent().getExtras().getString("LICENSE_NO", "").toUpperCase();
        serviceIntent.putExtra("LICENSE_NO",lic);
        if(myOwnBusDatabaseReference!=null) {
            myOwnBusDatabaseReference.removeValue();
        }
        if(is_this_a_driver) {
            timer.cancel();
            timer.purge();
            startService(serviceIntent);
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }




}

