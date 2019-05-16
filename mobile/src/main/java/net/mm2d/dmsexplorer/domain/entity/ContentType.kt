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
enum class ContentType(
    val isPlayable: Boolean,
    val hasDuration: Boolean
) {
    MOVIE(true, true),
    MUSIC(true, true),
    PHOTO(true, false),
    CONTAINER(false, false),
    UNKNOWN(false, false),
}
