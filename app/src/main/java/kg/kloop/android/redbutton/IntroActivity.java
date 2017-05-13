package kg.kloop.android.redbutton;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.Manifest;
import com.example.alexwalker.sendsmsapp.R;
import com.firebase.ui.auth.AuthUI;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import java.util.Arrays;

/**
 * Created by ThirtySeven on 13.05.2017.
 */

public class IntroActivity extends AppIntro2 {
    private static final int RC_SIGN_IN = 10;
    int version = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        addSlide(SampleSlide.newInstance(R.layout.slide_1));

        addSlide(SampleSlide.newInstance(R.layout.slide_2));

        if (version > 22){
            addSlide(SampleSlide.newInstance(R.layout.slide_3));
        }

        addSlide(SampleSlide.newInstance(R.layout.slide_4));

        showSkipButton(false);

        //        setFadeAnimation();
        askForPermissions(new String[]{
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        }, 3);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Toast.makeText(this, "You can't skip setup. SORRY bitch", Toast.LENGTH_SHORT).show();
    }

    public void registrationButton (View view){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN); //RC_SIGN_IN - request code
    }

    public void goToSettingsButton(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        i.putExtra("key", 1);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        return;
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

}
