package com.pocketapps.pockalendar.HomePage.ViewPager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pocketapps.pockalendar.AppSigninPages.CreateLoginFragment;
import com.pocketapps.pockalendar.AppSigninPages.SignInFragment;
import com.pocketapps.pockalendar.DetailedWeatherPage.FiveDayWeatherFragment;
import com.pocketapps.pockalendar.HomePage.HomeFragment;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.SchedulePage.ScheduleFragment;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by chandrima on 22/03/18.
 */

public class HomeViewPagerAdapter extends FragmentPagerAdapter {
    private static final int NUMBEROFTABSINHOME = 3;

    private Context mContext;
    private SortedMap<Integer, Integer> mTabIcons = new TreeMap<>();

    public HomeViewPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
        mTabIcons.put(0, R.drawable.ic_home_white_24dp);
        mTabIcons.put(1, R.drawable.ic_date_range_white_24dp);
        mTabIcons.put(2, R.drawable.ic_cloud_queue_white_24dp);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = Fragment.instantiate(mContext, HomeFragment.class.getName());
                break;
            case 1:
                fragment = Fragment.instantiate(mContext, ScheduleFragment.class.getName());
                break;
            case 2:
                fragment = Fragment.instantiate(mContext, FiveDayWeatherFragment.class.getName());
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return NUMBEROFTABSINHOME;
    }

    public int getTabIcon(int position) {
        return mTabIcons.get(position);
    }
}