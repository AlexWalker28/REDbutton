package kg.kloop.android.redbutton.groups;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class GroupActivity extends AppCompatActivity {

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    CharSequence titles[] = {"Участники", "Запросы"};
    int numOfTabs = 2;
    String userId;
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        groupName = getIntent().getStringExtra("groupName");
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numOfTabs, this);
        viewPager = (ViewPager) findViewById(R.id.viewPagerGroupMembers);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }
}
