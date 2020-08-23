/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Avt {
    /**
     * MediaRendererのデバイスタイプ。
     */
    const val MR_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer"

    /**
     * AVTransportのサービスID。
     */
    const val AVT_SERVICE_ID = "urn:upnp-org:serviceId:AVTransport"

    /**
     * ConnectionManagerのサービスID。
     */
    const val CMS_SERVICE_ID = "urn:upnp-org:serviceId:ConnectionManager"
}
