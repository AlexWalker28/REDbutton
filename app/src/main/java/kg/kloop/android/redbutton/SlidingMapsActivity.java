package kg.kloop.android.redbutton;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;

import kg.kloop.android.redbutton.groups.ViewPagerAdapter;

public class SlidingMapsActivity extends AppCompatActivity {

    ViewPager viewPager;
    MapViewPageAdapter pagerAdapter;
    CharSequence titles[] = {"Map", "Events"};
    int numOfTabs = 2;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_maps);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pagerAdapter = new MapViewPageAdapter(getSupportFragmentManager(), titles, numOfTabs, this);
        viewPager = (ViewPager) findViewById(R.id.mapsViewPager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mapsTabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
