/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

/**
 * 内部で使用する定数を定義する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Cds {
    /**
     * MediaServerのデバイスタイプ。
     */
    const val MS_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaServer"
    /**
     * ContentDirectoryのサービスID。
     */
    const val CDS_SERVICE_ID = "urn:upnp-org:serviceId:ContentDirectory"
    /**
     * ContainerUpdateIDsのタイプ名。
     */
    const val CONTAINER_UPDATE_IDS = "ContainerUpdateIDs"
    /**
     * SystemUpdateIDのタイプ名。
     */
    const val SYSTEM_UPDATE_ID = "SystemUpdateID"
}
