package kg.kloop.android.redbutton;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.Manifest;
import com.example.alexwalker.sendsmsapp.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by ThirtySeven on 13.05.2017.
 */

public class IntroActivity extends AppIntro2 {
    Button regButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        init();

//        regButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(IntroActivity.this, "reg test", Toast.LENGTH_SHORT).show();
//            }
//        });

        addSlide(SampleSlide.newInstance(R.layout.slide_1));

        addSlide(SampleSlide.newInstance(R.layout.slide_2));

        addSlide(SampleSlide.newInstance(R.layout.slide_3));

        addSlide(SampleSlide.newInstance(R.layout.slide_4));

        showSkipButton(false);
//        setFadeAnimation();
        askForPermissions(new String[]{
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        }, 3);
    }

    private void init() {
        regButton = (Button) findViewById(R.id.redButton);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Toast.makeText(this, "You can't skip setup. SORRY bitch", Toast.LENGTH_SHORT).show();
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
