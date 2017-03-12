/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.LaunchUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerPropertyAdapter extends PropertyAdapter {
    public ServerPropertyAdapter(@NonNull Context context, @NonNull MediaServer server) {
        super(context);
        setOnItemLinkClickListener(link -> LaunchUtils.openUri(context, link));
        setServerInfo(this, server);
    }

    private static void setServerInfo(PropertyAdapter adapter, MediaServer server) {
        adapter.addEntry("FriendlyName:", server.getFriendlyName());
        adapter.addEntry("SerialNumber:", server.getSerialNumber());
        adapter.addEntry("IP Address:", server.getIpAddress());
        adapter.addEntry("UDN:", server.getUdn());
        adapter.addEntry("Manufacture:", server.getManufacture());
        adapter.addEntry("ManufactureUrl:", server.getManufactureUrl(), true);
        adapter.addEntry("ModelName:", server.getModelName());
        adapter.addEntry("ModelUrl:", server.getModelUrl(), true);
        adapter.addEntry("ModelDescription:", server.getModelDescription());
        adapter.addEntry("ModelNumber:", server.getModelNumber());
        adapter.addEntry("PresentationUrl:", server.getPresentationUrl(), true);
        adapter.addEntry("Location:", server.getLocation());
    }
}
