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
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.adapter.ServerPropertyAdapter;
import net.mm2d.dmsexplorer.util.ToolbarThemeHelper;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import static net.mm2d.dmsexplorer.ServerDetailFragment.setUpGoButton;


/**
 * メディアサーバの詳細情報を表示するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailActivity extends AppCompatActivity {
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
        final Toolbar toolbar = (Toolbar) findViewById(R.id.serverDetailToolbar);
        if (toolbar == null) {
            finish();
            return;
        }
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(server.getFriendlyName());

        ToolbarThemeHelper.setServerDetailTheme(this, server,
                (CollapsingToolbarLayout) findViewById(R.id.toolbarLayout));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.serverDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ServerPropertyAdapter(this, server));

        setUpGoButton(this, findViewById(R.id.fab), udn);

        prepareTransition(savedInstanceState != null);
    }

    private void prepareTransition(boolean recreate) {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        findViewById(R.id.toolbarIcon).setTransitionName(Const.SHARE_ELEMENT_NAME_DEVICE_ICON);
        if (recreate) {
            return;
        }
        final View background = findViewById(R.id.toolbarBackground);
        background.setVisibility(View.INVISIBLE);
        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @TargetApi(VERSION_CODES.KITKAT)
            @Override
            public void onTransitionEnd(final Transition transition) {
                transition.removeListener(this);
                startAnimation(background);
            }
        });
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void startAnimation(final @NonNull View background) {
        background.setVisibility(View.VISIBLE);

        final Resources res = getResources();
        final float iconRadius = res.getDimension(R.dimen.expanded_toolbar_icon_radius);
        final float iconMargin = res.getDimension(R.dimen.expanded_toolbar_icon_margin);
        final float iconCenter = iconRadius + iconMargin;
        final float cx = background.getWidth() - iconCenter;
        final float cy = background.getHeight() - iconCenter;

        ViewAnimationUtils.createCircularReveal(background,
                (int) cx, (int) cy, iconRadius, (float) Math.sqrt(cx * cx + cy * cy)).start();
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
