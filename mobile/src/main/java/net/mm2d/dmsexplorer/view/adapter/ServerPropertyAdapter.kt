/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter

import android.content.Context
import net.mm2d.android.upnp.cds.MediaServer

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ServerPropertyAdapter(
    context: Context,
    server: MediaServer,
) : PropertyAdapter(context) {
    init {
        setServerInfo(this, server)
    }

    private fun setServerInfo(
        adapter: PropertyAdapter,
        server: MediaServer,
    ) {
        adapter.addEntry("FriendlyName:", server.friendlyName)
        adapter.addEntry("SerialNumber:", server.serialNumber)
        adapter.addEntry("IP Address:", server.ipAddress)
        adapter.addEntry("UDN:", server.udn)
        adapter.addEntry("Manufacture:", server.manufacture)
        adapter.addEntry("ManufactureUrl:", server.manufactureUrl, Type.LINK)
        adapter.addEntry("ModelName:", server.modelName)
        adapter.addEntry("ModelUrl:", server.modelUrl, Type.LINK)
        adapter.addEntry("ModelDescription:", server.modelDescription)
        adapter.addEntry("ModelNumber:", server.modelNumber)
        adapter.addEntry("PresentationUrl:", server.presentationUrl, Type.LINK)
        adapter.addEntry("Location:", server.location)
    }
}
