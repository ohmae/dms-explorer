/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class Repository {
    private static final Repository INSTANCE = new Repository();

    public static Repository getInstance() {
        return INSTANCE;
    }

    private ControlPointModel mControlPointModel;
    private MediaServerModel mMediaServerModel;

    private Repository() {
    }

    public void initialize(@NonNull Context context) {
        mControlPointModel = new ControlPointModel(context);
    }

    public ControlPointModel getControlPointModel() {
        return mControlPointModel;
    }

    public void updateMediaServer(@Nullable MediaServer server) {
        if (mMediaServerModel != null) {
            mMediaServerModel.terminate();
            mMediaServerModel = null;
        }
        if (server != null) {
            mMediaServerModel = new MediaServerModel(server);
            mMediaServerModel.initialize();
        }
    }

    @Nullable
    public MediaServerModel getMediaServerModel() {
        return mMediaServerModel;
    }
}
