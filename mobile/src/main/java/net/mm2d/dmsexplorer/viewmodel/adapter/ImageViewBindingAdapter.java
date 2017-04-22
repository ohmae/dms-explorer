/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import net.mm2d.android.util.BitmapUtils;
import net.mm2d.android.util.ViewUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ImageViewBindingAdapter {
    @BindingAdapter("imageBinary")
    public static void setImageBinary(@NonNull final ImageView imageView,
                                      @Nullable final byte[] binary) {
        if (binary == null) {
            imageView.setImageDrawable(null);
            return;
        }
        ViewUtils.execAfterAllocateSize(imageView, () -> decodeAndSetImage(imageView, binary));
    }

    private static void decodeAndSetImage(@NonNull final ImageView imageView,
                                          @NonNull final byte[] binary) {
        final int width = imageView.getWidth();
        final int height = imageView.getHeight();
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(final Void... params) {
                return BitmapUtils.decodeBitmap(binary, width, height);
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute();
    }
}
