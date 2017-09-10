/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.entity.ContentType;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class EventLogger {
    private static FirebaseAnalytics sAnalytics;

    public static void initialize(@NonNull final Context context) {
        sAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void sendSelectServer() {
        final MediaServerModel serverModel = Repository.get().getMediaServerModel();
        if (serverModel == null) {
            return;
        }
        final MediaServer server = serverModel.getMediaServer();
        final Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_NAME, server.getFriendlyName());
        bundle.putString(Param.ITEM_BRAND, server.getManufacture());
        sAnalytics.logEvent("select_server", bundle);
    }

    public static void sendSelectRenderer() {
        final MediaRendererModel rendererModel = Repository.get().getMediaRendererModel();
        if (rendererModel == null) {
            return;
        }
        final MediaRenderer renderer = rendererModel.getMediaRenderer();
        final Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_NAME, renderer.getFriendlyName());
        bundle.putString(Param.ITEM_BRAND, renderer.getManufacture());
        sAnalytics.logEvent("select_renderer", bundle);
    }

    public static void sendSendContent() {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        if (targetModel == null) {
            return;
        }
        final ContentEntity entity = targetModel.getContentEntity();
        final CdsObject object = (CdsObject) entity.getObject();
        final Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_VARIANT, object.getValue(CdsObject.RES_PROTOCOL_INFO));
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.getType()));
        bundle.putString(Param.ORIGIN, object.hasProtectedResource() ? "dlna-dtcp" : "dlna");
        bundle.putString(Param.DESTINATION, "dmr");
        sAnalytics.logEvent(Event.SELECT_CONTENT, bundle);
    }

    public static void sendPlayContent() {
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        if (targetModel == null) {
            return;
        }
        final ContentEntity entity = targetModel.getContentEntity();
        final CdsObject object = (CdsObject) entity.getObject();
        final Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_VARIANT, object.getValue(CdsObject.RES_PROTOCOL_INFO));
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.getType()));
        bundle.putString(Param.ORIGIN, "dlna");
        final Settings settings = new Settings();
        bundle.putString(Param.DESTINATION, settings.isPlayMyself(entity.getType()) ? "myself" : "other");
        sAnalytics.logEvent(Event.SELECT_CONTENT, bundle);
    }

    @NonNull
    private static String getTypeString(@NonNull final ContentType type) {
        return type.name().toLowerCase();
    }
}
