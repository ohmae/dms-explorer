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
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.ActivityUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.CdsListActivityBinding;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel;
import net.mm2d.dmsexplorer.viewmodel.CdsListActivityModel;
import net.mm2d.dmsexplorer.viewmodel.CdsListActivityModel.CdsSelectListener;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;

/**
 * MediaServerのContentDirectoryを表示、操作するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsListActivity extends AppCompatActivity implements CdsSelectListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";
    private boolean mTwoPane;
    private final Repository mRepository = Repository.getInstance();
    private CdsTreeModel mCdsTreeModel;
    private CdsDetailFragment mCdsDetailFragment;
    private CdsListActivityBinding mBinding;

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleへの値の設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return インスタンス
     */
    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, CdsListActivity.class);
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
                ItemSelectUtils.play(this, object, 0);
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
        ItemSelectUtils.play(this, object, 0);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCdsTreeModel = mRepository.getCdsTreeModel();
        mBinding = DataBindingUtil.setContentView(this, R.layout.cds_list_activity);
        mBinding.setModel(new CdsListActivityModel(this, this));

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final MediaServer server = mRepository.getControlPointModel().getSelectedMediaServer();
        ToolbarThemeUtils.setCdsListTheme(this, server, mBinding.toolbar);

        mTwoPane = mBinding.cdsDetailContainer != null;
        if (savedInstanceState != null) {
            restoreScroll(savedInstanceState);
        }
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
        removeDetailFragment();
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
        final CdsObject object = mCdsTreeModel.getSelectedObject();
        if (object == null) {
            return;
        }
        if (mTwoPane && object.isItem()) {
            setDetailFragment(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBinding.getModel().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mCdsTreeModel.terminate();
            mCdsTreeModel.initialize();
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        final Intent intent = CdsDetailActivity.makeIntent(v.getContext());
        startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v));
    }

    private void setDetailFragment(final boolean animate) {
        if (!mTwoPane) {
            return;
        }
        mCdsDetailFragment = CdsDetailFragment.newInstance();
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCdsDetailFragment.setEnterTransition(new Slide(Gravity.START));
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cdsDetailContainer, mCdsDetailFragment)
                .commit();
    }

    private void removeDetailFragment() {
        if (!mTwoPane || mCdsDetailFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mCdsDetailFragment)
                .commit();
        mCdsDetailFragment = null;
    }
}
