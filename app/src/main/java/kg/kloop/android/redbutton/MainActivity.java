package kg.kloop.android.redbutton;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import kg.kloop.android.redbutton.groups.SlidingGroupsActivity;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Button sendButton;
    private String firstPhoneNumber;
    private String secondPhoneNumber;
    private String message;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference groupsReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private MenuItem signInMenuItem;
    private MenuItem signOutMenuItem;
    private MenuItem groups;
    private LocationManager locationManager;
    private User user;
    private Event event;
    private long timeInMillis;
    private Button mapButton;
    private ProgressBar progressBar;
    private TextView latLngTextView;
    private TextView userInfoTextView;
    private static final int RC_SIGN_IN = 10;
    private EventStateReceiver eventStateReceiver;
    private CustomLatLng coordinates;
    private SharedPreferences preferences;
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;

    protected Location mCurrentLocation;
    protected String mAddressOutput;
    private String childUniqueKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*startActivity(new Intent(MainActivity.this, IntroActivity.class));*/

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        if (isFirstRun) {
            startActivity(new Intent(MainActivity.this, IntroActivity.class));
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();


        init();
        buildGoogleApiClient();
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);
                requestLocationUpdates();
            }
        } else showAlertToEnableGPS();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(getAuthStateListener());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessageDataFromSharedPref();
                sendAlertMessage();

                if (isLocationEnabled()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestGPSPermission();
                    }else{
                        timeInMillis = System.currentTimeMillis();
                        user = new User(user.getUserID(), user.getUserName(), user.getUserEmail(),
                                user.getFirstNumber(), user.getSecondNumber(), user.getMessage());
                        coordinates = event.getCoordinates();
                        event = new Event(coordinates, user, timeInMillis);
                        childUniqueKey = databaseReference.push().getKey();
                        databaseReference.child(childUniqueKey).setValue(event);
                        if(event.getCoordinates() == null){
                            if (mGoogleApiClient.isConnected() && mCurrentLocation != null) {
                                initReceiver();
                                startLocationService();
                            }
                        }

                    }
                } else showAlertToEnableGPS();

               // MessageData messageData = new MessageData(firstPhoneNumber, secondPhoneNumber, message);

            }
        });


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        //=====================
        //Notification service
        //=====================
        Intent notificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);
        startService(notificationServiceIntent);

    }

    private FirebaseAuth.AuthStateListener getAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    String userID = firebaseUser.getUid();
                    String userName = firebaseUser.getDisplayName();
                    String userEmail = firebaseUser.getEmail();
                    String userPhoneNumber = firebaseUser.getPhoneNumber();
                    user.setUserID(userID);
                    user.setUserName(userName);
                    user.setUserEmail(userEmail);
                    user.setPhoneNumber(userPhoneNumber);
                    saveInPref(userID);
                    if(userPhoneNumber == null){
                        userInfoTextView.setText(userName + "\n" + userEmail);
                    } else {
                        userInfoTextView.setText(userPhoneNumber);
                    }
                    Log.v("User", "userData: " + userID + "\n" + userName + "\n" + userEmail);
                } else userInfoTextView.setText("You need to log in \nand configure your settings");

            }
        };
        return authStateListener;
    }

    private void saveInPref(String string){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CURRENT_USER_ID, string);
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void startLocationService() {
        Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
        serviceIntent.putExtra(Constants.DATABASE_CHILD_ID, childUniqueKey);
        serviceIntent.putExtra(Constants.FIRST_NUMBER, firstPhoneNumber);
        serviceIntent.putExtra(Constants.SECOND_NUMBER, secondPhoneNumber);
        startService(serviceIntent);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestGPSPermission();
        }else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(0);
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

    }


    /*@Override
    public void onLocationChanged(Location location) {
        if(location.getLatitude() != 0 && location.getLongitude() != 0){
            CustomLatLng latLng = new CustomLatLng(location.getLatitude(), location.getLongitude());
            event.setCoordinates(latLng);
        }
        if(event.getCoordinates().getLat() == 0 && event.getCoordinates().getLng() == 0){
            progressBar.setVisibility(View.VISIBLE);
        } else progressBar.setVisibility(View.GONE);
        latLngTextView.setText("lat: " + event.getCoordinates().getLat() + "\n" + "lng: " + event.getCoordinates().getLng());
    }*/


    //=================================
    //Broadcast receiver for service
    //=================================
    private void initReceiver() {
        eventStateReceiver = new EventStateReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(eventStateReceiver, intentFilter);
    }


    private class EventStateReceiver extends WakefulBroadcastReceiver{
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
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        firstPhoneNumber = preferences.getString(Constants.FIRST_NUMBER, "");
        secondPhoneNumber = preferences.getString(Constants.SECOND_NUMBER, "");
        message = preferences.getString(Constants.MESSAGE, "");
        Log.v("data", firstPhoneNumber + "\n" + secondPhoneNumber + "\n" + message);
    }

    private void sendAlertMessage() {
        if (isSMSPermissionGranted()) {
            requestSMSPermission();
        } else {
            try {
                sendSMS(firstPhoneNumber, message);
                sendSMS(secondPhoneNumber, message);
                Toast.makeText(getApplicationContext(), "Sms sent", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Fail. Please try again", Toast.LENGTH_LONG).show();
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
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
    }
    private void requestGPSPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }
    private boolean isSMSPermissionGranted() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            return true;
        }else return false;
    }
    private boolean isGPSPermissionGranted(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
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
                        Toast.makeText(getApplicationContext(), R.string.sms_sent, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.sms_fail, Toast.LENGTH_LONG).show();
                        Log.v("SMS", "sms failed: " + e);
                        e.printStackTrace();
                    }
                }
                break;
            case 2: //gps permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);

                }
                break;
        }
    }


    //============================
    //menu mapButton in action bar
    //============================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        signInMenuItem = menu.findItem(R.id.sign_in_item);
        signOutMenuItem = menu.findItem(R.id.sign_out_item);
        groups = menu.findItem(R.id.groups);
        setAuthMenuItemsVisibility();
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        setAuthMenuItemsVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_item:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_in_item:
                if (firebaseUser == null) {
                    //   onSignOutCleanUp();
                    //Starts sign-in flow
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN); //RC_SIGN_IN - request code
                    //User is signed in
                } else {
                    //    onSignInInit(mFirebaseUser);
                    Toast.makeText(MainActivity.this, "Your are logged in!", Toast.LENGTH_SHORT).show();

                }
                auth.addAuthStateListener(getAuthStateListener());
                break;
            case R.id.sign_out_item:
                auth.signOut();
                if (firebaseUser == null) {
                    Toast.makeText(MainActivity.this, R.string.logout, Toast.LENGTH_SHORT).show();
                }
                Log.v("Logout", "item pressed: " + item.getItemId() + "\n" + "should be:    " + R.id.sign_out_item);
                break;

            case R.id.groups:
                //startActivity(new Intent(MainActivity.this, GroupsList.class));
                if (firebaseUser != null) {
                    startActivity(new Intent(MainActivity.this, SlidingGroupsActivity.class));
                    break;
                } else {
                    Toast.makeText(this, "You need to log in", Toast.LENGTH_SHORT).show();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    private void setAuthMenuItemsVisibility() {
        if(firebaseUser == null){
            signOutMenuItem.setVisible(false);
            signInMenuItem.setVisible(true);
        } else {
            signInMenuItem.setVisible(false);
            signOutMenuItem.setVisible(true);
        }
    }



    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //if(eventStateReceiver != null) unregisterReceiver(eventStateReceiver);
        super.onDestroy();
    }

    private void init() {
        user = new User();
        event = new Event();
        sendButton = (Button) findViewById(R.id.redButton);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        auth = FirebaseAuth.getInstance();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        mapButton = (Button)findViewById(R.id.button2);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        latLngTextView = (TextView)findViewById(R.id.textView);
        userInfoTextView = (TextView)findViewById(R.id.textView2);
    }
}
