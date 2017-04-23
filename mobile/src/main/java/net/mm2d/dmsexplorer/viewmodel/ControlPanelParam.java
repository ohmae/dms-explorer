/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.ColorInt;

import net.mm2d.dmsexplorer.BR;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ControlPanelParam extends BaseObservable {
    private int mBottomPadding;
    private int mMarginRight;
    private int mBackgroundColor;

    @Bindable
    public int getBottomPadding() {
        return mBottomPadding;
    }

    public void setBottomPadding(final int padding) {
        mBottomPadding = padding;
        notifyPropertyChanged(BR.bottomPadding);
    }

    @Bindable
    public int getMarginRight() {
        return mMarginRight;
    }

    public void setMarginRight(final int margin) {
        mMarginRight = margin;
        notifyPropertyChanged(BR.marginRight);
    }

    @Bindable
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(@ColorInt final int color) {
        mBackgroundColor = color;
        notifyPropertyChanged(BR.backgroundColor);
    }
}
