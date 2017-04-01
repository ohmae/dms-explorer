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
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class DataHolder {
    private static final DataHolder INSTANCE = new DataHolder();

    public static DataHolder getInstance() {
        return INSTANCE;
    }

    private ControlPointModel mControlPointModel;
    private CdsTreeModel mCdsTreeModel;

    private DataHolder() {
    }

    public void initialize(@NonNull Context context) {
        mControlPointModel = new ControlPointModel(context);
    }

    public ControlPointModel getControlPointModel() {
        return mControlPointModel;
    }

    public void selectMediaServer(@Nullable MediaServer server) {
        if (mCdsTreeModel != null) {
            mCdsTreeModel.terminate();
            mCdsTreeModel = null;
        }
        if (server != null) {
            mCdsTreeModel = new CdsTreeModel(server);
            mCdsTreeModel.initialize();
        }
    }

    public CdsTreeModel getCdsTreeModel() {
        return mCdsTreeModel;
    }
}
