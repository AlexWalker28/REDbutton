package kg.kloop.android.redbutton;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
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

import java.util.Arrays;

import kg.kloop.android.redbutton.groups.SlidingGroupsActivity;
import kg.kloop.android.redbutton.helpers.BottomNavigationViewHelper;
import kg.kloop.android.redbutton.information.InformationActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
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
    private MenuItem signInMenuItem;
    private MenuItem signOutMenuItem;
    private User user;
    private Event event;
    private ProgressBar progressBar;
    private TextView latLngTextView;
    private TextView userInfoTextView;
    private static final int RC_SIGN_IN = 10;
    private SharedPreferences preferences;
    private LocationCallback mLocationCallback;

    protected Location mCurrentLocation;
    private String childUniqueKey;
    private TextView permissionsInfoTextView;
    private static Boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        if (isFirstRun) {
            startActivity(new Intent(MainActivity.this, IntroActivity.class));
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();


        init();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(getAuthStateListener());

        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction;
                switch (item.getItemId()){
                    case R.id.home_item:
                        fragment = new HomeFragment();
                        manager = getFragmentManager();
                        transaction = manager.beginTransaction();
                        transaction.add(R.id.main_frame_layout, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case R.id.map_item:
                        if (firebaseUser != null) {
                            Intent intent = new Intent(MainActivity.this, SlidingMapsActivity.class);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(MainActivity.this, IntroActivity.class));
                            Toast.makeText(getApplicationContext(), R.string.you_need_to_log_in, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.opportunities_item:
                        startActivity(new Intent(MainActivity.this, InformationActivity.class));
                        break;
                    case R.id.read_item:
                        break;
                    case R.id.profile_item:
                        /*Intent intent = new Intent(MainActivity.this, SettingsFragment.class);
                        startActivity(intent);*/
                        fragment = new SettingsFragment();
                        transaction = manager.beginTransaction();
                        transaction.add(R.id.main_frame_layout, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                }
                return true;
            }
        });

        //=====================
        //Notification service
        //=====================
        if(!isMyServiceRunning(NotificationService.class)) {
            Intent notificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);
            startService(notificationServiceIntent);
        }

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

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0);
        FusedLocationProviderClient locationClient = new FusedLocationProviderClient(getApplicationContext());
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
            }
        } else locationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    //============================
    //menu in action bar
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
        switch (item.getItemId()){
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
                    startActivity(new Intent(MainActivity.this, IntroActivity.class));
                    Toast.makeText(this, R.string.you_need_to_log_in, Toast.LENGTH_SHORT).show();
                    break;
                }
            case R.id.help:
                startActivity(new Intent(MainActivity.this, IntroActivity.class));
                break;

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
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFirstRun(){
        return isFirstRun;
    }

    @Override
    protected void onStart() {
        auth.addAuthStateListener(getAuthStateListener());
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
            super.onStop();
        }
    }

    private void init() {
        user = new User();
        event = new Event();
        sendButton = (Button) findViewById(R.id.redButton);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        auth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        latLngTextView = (TextView)findViewById(R.id.latLngTextView);
        userInfoTextView = (TextView)findViewById(R.id.userInfoTextView);
        permissionsInfoTextView = (TextView)findViewById(R.id.permissionsInfoTextView);
    }
}
