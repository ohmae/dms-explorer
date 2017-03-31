/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class DataHolder {
    private static final DataHolder INSTANCE = new DataHolder();

    public static DataHolder getInstance() {
        return INSTANCE;
    }

    private ControlPointModel mControlPointModel;

    private DataHolder() {
    }

    public void initialize(@NonNull Context context) {
        mControlPointModel = new ControlPointModel(context);
    }

    public ControlPointModel getControlPointModel() {
        return mControlPointModel;
    }

    private static class Cache {
        private final String mId;
        private final List<CdsObject> mList;

        public Cache(String id, List<CdsObject> list) {
            mId = id;
            mList = list;
        }

        public String getId() {
            return mId;
        }

        public List<CdsObject> getList() {
            return mList;
        }
    }

    private final LinkedList<Cache> mCacheQueue = new LinkedList<>();

    public void clearCache() {
        mCacheQueue.clear();
    }

    public void popCache() {
        mCacheQueue.pop();
    }

    public void pushCache(String id, List<CdsObject> list) {
        mCacheQueue.push(new Cache(id, list));
    }

    public String getCurrentContainer() {
        return mCacheQueue.peek().getId();
    }

    public List<CdsObject> getCurrentList() {
        return mCacheQueue.peek().getList();
    }
}
