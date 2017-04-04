/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.avt.MrControlPoint.MrDiscoveryListener;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentDetailFragmentModel extends BaseObservable {
    public final int collapsedColor;
    public final int expandedColor;
    public final String title;
    public final ContentPropertyAdapter propertyAdapter;
    public final boolean hasResource;
    public final boolean hasProtectedResource;
    private boolean mCanSend;

    private final MrControlPoint mMrControlPoint;
    private final MrDiscoveryListener mMrDiscoveryListener = new MrDiscoveryListener() {
        @Override
        public void onDiscover(@NonNull final MediaRenderer server) {
            updateCanSend();
        }

        @Override
        public void onLost(@NonNull final MediaRenderer server) {
            updateCanSend();
        }
    };

    public ContentDetailFragmentModel(@NonNull final Context context,
                                      @NonNull final CdsObject object) {
        title = object.getTitle();
        propertyAdapter = new ContentPropertyAdapter(context, object);
        collapsedColor = ThemeUtils.getAccentColor(title);
        expandedColor = ThemeUtils.getPastelColor(title);
        hasResource = object.getTagList(CdsObject.RES) != null;
        hasProtectedResource = object.hasProtectedResource();

        mMrControlPoint = Repository.getInstance().getControlPointModel().getMrControlPoint();
        updateCanSend();
        mMrControlPoint.addMrDiscoveryListener(mMrDiscoveryListener);
    }

    @Bindable
    public boolean getCanSend() {
        return mCanSend;
    }

    private void updateCanSend() {
        mCanSend = mMrControlPoint.getDeviceListSize() > 0 && hasResource;
        notifyPropertyChanged(BR.canSend);
    }

    public void terminate() {
        mMrControlPoint.removeMrDiscoveryListener(mMrDiscoveryListener);
    }
}
