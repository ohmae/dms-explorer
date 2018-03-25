/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

/**
 * 内部で使用する定数を定義する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
final class Cds {
    /**
     * MediaServerのデバイスタイプ。
     */
    static final String MS_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaServer";
    /**
     * ContentDirectoryのサービスID。
     */
    static final String CDS_SERVICE_ID = "urn:upnp-org:serviceId:ContentDirectory";
    /**
     * ContainerUpdateIDsのタイプ名。
     */
    static final String CONTAINER_UPDATE_IDS = "ContainerUpdateIDs";
    /**
     * SystemUpdateIDのタイプ名。
     */
    static final String SYSTEM_UPDATE_ID = "SystemUpdateID";
}
