/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.transition;

import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.transition.PathMotion;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * マテリアルデザインにある下に凸な曲線のモーションを実現する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SmilingCurveMotion extends PathMotion {
    public SmilingCurveMotion() {
    }

    public SmilingCurveMotion(
            @NonNull final Context context,
            @NonNull final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Path getPath(
            final float startX,
            final float startY,
            final float endX,
            final float endY) {
        final Path path = new Path();
        path.moveTo(startX, startY);
        final float middleX;
        final float middleY;
        if (startY > endY) {
            middleX = (startX + endX * 3f) / 4f;
            middleY = (startY * 3f + endY) / 4f;
        } else {
            middleX = (startX * 3f + endX) / 4f;
            middleY = (startY + endY * 3f) / 4f;
        }
        path.quadTo(middleX, middleY, endX, endY);
        return path;
    }
}
