/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public abstract class Repository {
    private static Repository sInstance;

    public static Repository get() {
        return sInstance;
    }

    static void set(@NonNull Repository instance) {
        sInstance = instance;
    }

    public abstract OpenUriModel getOpenUriModel();

    public abstract ControlPointModel getControlPointModel();

    public abstract MediaServerModel getMediaServerModel();

    public abstract PlayerModel getMediaRendererModel();

    public abstract PlaybackTargetModel getPlaybackTargetModel();
}
