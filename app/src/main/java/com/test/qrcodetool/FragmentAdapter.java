package com.test.qrcodetool;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by WTZ on 2017/10/8.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return (mFragments == null) ? null : mFragments.get(position);
    }

    @Override
    public int getCount() {
        return (mFragments == null) ? 0 : mFragments.size();
    }
}
