/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter;

import android.content.Context;

import net.mm2d.android.upnp.cds.MediaServer;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ServerPropertyAdapter extends PropertyAdapter {
    ServerPropertyAdapter(
            @NonNull final Context context,
            @NonNull final MediaServer server) {
        super(context);
        setServerInfo(this, server);
    }

    private static void setServerInfo(
            @NonNull final PropertyAdapter adapter,
            @NonNull final MediaServer server) {
        adapter.addEntry("FriendlyName:", server.getFriendlyName());
        adapter.addEntry("SerialNumber:", server.getSerialNumber());
        adapter.addEntry("IP Address:", server.getIpAddress());
        adapter.addEntry("UDN:", server.getUdn());
        adapter.addEntry("Manufacture:", server.getManufacture());
        adapter.addEntry("ManufactureUrl:", server.getManufactureUrl(), Type.LINK);
        adapter.addEntry("ModelName:", server.getModelName());
        adapter.addEntry("ModelUrl:", server.getModelUrl(), Type.LINK);
        adapter.addEntry("ModelDescription:", server.getModelDescription());
        adapter.addEntry("ModelNumber:", server.getModelNumber());
        adapter.addEntry("PresentationUrl:", server.getPresentationUrl(), Type.LINK);
        adapter.addEntry("Location:", server.getLocation());
    }
}
