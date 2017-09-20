package kg.kloop.android.redbutton.information;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

/**
 * Created by alexwalker on 20.09.17.
 */

class CustomViewPagerAdapter extends FragmentPagerAdapter {

    String[] tabTitles = {"Информация", "Новости"};

    public CustomViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new WebViewFragment();
            case 1:
                return new RSSFeedFragment();
            default:
                return new WebViewFragment();
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
