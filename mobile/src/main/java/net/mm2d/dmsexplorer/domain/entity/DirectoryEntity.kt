/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface DirectoryEntity {
    val parentName: String
    val isInProgress: Boolean
    val entities: List<ContentEntity>
    var selectedEntity: ContentEntity?
}
