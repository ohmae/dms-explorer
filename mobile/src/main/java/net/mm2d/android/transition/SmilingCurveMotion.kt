/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.transition

import android.content.Context
import android.graphics.Path
import android.transition.PathMotion
import android.util.AttributeSet

/**
 * マテリアルデザインにある下に凸な曲線のモーションを実現する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SmilingCurveMotion(context: Context, attrs: AttributeSet) : PathMotion(context, attrs) {
    override fun getPath(startX: Float, startY: Float, endX: Float, endY: Float) = Path().also {
        it.moveTo(startX, startY)
        if (startY > endY) {
            it.quadTo(
                centerInRatio(startX, endX, 1f, 3f),
                centerInRatio(startY, endY, 3f, 1f),
                endX,
                endY,
            )
        } else {
            it.quadTo(
                centerInRatio(startX, endX, 3f, 1f),
                centerInRatio(startY, endY, 1f, 3f),
                endX,
                endY,
            )
        }
    }

    private fun centerInRatio(v1: Float, v2: Float, r1: Float, r2: Float): Float =
        (v1 * r1 + v2 * r2) / (r1 + r2)
}
