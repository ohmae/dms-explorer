/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.android.cds.CdsObject;
import net.mm2d.android.cds.MediaServer;
import net.mm2d.android.util.AribUtils;

/**
 * CDSアイテムの詳細情報を表示するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsDetailActivity extends AppCompatActivity {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @param udn     メディアサーバのUDN
     * @param object  表示するコンテンツのObject情報
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(@NonNull Context context, @NonNull String udn, @NonNull CdsObject object) {
        final Intent intent = new Intent(context, CdsDetailActivity.class);
        intent.putExtra(Const.EXTRA_UDN, udn);
        intent.putExtra(Const.EXTRA_OBJECT, object);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_cds_detail);

        final CdsObject object = getIntent().getParcelableExtra(Const.EXTRA_OBJECT);
        final String udn = getIntent().getStringExtra(Const.EXTRA_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getMediaServer(udn);

        final String rawTitle = object.getTitle();
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ThemeUtils.getAccentDarkColor(rawTitle));
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar == null || server == null) {
            finish();
            return;
        }
        toolbar.setBackgroundColor(ThemeUtils.getAccentColor(rawTitle));
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        final String title = AribUtils.toDisplayableString(rawTitle);
        actionBar.setTitle(title);
        if (savedInstanceState == null) {
            final CdsDetailFragment fragment = CdsDetailFragment.newInstance(udn, object);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.cds_detail_container, fragment)
                    .commit();
        }
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
}
