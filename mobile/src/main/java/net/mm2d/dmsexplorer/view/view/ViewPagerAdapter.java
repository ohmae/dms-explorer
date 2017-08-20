/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewPagerAdapter extends PagerAdapter {
    private final List<View> mViewList;

    public ViewPagerAdapter() {
        mViewList = new ArrayList<>();
    }

    public void add(View view) {
        mViewList.add(view);
    }

    public View get(int position) {
        return mViewList.get(position);
    }

    public void clear() {
        mViewList.clear();
    }

    @Override
    public Object instantiateItem(
            ViewGroup container,
            int position) {
        final View view = mViewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(
            ViewGroup container,
            int position,
            Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(
            View view,
            Object object) {
        return view == object;
    }
}
