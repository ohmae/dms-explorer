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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.DmcActivityBinding;
import net.mm2d.dmsexplorer.viewmodel.DmcActivityModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class DmcActivity extends AppCompatActivity {

    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context     コンテキスト
     * @param serverUdn   MediaServerのUDN
     * @param object      再生するCdsObject
     * @param uri         再生するURI
     * @param rendererUdn MediaRendererのUDN
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(final @NonNull Context context,
                                    final @NonNull String serverUdn,
                                    final @NonNull CdsObject object,
                                    final @NonNull String uri,
                                    final @NonNull String rendererUdn) {
        final Intent intent = new Intent(context, DmcActivity.class);
        intent.putExtra(Const.EXTRA_SERVER_UDN, serverUdn);
        intent.putExtra(Const.EXTRA_OBJECT, object);
        intent.putExtra(Const.EXTRA_URI, uri);
        intent.putExtra(Const.EXTRA_RENDERER_UDN, rendererUdn);
        return intent;
    }

    private DmcActivityBinding mBinding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.dmc_activity);
        final Intent intent = getIntent();
        final String uri = intent.getStringExtra(Const.EXTRA_URI);
        final DmcActivityModel model = DmcActivityModel.create(this, uri);
        if (model == null) {
            finish();
            return;
        }
        mBinding.setModel(model);

        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        model.initialize();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.getModel().terminate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
