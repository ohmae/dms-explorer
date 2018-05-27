/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.transition.Slide;
import android.support.transition.Transition;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.view.ContentDetailFragment;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ContentListActivityDelegateTwoPane extends ContentListActivityDelegate {
    private Fragment mFragment;

    ContentListActivityDelegateTwoPane(
            @NonNull final BaseActivity activity,
            @NonNull final ContentListActivityBinding binding) {
        super(activity, binding);
    }

    @Override
    protected boolean isTwoPane() {
        return true;
    }

    @Override
    public void onSelect(
            @NonNull final View v,
            @NonNull final ContentEntity entity) {
        setDetailFragment(true);
    }

    @Override
    public void onLostSelection() {
        removeDetailFragment();
    }

    @Override
    public void onExecute(
            @NonNull final View v,
            @NonNull final ContentEntity entity,
            final boolean selected) {
        if (entity.isProtected()) {
            if (!selected) {
                setDetailFragment(true);
            }
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            return;
        }
        ItemSelectUtils.play(getActivity(), 0);
    }

    private void setDetailFragment(final boolean animate) {
        mFragment = ContentDetailFragment.newInstance();
        if (animate) {
            final Transition transition = new Slide(Gravity.BOTTOM)
                    .setDuration(150L)
                    .setInterpolator(new DecelerateInterpolator());
            mFragment.setEnterTransition(transition);
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cds_detail_container, mFragment)
                .commitAllowingStateLoss();
    }

    private void removeDetailFragment() {
        if (mFragment == null) {
            return;
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(mFragment)
                .commitAllowingStateLoss();
        mFragment = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentListActivityModel model = getModel();
        if (model == null) {
            return;
        }
        if (model.isItemSelected()) {
            setDetailFragment(false);
        }
    }

    @Override
    public void onDelete() {
        removeDetailFragment();
    }
}
