package kg.kloop.android.redbutton;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private String firstPhoneNumber;
    private String secondPhoneNumber;
    private String message;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private MenuItem signInMenuItem;
    private MenuItem signOutMenuItem;
    private LocationManager locationManager;
    private User user;
    private Event event;
    private Button button;
    private ProgressBar progressBar;
    private TextView textView;
    private TextView textView1;
    private static final int RC_SIGN_IN = 10;
    private EventStateReceiver eventStateReceiver;
    private CustomLatLng coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListenerGPS);
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
                    } else {
                        user = new User(user.getUserID(), user.getUserName(), user.getUserEmail(),
                                user.getFirstNumber(), user.getSecondNumber(), user.getMessage());
                        coordinates = event.getCoordinates();
                        event = new Event(coordinates, user);
                        String childUniqueKey = databaseReference.push().getKey();
                        databaseReference.child(childUniqueKey).setValue(event);
                        if (event.getCoordinates() == null) {
                            initReceiver();
                            Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                            serviceIntent.putExtra(Constants.DATABASE_CHILD_ID, childUniqueKey);
                            startService(serviceIntent);
                        }

                    }
                } else showAlertToEnableGPS();

                // MessageData messageData = new MessageData(firstPhoneNumber, secondPhoneNumber, message);

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    private FirebaseAuth.AuthStateListener getAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String userID = firebaseUser.getUid();
                    String userName = firebaseUser.getDisplayName();
                    String userEmail = firebaseUser.getEmail();
                    user.setUserID(userID);
                    user.setUserName(userName);
                    user.setUserEmail(userEmail);
                    textView1.setText(userName + "\n" + userEmail);
                    Log.v("User", "userData: " + userID + "\n" + userName + "\n" + userEmail);
                } else {

                    Intent intent = new Intent(MainActivity.this, ManualActivity.class);
                    startActivity(intent);
                }

            }
        };
        return authStateListener;
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
                Toast.makeText(getApplicationContext(), R.string.sms_sent, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.sms_fail, Toast.LENGTH_LONG).show();
                Log.v("SMS", "sms failed: " + e);
                e.printStackTrace();
            }
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message
                + "\nhttp://maps.google.com/maps?q="
                + event.getCoordinates().getLat()
                + "," + event.getCoordinates().getLng(), null, null);
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
        dialog.setTitle(R.string.enable_location)
                .setMessage(R.string.check_gps_false)
                .setPositiveButton(R.string.location_settings, new DialogInterface.OnClickListener() {
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

    private LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                CustomLatLng latLng = new CustomLatLng(location.getLatitude(), location.getLongitude());
                event.setCoordinates(latLng);
            }
            if (event.getCoordinates().getLat() == 0 && event.getCoordinates().getLng() == 0) {
                progressBar.setVisibility(View.VISIBLE);
            } else progressBar.setVisibility(View.GONE);
            textView.setText("lat: " + event.getCoordinates().getLat() + "\n" + "lng: " + event.getCoordinates().getLng());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    //================================
    //permissions
    //================================
    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
    }

    private void requestGPSPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }

    private boolean isSMSPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else return false;
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListenerGPS);
                }
                break;
        }
    }


    //============================
    //menu button in action bar
    //============================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        signInMenuItem = menu.findItem(R.id.sign_in_item);
        signOutMenuItem = menu.findItem(R.id.sign_out_item);
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
        switch (item.getItemId()) {
            case R.id.manual:
                break;
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
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN); //RC_SIGN_IN - request code
                    //User is signed in
                } else {
                    //    onSignInInit(mFirebaseUser);
                    Toast.makeText(MainActivity.this, R.string.login, Toast.LENGTH_SHORT).show();

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

        }
        return super.onOptionsItemSelected(item);
    }

    private void setAuthMenuItemsVisibility() {
        if (firebaseUser == null) {
            signOutMenuItem.setVisible(false);
            signInMenuItem.setVisible(true);
        } else {
            signInMenuItem.setVisible(false);
            signOutMenuItem.setVisible(true);
        }
    }

    //=================================
    //Broadcast receiver for service
    //=================================
    private void initReceiver() {
        eventStateReceiver = new EventStateReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(eventStateReceiver, intentFilter);
    }

    private class EventStateReceiver extends WakefulBroadcastReceiver {
        private EventStateReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                CustomLatLng customLatLng = new CustomLatLng(intent.getDoubleExtra(Constants.EVENT_LAT, 0),
                        intent.getDoubleExtra(Constants.EVENT_LNG, 0));
                event.setCoordinates(customLatLng);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        button = (Button) findViewById(R.id.button2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView);
        textView1 = (TextView) findViewById(R.id.textView2);
    }
}
