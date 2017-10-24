package kg.kloop.android.redbutton;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class HomeFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MainActivity";
    private View view;
    private Button sendButton;
    private BottomNavigationView bottomNavigationView;
    private String firstPhoneNumber;
    private String secondPhoneNumber;
    private String message;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private LocationManager locationManager;
    private User user;
    private Event event;
    private long timeInMillis;
    private ProgressBar progressBar;
    private TextView latLngTextView;
    private TextView userInfoTextView;
    private EventStateReceiver eventStateReceiver;
    private CustomLatLng coordinates;
    private SharedPreferences preferences;
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;

    protected Location mCurrentLocation;
    private String childUniqueKey;
    private TextView permissionsInfoTextView;
    private Boolean isFirstRun;


    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        buildGoogleApiClient();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessageDataFromSharedPref();
                sendAlertMessage();

                if (isLocationEnabled()) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestGPSPermission();
                    } else {
                        timeInMillis = System.currentTimeMillis();
                        user = new User(user.getUserID(), user.getUserName(), user.getUserEmail(),
                                user.getFirstNumber(), user.getSecondNumber(), message);
                        coordinates = event.getCoordinates();
                        event = new Event(coordinates, user, timeInMillis);
                        childUniqueKey = databaseReference.push().getKey();
                        databaseReference.child(childUniqueKey).setValue(event);
                        if (event.getCoordinates() == null) {
                            if (mGoogleApiClient.isConnected()) {
                                initReceiver();
                                startLocationService();
                            }
                        }

                    }
                } else {
                    showAlertToEnableGPS();
                }
                if(firebaseUser != null){
                    saveInPref(firebaseUser.getUid());
                }

            }
        });
        setPermissionsInfo();
        return view;
    }
    private FirebaseAuth.AuthStateListener getAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String userID = firebaseUser.getUid();
                    String userName = firebaseUser.getDisplayName();
                    if(userName == null){
                        userName = firebaseUser.getPhoneNumber();
                    }
                    String userEmail = firebaseUser.getEmail();
                    String userPhoneNumber = firebaseUser.getPhoneNumber();
                    user.setUserID(userID);
                    user.setUserName(userName);
                    user.setUserEmail(userEmail);
                    user.setPhoneNumber(userPhoneNumber);
                    saveInPref(userID);
                    if (userName != null && userEmail != null) {
                        userInfoTextView.setText(userName + "\n" + userEmail);
                    } else if (userPhoneNumber != null) {
                        userInfoTextView.setText(userPhoneNumber);
                    }
                    Log.v("User", "userData: " + userID + "\n" + userName + "\n" + userEmail);
                } else  userInfoTextView.setText(R.string.you_need_to_log_in);

            }
        };
        return authStateListener;
    }

    private void saveInPref(String string) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CURRENT_USER_ID, string);
        editor.putString(Constants.DATABASE_CHILD_ID, childUniqueKey);
        editor.apply();
    }

    //=====================
    //Location
    //=====================
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlertToEnableGPS() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void startLocationService() {
        Intent serviceIntent = new Intent(getActivity(), LocationService.class);
        serviceIntent.putExtra(Constants.DATABASE_CHILD_ID, childUniqueKey);
        serviceIntent.putExtra(Constants.FIRST_NUMBER, firstPhoneNumber);
        serviceIntent.putExtra(Constants.SECOND_NUMBER, secondPhoneNumber);
        getActivity().startService(serviceIntent);
        Log.v("LocationService", "Location service should start");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(!isFirstRun) requestGPSPermission();
        } else {
            requestLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() != 0 && location.getLongitude() != 0) {
            CustomLatLng latLng = new CustomLatLng(location.getLatitude(), location.getLongitude());
            event.setCoordinates(latLng);
            latLngTextView.setText("lat: " + event.getCoordinates().getLat() + "\nlat: " + event.getCoordinates().getLng());
        }
        if (event.getCoordinates().getLat() == 0 && event.getCoordinates().getLng() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        } else progressBar.setVisibility(View.GONE);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0);
        FusedLocationProviderClient locationClient = new FusedLocationProviderClient(getActivity());
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    if(location.getLatitude() != 0 && location.getLongitude() != 0){
                        CustomLatLng latLng = new CustomLatLng(location.getLatitude(), location.getLongitude());
                        event.setCoordinates(latLng);
                    }
                    if(event.getCoordinates().getLat() == 0 && event.getCoordinates().getLng() == 0){
                        progressBar.setVisibility(View.VISIBLE);
                    } else progressBar.setVisibility(View.GONE);
                    latLngTextView.setText("lat: " + event.getCoordinates().getLat() + "\n" + "lng: " + event.getCoordinates().getLng());
                }
            }
        };
        if(android.os.Build.VERSION.SDK_INT > 22) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
            }
        } else locationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }


    //=================================
    //Broadcast receiver for service
    //=================================
    private void initReceiver() {
        eventStateReceiver = new EventStateReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(eventStateReceiver, intentFilter);
    }


    private class EventStateReceiver extends WakefulBroadcastReceiver {
        private EventStateReceiver(){

        }
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                CustomLatLng customLatLng = new CustomLatLng(intent.getDoubleExtra(Constants.EVENT_LAT, 0),
                        intent.getDoubleExtra(Constants.EVENT_LNG, 0));
                event.setCoordinates(customLatLng);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //=======================
    //SMS
    //=======================
    private void getMessageDataFromSharedPref() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        firstPhoneNumber = preferences.getString(Constants.FIRST_NUMBER, "");
        secondPhoneNumber = preferences.getString(Constants.SECOND_NUMBER, "");
        message = preferences.getString(Constants.MESSAGE, "");
        Log.v("data", firstPhoneNumber + "\n" + secondPhoneNumber + "\n" + message);
    }

    private void sendAlertMessage() {
        if(Build.VERSION.SDK_INT > 25){
            if(!isReadPhoneStatePermissionGranted()){
                requestReadPhoneStatePermission();
            }
        }
        if (!isSMSPermissionGranted()) {
            requestSMSPermission();
        } else {
            try {
                sendSMS(firstPhoneNumber, message);
                sendSMS(secondPhoneNumber, message);
                Toast.makeText(getActivity(), R.string.sms_sent, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                if (firstPhoneNumber == null && secondPhoneNumber == null) {
                    Toast.makeText(getActivity(), R.string.no_phone_numbers, Toast.LENGTH_LONG).show();
                }
                Log.v("SMS", "sms failed: " + e);
                e.printStackTrace();
            }
        }
    }
    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        if(event.getCoordinates() != null) { //send SMS with coordinates
            smsManager.sendTextMessage(phoneNumber, null, message
                    + "\nhttp://maps.google.com/maps?q="
                    + event.getCoordinates().getLat()
                    + "," + event.getCoordinates().getLng(), null, null);
        } else { //send SMS without coordinates (SMS with coordinates will be sent from service)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }



    //================================
    //permissions
    //================================
    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, 1);
    }
    private void requestGPSPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }
    private void requestReadPhoneStatePermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
    }
    private boolean isSMSPermissionGranted() {
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else return false;
    }
    private boolean isGPSPermissionGranted(){
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else return false;
    }
    private boolean isReadPhoneStatePermissionGranted(){
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: //sms permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        sendSMS(firstPhoneNumber, message);
                        sendSMS(secondPhoneNumber, message);
                        Toast.makeText(getActivity(), R.string.sms_sent, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.sms_fail, Toast.LENGTH_LONG).show();
                        Log.v("SMS", "sms failed: " + e);
                        e.printStackTrace();
                    }
                }
                break;
            case 2: //gps permission
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates();

                }
                break;
            case 3: //read phone state permission

        }
    }
    private void setPermissionsInfo() {
        if (!isGPSPermissionGranted()){
            permissionsInfoTextView.setText(R.string.noGPSPermissionText);
        } else permissionsInfoTextView.setText("");
        if (!isSMSPermissionGranted()) {
            permissionsInfoTextView.setText(R.string.noSMSPermissionText);
        } else permissionsInfoTextView.setText("");
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        auth.addAuthStateListener(getAuthStateListener());
        super.onStart();
    }

    @Override
    public void onResume() {
        mGoogleApiClient.connect();
        requestLocationUpdates();
        setPermissionsInfo();
        super.onResume();
    }

    @Override
    public void onStop() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void init() {
        user = new User();
        event = new Event();
        sendButton = (Button) view.findViewById(R.id.redButton);
        bottomNavigationView = (BottomNavigationView)view.findViewById(R.id.bottom_navigation_view);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        auth = FirebaseAuth.getInstance();
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        latLngTextView = (TextView)view.findViewById(R.id.latLngTextView);
        userInfoTextView = (TextView)view.findViewById(R.id.userInfoTextView);
        permissionsInfoTextView = (TextView)view.findViewById(R.id.permissionsInfoTextView);
        isFirstRun = MainActivity.isFirstRun();
    }



}
