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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.adapter.CdsPropertyAdapter;
import net.mm2d.dmsexplorer.util.ToolbarThemeHelper;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import static net.mm2d.dmsexplorer.CdsDetailFragment.setUpPlayButton;
import static net.mm2d.dmsexplorer.CdsDetailFragment.setUpSendButton;

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
        intent.putExtra(Const.EXTRA_SERVER_UDN, udn);
        intent.putExtra(Const.EXTRA_OBJECT, object);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cds_detail_activity);

        final String udn = getIntent().getStringExtra(Const.EXTRA_SERVER_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getDevice(udn);
        if (server == null) {
            finish();
            return;
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.cds_detail_toolbar);
        if (toolbar == null) {
            finish();
            return;
        }
        setSupportActionBar(toolbar);

        final CdsObject object = getIntent().getParcelableExtra(Const.EXTRA_OBJECT);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(AribUtils.toDisplayableString(object.getTitle()));

        ToolbarThemeHelper.setCdsDetailTheme(this, object,
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cds_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CdsPropertyAdapter(this, object));

        setUpPlayButton(this, (FloatingActionButton) findViewById(R.id.fab_play), object);
        setUpSendButton(this, (FloatingActionButton) findViewById(R.id.fab_send), udn, object);
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
