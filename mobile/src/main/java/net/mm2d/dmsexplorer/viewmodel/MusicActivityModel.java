/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;

import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MusicActivityModel {
    public final String title;
    public final ContentPropertyAdapter propertyAdapter;
    public final int accentColor;

    public static MusicActivityModel create(@NonNull final Activity activity,
                                            @NonNull final Repository repository) {
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        if (targetModel == null) {
            return null;
        }
        return new MusicActivityModel(activity, targetModel);
    }

    private MusicActivityModel(@NonNull final Activity activity,
                               @NonNull final PlaybackTargetModel targetModel) {
        title = AribUtils.toDisplayableString(targetModel.getCdsObject().getTitle());
        accentColor = ThemeUtils.getDeepColor(title);
        propertyAdapter = new ContentPropertyAdapter(activity, targetModel.getCdsObject());
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(accentColor));
        }
    }
}
