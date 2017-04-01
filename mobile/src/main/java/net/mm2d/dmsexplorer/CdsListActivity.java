/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.databinding.CdsListActivityBinding;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel;
import net.mm2d.dmsexplorer.model.CdsListActivityModel;
import net.mm2d.dmsexplorer.model.CdsListActivityModel.CdsSelectListener;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;

/**
 * MediaServerのContentDirectoryを表示、操作するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsListActivity extends AppCompatActivity implements CdsSelectListener {
    private boolean mTwoPane;
    private final DataHolder mDataHolder = DataHolder.getInstance();
    private CdsTreeModel mCdsTreeModel;
    private CdsDetailFragment mCdsDetailFragment;
    private CdsListActivityBinding mBinding;

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleへの値の設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @param udn     MediaServerのUDN
     * @return インスタンス
     */
    @NonNull
    public static Intent makeIntent(@NonNull Context context, @NonNull String udn) {
        final Intent intent = new Intent(context, CdsListActivity.class);
        intent.putExtra(Const.EXTRA_SERVER_UDN, udn);
        return intent;
    }

    @Override
    public void onSelect(@NonNull final View v, final boolean alreadySelected) {
        final CdsObject object = mCdsTreeModel.getSelectedObject();
        if (mTwoPane) {
            if (alreadySelected) {
                if (object.hasProtectedResource()) {
                    Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
                    return;
                }
                ItemSelectUtils.play(this, object, 0);
                return;
            }
            setDetailFragment(object, true);
        } else {
            startDetailActivity(v, object);
        }
    }

    @Override
    public void onUnselect() {
        removeDetailFragment();
    }

    @Override
    public void onDetermine(@NonNull final View v, final boolean alreadySelected) {
        final CdsObject object = mCdsTreeModel.getSelectedObject();
        if (object.hasProtectedResource()) {
            if (!alreadySelected) {
                setDetailFragment(object, true);
            }
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            return;
        }
        ItemSelectUtils.play(this, object, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCdsTreeModel = mDataHolder.getCdsTreeModel();
        mBinding = DataBindingUtil.setContentView(this, R.layout.cds_list_activity);
        mBinding.setModel(new CdsListActivityModel(this, this));

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String udn = getIntent().getStringExtra(Const.EXTRA_SERVER_UDN);
        final MediaServer server = mDataHolder.getControlPointModel().getMsControlPoint().getDevice(udn);
        ToolbarThemeUtils.setCdsListTheme(this, server, mBinding.toolbar);

        mTwoPane = mBinding.cdsDetailContainer != null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        removeDetailFragment();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final CdsObject object = mCdsTreeModel.getSelectedObject();
        if (mTwoPane && object != null && object.isItem()) {
            setDetailFragment(object, false);
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

    private void startDetailActivity(@NonNull View v, @NonNull CdsObject object) {
        final Intent intent = CdsDetailActivity.makeIntent(v.getContext(), mCdsTreeModel.getUdn(), object);
        startActivity(intent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
    }

    private void setDetailFragment(@NonNull CdsObject object, boolean animate) {
        if (!mTwoPane) {
            return;
        }
        mCdsDetailFragment = CdsDetailFragment.newInstance(mCdsTreeModel.getUdn(), object);
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
