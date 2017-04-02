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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding;
import net.mm2d.dmsexplorer.util.ThemeUtils;


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
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(Context context) {
        return new Intent(context, ServerDetailActivity.class);
    }

    private ServerDetailFragmentBinding mBinding;

    @Nullable
    private ServerDetailFragmentBinding getBinding() {
        final ServerDetailFragment fragment = (ServerDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.serverDetailFragment);
        if (fragment == null) {
            return null;
        }
        return fragment.getBinding();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_detail_activity);
        mBinding = getBinding();
        if (mBinding == null) {
            finish();
            return;
        }
        setSupportActionBar(mBinding.serverDetailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ThemeUtils.getDarkerColor(mBinding.getModel().collapsedColor));
        }

        final boolean hasTransition = getIntent().getBooleanExtra(Const.EXTRA_HAS_TRANSITION, false);
        prepareTransition(hasTransition && savedInstanceState == null);
    }

    private void prepareTransition(boolean hasTransition) {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        mBinding.toolbarIcon.setTransitionName(Const.SHARE_ELEMENT_NAME_DEVICE_ICON);
        if (!hasTransition) {
            return;
        }
        mBinding.toolbarBackground.setVisibility(View.INVISIBLE);
        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @TargetApi(VERSION_CODES.KITKAT)
            @Override
            public void onTransitionEnd(final Transition transition) {
                transition.removeListener(this);
                startAnimation(mBinding.toolbarBackground);
            }
        });
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void startAnimation(final @NonNull View background) {
        if (!background.isAttachedToWindow()) {
            return;
        }
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
