package kg.kloop.android.redbutton;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;

import kg.kloop.android.redbutton.helpers.BottomNavigationViewHelper;
import kg.kloop.android.redbutton.helpers.NavigationHelper;

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

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_item:
                        NavigationHelper.setSelectedItemId(R.id.home_item);
                        finish();
                        break;
                    case R.id.map_item:
                        break;
                    case R.id.opportunities_item:
                        NavigationHelper.setSelectedItemId(R.id.opportunities_item);
                        finish();
                        break;
                    case R.id.read_item:
                        NavigationHelper.setSelectedItemId(R.id.read_item);
                        finish();
                        break;
                    case R.id.profile_item:
                        NavigationHelper.setSelectedItemId(R.id.profile_item);
                        finish();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavigationHelper.setSelectedItemId(R.id.home_item);
        finish();
    }
}
