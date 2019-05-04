/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity

import android.net.Uri

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface ContentEntity {
    val cdsObject: Any
    val name: String
    val description: String
    val artUri: Uri
    val iconUri: Uri
    val type: ContentType
    val uri: Uri?
    val mimeType: String?
    val resourceCount: Int
    val isProtected: Boolean
    fun hasResource(): Boolean
    fun canDelete(): Boolean
    fun selectResource(index: Int)
}
