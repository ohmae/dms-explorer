/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.android.upnp.cds.MediaServer;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import static net.mm2d.dmsexplorer.ServerDetailFragment.setUpGoButton;


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
        intent.putExtra(Const.EXTRA_SERVER_UDN, udn);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String udn = getIntent().getStringExtra(Const.EXTRA_SERVER_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getDevice(udn);
        if (server == null) {
            finish();
            return;
        }
        setContentView(R.layout.server_detail_activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.server_detail_toolbar);
        if (toolbar == null) {
            finish();
            return;
        }
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(server.getFriendlyName());

        ToolbarThemeHelper.setServerDetailTheme(this, server,
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.server_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ServerPropertyAdapter(this, server));

        setUpGoButton(this, findViewById(R.id.fab), udn);

        prepareTransition();
    }

    @Override
    public void finishAfterTransition() {
        super.finishAfterTransition();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void prepareTransition() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.toolbar_icon).setTransitionName(Const.SHARE_ELEMENT_NAME_ICON);
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
