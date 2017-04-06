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

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;

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
    private MediaRendererModel mMediaRendererModel;

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

    public void updateMediaRenderer(@Nullable MediaRenderer renderer) {
        if (mMediaRendererModel != null) {
            mMediaRendererModel.terminate();
            mMediaRendererModel = null;
        }
        if (renderer != null) {
            mMediaRendererModel = new MediaRendererModel(renderer);
            mMediaRendererModel.initialize();
        }
    }

    @Nullable
    public MediaServerModel getMediaServerModel() {
        return mMediaServerModel;
    }

    @Nullable
    public MediaRendererModel getMediaRendererModel() {
        return mMediaRendererModel;
    }
}
