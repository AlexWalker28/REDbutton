package kg.kloop.android.redbutton;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import kg.kloop.android.redbutton.groups.SlidingGroupsActivity;
import kg.kloop.android.redbutton.helpers.BottomNavigationViewHelper;
import kg.kloop.android.redbutton.helpers.NavigationHelper;
import kg.kloop.android.redbutton.information.InfoWebViewFragment;
import kg.kloop.android.redbutton.information.RSSFeedFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private MenuItem signInMenuItem;
    private MenuItem signOutMenuItem;
    private User user;
    private static final int RC_SIGN_IN = 10;
    private SharedPreferences preferences;

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
                switch (item.getItemId()){
                    case R.id.home_item:
                        setFragment(new HomeFragment());
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
                        setFragment(new RSSFeedFragment());
                        break;
                    case R.id.read_item:
                        setFragment(new InfoWebViewFragment());
                        break;
                    case R.id.profile_item:
                        setFragment(new SettingsFragment());
                        break;
                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.home_item);

        //=====================
        //Notification service
        //=====================
        if(!isMyServiceRunning(NotificationService.class)) {
            Intent notificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);
            startService(notificationServiceIntent);
        }

    }

    private void setFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction;
        transaction = manager.beginTransaction();
        transaction.add(R.id.main_frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
                    Log.v("User", "userData: " + userID + "\n" + userName + "\n" + userEmail);
                }

            }
        };
        return authStateListener;
    }

    private void saveInPref(String string) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CURRENT_USER_ID, string);
        editor.apply();
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
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.home_item) {
            finish();
        } else if(bottomNavigationView.getSelectedItemId() == R.id.opportunities_item) {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_frame_layout);
            if(currentFragment instanceof InfoWebViewFragment){
                super.onBackPressed();
            } else bottomNavigationView.setSelectedItemId(R.id.home_item);
        } else bottomNavigationView.setSelectedItemId(R.id.home_item);
    }

    @Override
    protected void onStart() {
        auth.addAuthStateListener(getAuthStateListener());
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(NavigationHelper.getSelectedItemId() == 0){
            bottomNavigationView.setSelectedItemId(R.id.home_item);
        } else {
            bottomNavigationView.setSelectedItemId(NavigationHelper.getSelectedItemId());
            Log.v(TAG, "selected item: " + bottomNavigationView.getSelectedItemId());
            Log.v(TAG, "should be selected: " + NavigationHelper.getSelectedItemId());
        }
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
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        auth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
    }
}
