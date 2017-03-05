/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Avt {
    /**
     * MediaRendererのデバイスタイプ。
     */
    static final String MR_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer";
    /**
     * AVTransportのサービスID。
     */
    static final String AVT_SERVICE_ID = "urn:upnp-org:serviceId:AVTransport";
    /**
     * ConnectionManagerのサービスID。
     */
    static final String CMS_SERVICE_ID = "urn:upnp-org:serviceId:ConnectionManager";
}
