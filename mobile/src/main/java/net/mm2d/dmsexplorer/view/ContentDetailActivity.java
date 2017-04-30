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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;

/**
 * CDSアイテムの詳細情報を表示するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ContentDetailActivity extends AppCompatActivity {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(@NonNull final Context context) {
        return new Intent(context, ContentDetailActivity.class);
    }

    private MediaServerModel mMediaServerModel;
    private CdsObject mCdsObject;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_detail_activity);
        final Repository repository = Repository.get();
        mMediaServerModel = repository.getMediaServerModel();
        final ContentDetailFragmentBinding binding =
                DataBindingUtil.findBinding(findViewById(R.id.cds_detail_fragment));
        if (binding == null) {
            finish();
            return;
        }
        mCdsObject = getSelectedObject();
        setSupportActionBar(binding.cdsDetailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        repository.getThemeModel().setThemeColor(this, binding.getModel().collapsedColor, 0);
    }

    private CdsObject getSelectedObject() {
        return mMediaServerModel == null ? null : mMediaServerModel.getSelectedObject();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCdsObject != null && !mCdsObject.equals(getSelectedObject())) {
            finish();
        }
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
}
