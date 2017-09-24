/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.EventLogger;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.entity.ContentType;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.DmcActivity;
import net.mm2d.dmsexplorer.view.MovieActivity;
import net.mm2d.dmsexplorer.view.MusicActivity;
import net.mm2d.dmsexplorer.view.PhotoActivity;
import net.mm2d.dmsexplorer.view.dialog.SelectRendererDialog;
import net.mm2d.dmsexplorer.view.dialog.SelectResourceDialog;

/**
 * Item選択後の処理をまとめるクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ItemSelectUtils {
    private ItemSelectUtils() {
        throw new AssertionError();
    }

    public static void play(@NonNull final FragmentActivity activity) {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        final int resCount = targetModel.getResCount();
        if (resCount == 0) {
            return;
        }
        if (resCount == 1) {
            play(activity, 0);
            return;
        }
        SelectResourceDialog.show(activity);
    }

    public static void play(
            @NonNull final Activity activity,
            final int index) {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        if (targetModel == null) {
            return;
        }
        targetModel.setResIndex(index);
        if (targetModel.getUri() == Uri.EMPTY) {
            return;
        }
        final ContentType type = targetModel.getContentEntity().getType();
        final Class<?> player = getPlayerClass(type);
        if (player == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(targetModel.getUri(), targetModel.getMimeType());
        final Settings settings = new Settings(activity);
        if (settings.isPlayMyself(type)) {
            intent.setClass(activity, player);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
            EventLogger.sendPlayContent(settings.isPlayMyself(type));
        } catch (final Exception ignored) {
            Toaster.showLong(activity, R.string.toast_launch_error);
        }
    }

    @Nullable
    private static Class<?> getPlayerClass(@NonNull final ContentType type) {
        switch (type) {
            case MOVIE:
                return MovieActivity.class;
            case MUSIC:
                return MusicActivity.class;
            case PHOTO:
                return PhotoActivity.class;
            default:
                return null;
        }
    }

    public static void send(@NonNull final FragmentActivity activity) {
        SelectRendererDialog.show(activity);
    }

    public static void sendSelectedRenderer(@NonNull final Context context) {
        try {
            context.startActivity(DmcActivity.makeIntent(context));
            EventLogger.sendSendContent();
        } catch (final Exception ignored) {
            Toaster.showLong(context, R.string.toast_launch_error);
        }
    }
}
