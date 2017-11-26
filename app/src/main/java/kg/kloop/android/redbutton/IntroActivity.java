package kg.kloop.android.redbutton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.example.alexwalker.sendsmsapp.R;
import com.firebase.ui.auth.AuthUI;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import java.util.Arrays;

import kg.kloop.android.redbutton.helpers.NavigationHelper;

/**
 * Created by ThirtySeven on 13.05.2017.
 */

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {
    private static final int RC_SIGN_IN = 10;
    private static final int REQUEST_CODE = 113;
    int version = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new FragmentSlide.Builder() // sign in or register
                .background(R.color.slide1)
                .backgroundDark(R.color.slide2)
                .fragment(R.layout.slide_1, R.style.AppTheme)
                .build());
        addSlide(new FragmentSlide.Builder() // add phone numbers and message
                .background(R.color.slide1)
                .backgroundDark(R.color.slide2)
                .fragment(R.layout.slide_2, R.style.AppTheme)
                .build());
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            addSlide(new SimpleSlide.Builder() // sms permission
                    .title(R.string.permissionsText)
                    .description(R.string.permissionsDescriptionSMS)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .permission(android.Manifest.permission.SEND_SMS)
                    .build());
        }
        if(Build.VERSION.SDK_INT > 25 && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            addSlide(new SimpleSlide.Builder() // sms permission
                    .title(R.string.permissionsText)
                    .description(R.string.permissionDescriptionReadPhoneState)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .permission(Manifest.permission.READ_PHONE_STATE)
                    .build());
        }
        if (ActivityCompat.checkSelfPermission(IntroActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            addSlide(new SimpleSlide.Builder() // gps permission
                    .title("")
                    .description(R.string.permissionsDescriptionGPS)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .permission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .build());
        }
        addSlide(new SimpleSlide.Builder()
                    .title("")
                    .description(R.string.introGroups)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .build());
        setButtonBackVisible(false);
        setButtonNextVisible(false);
    }

    public void registrationButton (View view){
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
    }

    public void goToSettingsButton(View view) {
        NavigationHelper.setSelectedItemId(R.id.profile_item);
        startActivityForResult(new Intent(IntroActivity.this, MainActivity.class), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        NavigationHelper.setSelectedItemId(R.id.home_item);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    nextSlide();
            }
        }
    }
}
