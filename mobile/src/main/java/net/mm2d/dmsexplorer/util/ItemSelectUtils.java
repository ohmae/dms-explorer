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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.view.DmcActivity;
import net.mm2d.dmsexplorer.view.MovieActivity;
import net.mm2d.dmsexplorer.view.MusicActivity;
import net.mm2d.dmsexplorer.view.PhotoActivity;
import net.mm2d.dmsexplorer.view.dialog.SelectDeviceDialog;
import net.mm2d.dmsexplorer.view.dialog.SelectResourceDialog;

import java.util.List;

/**
 * Item選択後の処理をまとめるクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ItemSelectUtils {
    public static void play(final @NonNull Activity activity,
                            final @NonNull CdsObject object) {
        final List<Tag> list = object.getTagList(CdsObject.RES);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (list.size() == 1) {
            play(activity, object, 0);
            return;
        }
        final SelectResourceDialog dialog = SelectResourceDialog.newInstance(object);
        dialog.show(activity.getFragmentManager(), "");
    }

    public static void play(final @NonNull Activity activity,
                            final @NonNull CdsObject object, final int index) {
        final PlaybackTargetModel targetModel = Repository.getInstance().getPlaybackTargetModel();
        targetModel.setResIndex(index);
        if (targetModel.getUri() == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(targetModel.getUri(), targetModel.getMimeType());
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        switch (object.getType()) {
            case CdsObject.TYPE_VIDEO:
                if (pref.getBoolean(Const.LAUNCH_APP_MOVIE, true)) {
                    intent.setClass(activity, MovieActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_AUDIO:
                if (pref.getBoolean(Const.LAUNCH_APP_MUSIC, true)) {
                    intent.setClass(activity, MusicActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_IMAGE:
                if (pref.getBoolean(Const.LAUNCH_APP_PHOTO, true)) {
                    intent.setClass(activity, PhotoActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
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

    public static void send(final @NonNull Activity activity) {
        if (Repository.getInstance().getControlPointModel().getMrControlPoint().getDeviceListSize() == 0) {
            return;
        }
        final SelectDeviceDialog dialog = SelectDeviceDialog.newInstance();
        dialog.show(activity.getFragmentManager(), "");
    }

    public static void send(final @NonNull Context context, final @NonNull MediaRenderer renderer) {
        Repository.getInstance().getControlPointModel().setSelectedMediaRenderer(renderer);
        context.startActivity(DmcActivity.makeIntent(context));
    }
}
