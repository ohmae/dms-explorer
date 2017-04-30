/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.ActivityUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel;
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel.CdsSelectListener;

/**
 * MediaServerのContentDirectoryを表示、操作するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ContentListActivity extends AppCompatActivity implements CdsSelectListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";
    private boolean mTwoPane;
    private ContentDetailFragment mContentDetailFragment;
    private ContentListActivityBinding mBinding;
    private ContentListActivityModel mModel;

    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    @NonNull
    public static Intent makeIntent(@NonNull final Context context) {
        return new Intent(context, ContentListActivity.class);
    }

    @Override
    public void onSelect(@NonNull final View v,
                         @NonNull final CdsObject object,
                         final boolean alreadySelected) {
        if (mTwoPane) {
            if (alreadySelected) {
                if (object.hasProtectedResource()) {
                    Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
                    return;
                }
                ItemSelectUtils.play(this, 0);
                return;
            }
            setDetailFragment(true);
        } else {
            startDetailActivity(v);
        }
    }

    @Override
    public void onUnselect() {
        removeDetailFragment();
    }

    @Override
    public void onDetermine(@NonNull final View v,
                            @NonNull final CdsObject object,
                            final boolean alreadySelected) {
        if (object.hasProtectedResource()) {
            if (!alreadySelected) {
                setDetailFragment(true);
            }
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            return;
        }
        ItemSelectUtils.play(this, 0);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.content_list_activity);
        final Repository repository = Repository.get();
        try {
            mModel = new ContentListActivityModel(this, repository, this);
            mBinding.setModel(mModel);
        } catch (final IllegalStateException ignored) {
            return;
        }
        mTwoPane = mBinding.cdsDetailContainer != null;

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            restoreScroll(savedInstanceState);
        }
        repository.getThemeModel().setThemeColor(this, mModel.toolbarBackground, 0);
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

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScroll(outState);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (mModel == null) {
            return;
        }
        mModel.syncSelectedObject();
        if (mModel.isItemSelected()) {
            setDetailFragment(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mModel.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mModel.terminate();
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(SettingsActivity.makeIntent(this));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startDetailActivity(@NonNull final View v) {
        final Intent intent = ContentDetailActivity.makeIntent(v.getContext());
        startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v));
    }

    private void setDetailFragment(final boolean animate) {
        if (!mTwoPane) {
            return;
        }
        mContentDetailFragment = ContentDetailFragment.newInstance();
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mContentDetailFragment.setEnterTransition(new Slide(Gravity.START));
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cds_detail_container, mContentDetailFragment)
                .commit();
    }

    private void removeDetailFragment() {
        if (!mTwoPane || mContentDetailFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mContentDetailFragment)
                .commit();
        mContentDetailFragment = null;
    }
}
