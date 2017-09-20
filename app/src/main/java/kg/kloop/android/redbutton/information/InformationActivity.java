package kg.kloop.android.redbutton.information;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.alexwalker.sendsmsapp.R;

public class InformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        ViewPager viewPager = (ViewPager)findViewById(R.id.info_view_pager);
        viewPager.setAdapter(new CustomViewPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout)findViewById(R.id.info_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }
}
