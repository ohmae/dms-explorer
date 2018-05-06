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
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.DmcActivityBinding;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.DmcActivityModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DmcActivity extends BaseActivity {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(@NonNull final Context context) {
        return new Intent(context, DmcActivity.class);
    }

    private DmcActivityModel mModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DmcActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.dmc_activity);
        try {
            mModel = new DmcActivityModel(this, Repository.get());
            binding.setModel(mModel);
            mModel.initialize();
        } catch (final IllegalStateException ignored) {
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null) {
            mModel.terminate();
        }
        Repository.get().getControlPointModel().clearSelectedRenderer();
    }

    @Override
    protected void updateOrientationSettings() {
        new Settings(this).getDmcOrientation()
                .setRequestedOrientation(this);
    }
}
