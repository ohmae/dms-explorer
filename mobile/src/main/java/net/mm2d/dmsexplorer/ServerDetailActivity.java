/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.android.cds.MediaServer;

/**
 * メディアサーバの詳細情報を表示するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailActivity extends AppCompatActivity {
    public static final String TAG = "ServerDetailActivity";

    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @param udn     メディアサーバのUDN
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(Context context, String udn) {
        final Intent intent = new Intent(context, ServerDetailActivity.class);
        intent.putExtra(Const.EXTRA_UDN, udn);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_server_detail);
        final String udn = getIntent().getStringExtra(Const.EXTRA_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getMediaServer(udn);
        if (server == null) {
            finish();
            return;
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar == null) {
            finish();
            return;
        }
        toolbar.setBackgroundColor(ThemeUtils.getAccentColor(server.getFriendlyName()));
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(server.getFriendlyName());
        if (savedInstanceState == null) {
            final ServerDetailFragment fragment = ServerDetailFragment.newInstance(udn);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.server_detail_container, fragment)
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
