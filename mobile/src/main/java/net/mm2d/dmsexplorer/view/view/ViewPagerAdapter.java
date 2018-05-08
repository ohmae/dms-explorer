/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewPagerAdapter extends PagerAdapter {
    @NonNull
    private final List<View> mViewList;

    public ViewPagerAdapter() {
        mViewList = new ArrayList<>();
    }

    public void add(@NonNull final View view) {
        mViewList.add(view);
    }

    public View get(final int position) {
        return mViewList.get(position);
    }

    public void clear() {
        mViewList.clear();
    }

    @NonNull
    @Override
    public Object instantiateItem(
            @NonNull final ViewGroup container,
            final int position) {
        final View view = mViewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(
            @NonNull final ViewGroup container,
            final int position,
            @NonNull final Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(
            @NonNull final View view,
            @NonNull final Object object) {
        return view == object;
    }
}
