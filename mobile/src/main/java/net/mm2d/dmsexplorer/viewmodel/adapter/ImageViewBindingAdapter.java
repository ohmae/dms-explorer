/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import net.mm2d.android.util.BitmapUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ImageViewBindingAdapter {
    @BindingAdapter("imageBinary")
    public static void setImageBinary(
            @NonNull final ImageView imageView,
            @Nullable final byte[] binary) {
        if (binary == null) {
            imageView.setImageDrawable(null);
            return;
        }
        createDecoder(imageView, binary)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> setImageBitmap(imageView, bitmap),
                        throwable -> showToast(imageView.getContext()));
    }

    private static Single<Bitmap> createDecoder(
            @NonNull final ImageView imageView,
            @Nullable final byte[] binary) {
        final int width = imageView.getWidth();
        final int height = imageView.getHeight();
        return Single.create(emitter -> {
            try {
                final Bitmap bitmap = BitmapUtils.decodeBitmap(binary, width, height);
                if (bitmap != null) {
                    emitter.onSuccess(bitmap);
                } else {
                    emitter.onError(new IllegalStateException());
                }
            } catch (final OutOfMemoryError ignored) {
                emitter.onError(ignored);
            }
        });
    }

    private static void showToast(@NonNull final Context context) {
        Toaster.showLong(context, R.string.toast_decode_error_occurred);
    }

    private static void setImageBitmap(
            @NonNull final ImageView imageView,
            @NonNull final Bitmap bitmap) {
        ViewUtils.execAfterAllocateSize(imageView, () -> imageView.setImageBitmap(bitmap));
    }
}
