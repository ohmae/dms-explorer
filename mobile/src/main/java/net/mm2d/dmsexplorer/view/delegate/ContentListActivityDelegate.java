/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog.OnDeleteListener;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel.CdsSelectListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class ContentListActivityDelegate implements CdsSelectListener, OnDeleteListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";

    public static ContentListActivityDelegate create(@NonNull final BaseActivity activity) {
        final ContentListActivityBinding binding = DataBindingUtil.setContentView(activity, R.layout.content_list_activity);
        if (binding.cdsDetailContainer == null) {
            return new ContentListActivityDelegateOnePane(activity, binding);
        }
        return new ContentListActivityDelegateTwoPane(activity, binding);
    }

    private final BaseActivity mActivity;
    private final ContentListActivityBinding mBinding;
    private ContentListActivityModel mModel;

    ContentListActivityDelegate(
            @NonNull final BaseActivity activity,
            @NonNull final ContentListActivityBinding binding) {
        mActivity = activity;
        mBinding = binding;
    }

    @NonNull
    protected BaseActivity getActivity() {
        return mActivity;
    }

    @NonNull
    public ContentListActivityBinding getBinding() {
        return mBinding;
    }

    @Nullable
    protected ContentListActivityModel getModel() {
        return mModel;
    }

    protected abstract boolean isTwoPane();

    @CallSuper
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        final BaseActivity activity = getActivity();
        final Repository repository = Repository.get();
        try {
            mModel = new ContentListActivityModel(getActivity(), repository, this, isTwoPane());
        } catch (final IllegalStateException ignored) {
            activity.finish();
            return;
        }
        final ContentListActivityBinding binding = getBinding();

        binding.toolbar.setPopupTheme(new Settings(activity).getThemeParams().getPopupThemeId());
        binding.setModel(mModel);
        activity.setSupportActionBar(mBinding.toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            restoreScroll(savedInstanceState);
        }
        repository.getThemeModel().setThemeColor(getActivity(), mModel.toolbarBackground, 0);
    }

    @CallSuper
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        if (mModel == null) {
            return;
        }
        saveScroll(outState);
    }

    @CallSuper
    public void onStart() {
        if (mModel == null) {
            return;
        }
        mModel.syncSelectedEntity();
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
        final RecyclerView recyclerView = mBinding.recyclerView;
        if (recyclerView.getChildCount() == 0) {
            return;
        }
        final View view = recyclerView.getChildAt(0);
        outState.putInt(KEY_SCROLL_POSITION, recyclerView.getChildAdapterPosition(view));
        outState.putInt(KEY_SCROLL_OFFSET, -view.getTop());
    }

    public boolean onBackPressed() {
        return mModel != null && mModel.onBackPressed();
    }

    public boolean onKeyLongPress(
            final int keyCode,
            final KeyEvent event) {
        if (mModel != null && keyCode == KeyEvent.KEYCODE_BACK) {
            mModel.terminate();
            return true;
        }
        return false;
    }
}
