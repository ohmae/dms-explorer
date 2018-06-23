/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.text.TextUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class StringJoiner {
    private val builder = StringBuilder()

    @JvmOverloads
    fun join(string: String?, delimiter: Char = '\n') {
        if (TextUtils.isEmpty(string)) {
            return
        }
        if (builder.isNotEmpty()) {
            builder.append(delimiter)
        }
        builder.append(string)
    }

    fun join(string: String?, delimiter: String) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        if (builder.isNotEmpty()) {
            builder.append(delimiter)
        }
        builder.append(string)
    }

    fun isNotEmpty(): Boolean {
        return builder.isNotEmpty()
    }

    override fun toString(): String {
        return builder.toString()
    }
}
