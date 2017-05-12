package kg.kloop.android.redbutton.groups;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;


public class SlidingGroupsActivity extends AppCompatActivity {

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    CharSequence titles[] = {"Группы", "Мои группы"};
    int numOfTabs = 2;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_groups);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numOfTabs, this);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
