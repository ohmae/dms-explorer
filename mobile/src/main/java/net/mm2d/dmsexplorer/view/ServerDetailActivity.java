/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;

import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ServerDetailFragmentModel;


/**
 * メディアサーバの詳細情報を表示するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerDetailActivity extends BaseActivity {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(@NonNull final Context context) {
        return new Intent(context, ServerDetailActivity.class);
    }

    private Settings mSettings;
    private ServerDetailFragmentBinding mBinding;

    public ServerDetailActivity() {
        super(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        mSettings = new Settings(this);
        setTheme(mSettings.getThemeParams().getNoActionBarThemeId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_detail_activity);
        mBinding = DataBindingUtil.findBinding(findViewById(R.id.server_detail_fragment));
        if (mBinding == null) {
            finish();
            return;
        }
        mBinding.serverDetailToolbar.setPopupTheme(mSettings.getThemeParams().getPopupThemeId());
        setSupportActionBar(mBinding.serverDetailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ServerDetailFragmentModel model = mBinding.getModel();
        if (model != null) {
            Repository.get().getThemeModel().setThemeColor(this, model.collapsedColor, 0);
        }

        prepareTransition(savedInstanceState != null);
    }

    @Override
    protected void updateOrientationSettings() {
        mSettings.getBrowseOrientation()
                .setRequestedOrientation(this);
    }

    private void prepareTransition(final boolean hasState) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        mBinding.toolbarIcon.setTransitionName(Const.SHARE_ELEMENT_NAME_DEVICE_ICON);
        if (hasState) {
            return;
        }
        mBinding.toolbarBackground.setVisibility(View.INVISIBLE);
        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTransitionEnd(final Transition transition) {
                transition.removeListener(this);
                startAnimation(mBinding.toolbarBackground);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAnimation(@NonNull final View background) {
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
}
