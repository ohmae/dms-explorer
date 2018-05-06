/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.view.delegate.ContentListActivityDelegate;
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog.OnDeleteListener;

/**
 * MediaServerのContentDirectoryを表示、操作するActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentListActivity extends BaseActivity implements OnDeleteListener {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    @NonNull
    public static Intent makeIntent(@NonNull final Context context) {
        return new Intent(context, ContentListActivity.class);
    }

    private Settings mSettings;
    private ContentListActivityDelegate mDelegate;

    public ContentListActivity() {
        super(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        mSettings = new Settings(this);
        setTheme(mSettings.getThemeParams().getListThemeId());
        super.onCreate(savedInstanceState);
        mDelegate = ContentListActivityDelegate.create(this);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void updateOrientationSettings() {
        mSettings.getBrowseOrientation()
                .setRequestedOrientation(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDelegate.onStart();
    }

    @Override
    public void onBackPressed() {
        if (mDelegate.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyLongPress(
            final int keyCode,
            final KeyEvent event) {
        if (mDelegate.onKeyLongPress(keyCode, event)) {
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onDelete() {
        mDelegate.onDelete();
    }
}
