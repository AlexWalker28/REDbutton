package kg.kloop.android.redbutton;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kg.kloop.android.redbutton.helpers.BottomNavigationViewHelper;
import kg.kloop.android.redbutton.information.InformationActivity;

public class SlidingMapsActivity extends AppCompatActivity {

    ViewPager viewPager;
    MapViewPageAdapter pagerAdapter;
    CharSequence titles[] = {"Карта", "События"};
    int numOfTabs = 2;
    String userId;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_maps);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pagerAdapter = new MapViewPageAdapter(getSupportFragmentManager(), titles, numOfTabs, this);
        viewPager = (ViewPager) findViewById(R.id.mapsViewPager);
        viewPager.setAdapter(pagerAdapter);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.maps_bottom_navigation_view);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mapsTabLayout);
        tabLayout.setupWithViewPager(viewPager);

        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.map_item);
    }
}
