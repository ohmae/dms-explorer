/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.view.delegate.ServerListActivityDelegate;

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 * <p>アプリ起動時最初に表示されるActivity
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerListActivity extends BaseActivity {
    private ControlPointModel mControlPointModel;

    private ServerListActivityDelegate mDelegate;

    public ServerListActivity() {
        super(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Repository repository = Repository.get();
        mControlPointModel = repository.getControlPointModel();

        repository.getThemeModel().setThemeColor(this,
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.defaultStatusBar));

        if (savedInstanceState == null) {
            mControlPointModel.initialize();
        }
        mDelegate = ServerListActivityDelegate.create(this);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mControlPointModel.terminate();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        mDelegate.prepareSaveInstanceState();
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mControlPointModel.searchStart();
        mDelegate.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mControlPointModel.searchStop();
    }
}
