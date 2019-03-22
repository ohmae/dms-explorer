/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.content.Intent;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.mm2d.android.util.ActivityUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.view.ContentDetailActivity;
import net.mm2d.dmsexplorer.view.base.BaseActivity;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ContentListActivityDelegateOnePane extends ContentListActivityDelegate {

    ContentListActivityDelegateOnePane(
            @NonNull final BaseActivity activity,
            @NonNull final ContentListActivityBinding binding) {
        super(activity, binding);
    }

    @Override
    protected boolean isTwoPane() {
        return false;
    }

    @Override
    public void onSelect(
            @NonNull final View v,
            @NonNull final ContentEntity entity) {
        startDetailActivity(v);
    }

    @Override
    public void onLostSelection() {
    }

    @Override
    public void onExecute(
            @NonNull final View v,
            @NonNull final ContentEntity object,
            final boolean selected) {
        if (object.isProtected()) {
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            return;
        }
        ItemSelectUtils.play(getActivity(), 0);
    }

    private void startDetailActivity(@NonNull final View v) {
        final Intent intent = ContentDetailActivity.makeIntent(getActivity());
        getActivity().startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v));
    }

    @Override
    public void onDelete() {
    }
}
