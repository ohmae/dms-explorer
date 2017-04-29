/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.Repository;
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
    public static void play(@NonNull final Activity activity) {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        final int resCount = targetModel.getResCount();
        if (resCount == 0) {
            return;
        }
        if (resCount == 1) {
            play(activity, 0);
            return;
        }
        final SelectResourceDialog dialog = SelectResourceDialog.newInstance();
        dialog.show(activity.getFragmentManager(), "");
    }

    public static void play(@NonNull final Activity activity, final int index) {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        targetModel.setResIndex(index);
        if (targetModel.getUri() == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(targetModel.getUri(), targetModel.getMimeType());
        final Settings settings = new Settings(activity);
        switch (targetModel.getCdsObject().getType()) {
            case CdsObject.TYPE_VIDEO:
                if (settings.isPlayMovieMyself()) {
                    intent.setClass(activity, MovieActivity.class);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_AUDIO:
                if (settings.isPlayMusicMyself()) {
                    intent.setClass(activity, MusicActivity.class);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_IMAGE:
                if (settings.isPlayPhotoMyself()) {
                    intent.setClass(activity, PhotoActivity.class);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            default:
                return;
        }
        try {
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        } catch (final ActivityNotFoundException ignored) {
        }
    }

    public static void send(@NonNull final Activity activity) {
        final SelectRendererDialog dialog = SelectRendererDialog.newInstance();
        dialog.show(activity.getFragmentManager(), "");
    }

    public static void sendSelectedRenderer(@NonNull final Context context) {
        context.startActivity(DmcActivity.makeIntent(context));
    }
}
