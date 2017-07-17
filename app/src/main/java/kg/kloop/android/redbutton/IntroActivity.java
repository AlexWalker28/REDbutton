package kg.kloop.android.redbutton;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.example.alexwalker.sendsmsapp.Manifest;
import com.example.alexwalker.sendsmsapp.R;
import com.firebase.ui.auth.AuthUI;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import java.util.Arrays;

/**
 * Created by ThirtySeven on 13.05.2017.
 */

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {
    private static final int RC_SIGN_IN = 10;
    int version = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*addSlide(new SimpleSlide.Builder()
                .title(R.string.helloText)
                .description(R.string.helloDiscription)
                .background(R.color.slide1)
                .backgroundDark(R.color.colorPrimary)
                .scrollable(false)
                .build());*/
        addSlide(new FragmentSlide.Builder() //приветствие
                .background(R.color.slide1)
                .backgroundDark(R.color.slide2)
                .fragment(R.layout.slide_1, R.style.AppTheme)
                .build());
        addSlide(new FragmentSlide.Builder() // добавление номеров и сообщения
                .background(R.color.slide1)
                .backgroundDark(R.color.slide2)
                .fragment(R.layout.slide_2, R.style.AppTheme)
                .build());
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            addSlide(new SimpleSlide.Builder() // sms permission
                    .title(R.string.permissionsText)
                    .description(R.string.permissionsDiscriptionSMS)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .permission(android.Manifest.permission.SEND_SMS)
                    .build());
        }
        if (ActivityCompat.checkSelfPermission(IntroActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            addSlide(new SimpleSlide.Builder() // gps permission
                    .title("")
                    .description(R.string.permissionsDiscriptionGPS)
                    .background(R.color.slide1)
                    .backgroundDark(R.color.slide2)
                    .permission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .build());
        }
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
        Intent i = new Intent(this, SettingsActivity.class);
        i.putExtra("key", 1);
        startActivity(i);
    }

}
