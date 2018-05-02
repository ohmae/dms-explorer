/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding;
import net.mm2d.dmsexplorer.log.EventLogger;
import net.mm2d.dmsexplorer.view.ContentListActivity;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel;
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel.ServerSelectListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class ServerListActivityDelegate implements ServerSelectListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";

    public static ServerListActivityDelegate create(@NonNull final BaseActivity activity) {
        final ServerListActivityBinding binding = DataBindingUtil.setContentView(activity, R.layout.server_list_activity);
        if (binding.serverDetailContainer == null) {
            return new ServerListActivityDelegateOnePane(activity, binding);
        }
        return new ServerListActivityDelegateTwoPane(activity, binding);
    }

    private final BaseActivity mActivity;
    private final ServerListActivityBinding mBinding;

    ServerListActivityDelegate(
            @NonNull final BaseActivity activity,
            @NonNull final ServerListActivityBinding binding) {
        mActivity = activity;
        mBinding = binding;
    }

    protected BaseActivity getActivity() {
        return mActivity;
    }

    protected ServerListActivityBinding getBinding() {
        return mBinding;
    }

    protected abstract boolean isTwoPane();

    @CallSuper
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        getBinding().setModel(new ServerListActivityModel(getActivity(), Repository.get(), this, isTwoPane()));

        getActivity().setSupportActionBar(getBinding().toolbar);
        getActivity().getSupportActionBar().setTitle(R.string.title_device_select);

        if (savedInstanceState != null) {
            restoreScroll(savedInstanceState);
        }
    }

    public void prepareSaveInstanceState() {
    }

    @CallSuper
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        saveScroll(outState);
    }

    public void onStart() {
    }

    private void restoreScroll(@NonNull final Bundle savedInstanceState) {
        final int position = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        final int offset = savedInstanceState.getInt(KEY_SCROLL_OFFSET, 0);
        if (position == 0 && offset == 0) {
            return;
        }
        final RecyclerView recyclerView = mBinding.recyclerView;
        ViewUtils.execOnLayout(recyclerView, () -> {
            recyclerView.scrollToPosition(position);
            recyclerView.post(() -> recyclerView.scrollBy(0, offset));
        });
    }

    private void saveScroll(@NonNull final Bundle outState) {
        final RecyclerView recyclerView = getBinding().recyclerView;
        if (recyclerView.getChildCount() == 0) {
            return;
        }
        final View view = recyclerView.getChildAt(0);
        outState.putInt(KEY_SCROLL_POSITION, recyclerView.getChildAdapterPosition(view));
        outState.putInt(KEY_SCROLL_OFFSET, -view.getTop());
    }

    protected static void startCdsListActivity(
            @NonNull final Context context,
            @NonNull final View v) {
        final Intent intent = ContentListActivity.makeIntent(context);
        context.startActivity(intent, ActivityOptions.makeScaleUpAnimation(
                v, 0, 0, v.getWidth(), v.getHeight())
                .toBundle());
        EventLogger.sendSelectServer();
    }
}
