/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.util.Rational;
import android.view.View;

import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel;
import net.mm2d.log.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class MovieActivityPipHelperOreo implements MovieActivityPipHelper {
    @NonNull
    private final Activity mActivity;
    @Nullable
    private ControlPanelModel mControlPanelModel;
    @NonNull
    private final BroadcastReceiver mControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                Context context,
                Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.isEmpty(action) || mControlPanelModel == null) {
                return;
            }
            switch (intent.getAction()) {
                case Const.ACTION_PLAY:
                    mControlPanelModel.onClickPlayPause();
                    final PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                    builder.setActions(makeActions(mControlPanelModel.isPlaying()));
                    mActivity.setPictureInPictureParams(builder.build());
                    break;
                case Const.ACTION_NEXT:
                    mControlPanelModel.onClickNext();
                    break;
                case Const.ACTION_PREV:
                    mControlPanelModel.onClickPrevious();
                    break;
            }
        }
    };

    MovieActivityPipHelperOreo(@NonNull final Activity activity) {
        mActivity = activity;
    }

    @Override
    public void register() {
        mActivity.registerReceiver(mControlReceiver, makeIntentFilter());
    }

    @NonNull
    private IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_PLAY);
        intentFilter.addAction(Const.ACTION_NEXT);
        intentFilter.addAction(Const.ACTION_PREV);
        return intentFilter;
    }

    @Override
    public void unregister() {
        mActivity.unregisterReceiver(mControlReceiver);
    }

    @Override
    public void setControlPanelModel(@Nullable final ControlPanelModel model) {
        mControlPanelModel = model;
    }

    @Override
    public void enterPictureInPictureMode(@NonNull final View contentView) {
        final PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        final List<RemoteAction> actions = makeActions(mControlPanelModel.isPlaying());
        if (!actions.isEmpty()) {
            builder.setActions(actions);
        }
        final Rect rect = makeViewRect(contentView);
        if (rect.width() > 0 && rect.height() > 0) {
            builder.setSourceRectHint(rect)
                    .setAspectRatio(new Rational(rect.width(), rect.height()));
        }
        try {
            mActivity.enterPictureInPictureMode(builder.build());
        } catch (final Exception e) {
            Log.w(e);
        }
    }

    @NonNull
    private Rect makeViewRect(@NonNull final View v) {
        final Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        return rect;
    }

    @NonNull
    private List<RemoteAction> makeActions(final boolean isPlaying) {
        final int max = mActivity.getMaxNumPictureInPictureActions();
        if (max <= 0) {
            return Collections.emptyList();
        }
        if (max >= 3) {
            return Arrays.asList(
                    makePreviousAction(),
                    makePlayAction(isPlaying),
                    makeNextAction()
            );
        }
        return Collections.singletonList(makePlayAction(isPlaying));
    }

    @NonNull
    private RemoteAction makePlayAction(final boolean isPlaying) {
        return new RemoteAction(
                makeIcon(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play),
                getString(R.string.action_play_title),
                getString(R.string.action_play_description),
                makePlayPendingIntent(mActivity));
    }

    @NonNull
    private RemoteAction makeNextAction() {
        return new RemoteAction(
                makeIcon(R.drawable.ic_skip_next),
                getString(R.string.action_next_title),
                getString(R.string.action_next_description),
                makeNextPendingIntent(mActivity));
    }

    @NonNull
    private RemoteAction makePreviousAction() {
        return new RemoteAction(
                makeIcon(R.drawable.ic_skip_previous),
                getString(R.string.action_previous_title),
                getString(R.string.action_previous_description),
                makePreviousPendingIntent(mActivity));
    }

    @NonNull
    private String getString(@StringRes final int resId) {
        return mActivity.getResources().getText(resId, "").toString();
    }

    private Icon makeIcon(@DrawableRes final int resId) {
        return Icon.createWithResource(mActivity, resId);
    }

    @NonNull
    private static PendingIntent makePlayPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_PLAY,
                new Intent(Const.ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private static PendingIntent makeNextPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_NEXT,
                new Intent(Const.ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private static PendingIntent makePreviousPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_PREVIOUS,
                new Intent(Const.ACTION_PREV),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
