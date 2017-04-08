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

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
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
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(final @NonNull Context context) {
        return new Intent(context, DmcActivity.class);
    }

    private DmcActivityBinding mBinding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.dmc_activity);
        final DmcActivityModel model = DmcActivityModel.create(this);
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
        Repository.getInstance().getControlPointModel().clearSelectedRenderer();
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
