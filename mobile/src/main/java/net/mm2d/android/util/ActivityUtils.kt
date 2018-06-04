/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.view.View

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ActivityUtils {
    @JvmStatic
    fun makeScaleUpAnimationBundle(v: View): Bundle? {
        return ActivityOptionsCompat
                .makeScaleUpAnimation(v, 0, 0, v.width, v.height)
                .toBundle()
    }
}
