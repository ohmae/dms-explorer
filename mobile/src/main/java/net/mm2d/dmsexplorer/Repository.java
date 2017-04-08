/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public abstract class Repository {
    private static Repository sInstance;

    public static Repository get() {
        return sInstance;
    }

    public static void setInstance(@NonNull Repository instance) {
        sInstance = instance;
    }

    public abstract ControlPointModel getControlPointModel();

    public abstract MediaServerModel getMediaServerModel();

    public abstract MediaRendererModel getMediaRendererModel();

    public abstract PlaybackTargetModel getPlaybackTargetModel();
}
