/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding;
import net.mm2d.dmsexplorer.view.ServerDetailFragment;
import net.mm2d.dmsexplorer.view.base.BaseActivity;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ServerListActivityDelegateTwoPane extends ServerListActivityDelegate {
    private Fragment mFragment;

    ServerListActivityDelegateTwoPane(
            @NonNull final BaseActivity activity,
            @NonNull final ServerListActivityBinding binding) {
        super(activity, binding);
    }

    @Override
    protected boolean isTwoPane() {
        return true;
    }

    @Override
    public void onSelect(@NonNull final View v) {
        setDetailFragment(true);
    }

    @Override
    public void onLostSelection() {
        removeDetailFragment();
    }

    @Override
    public void onExecute(@NonNull final View v) {
        startCdsListActivity(getActivity(), v);
    }

    @Override
    public void prepareSaveInstanceState() {
        removeDetailFragment();
    }

    @Override
    public void onStart() {
        getBinding().getModel().updateListAdapter();
        updateFragmentState();
    }

    private void setDetailFragment(boolean animate) {
        mFragment = ServerDetailFragment.newInstance();
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("RtlHardcoded")
            final int gravity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                    ? Gravity.START : Gravity.LEFT;
            mFragment.setEnterTransition(new Slide(gravity));
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.server_detail_container, mFragment)
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

    private void updateFragmentState() {
        if (getBinding().getModel().hasSelectedMediaServer()) {
            setDetailFragment(false);
            return;
        }
        removeDetailFragment();
    }
}
