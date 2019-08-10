/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ActivityUtils {
    fun makeScaleUpAnimationBundle(v: View): Bundle? = ActivityOptionsCompat
        .makeScaleUpAnimation(v, 0, 0, v.width, v.height)
        .toBundle()
}
