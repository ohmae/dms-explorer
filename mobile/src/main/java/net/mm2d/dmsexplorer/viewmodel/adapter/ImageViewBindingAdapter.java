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
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.widget.ImageView;

import net.mm2d.android.util.BitmapUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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

    @NonNull
    private static Single<Bitmap> createDecoder(
            @NonNull final ImageView imageView,
            @Nullable final byte[] binary) {
        final int width = imageView.getWidth();
        final int height = imageView.getHeight();
        return Single.create(emitter -> {
            if (binary == null) {
                emitter.onError(new IllegalStateException());
                return;
            }
            try {
                final int orientation = extractOrientation(binary);
                final Bitmap bitmap = decodeBitmap(binary, width, height, orientation);
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

    private static int extractOrientation(@NonNull final byte[] binary) {
        final ExifInterface exif;
        try {
            exif = new ExifInterface(new ByteArrayInputStream(binary));
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (final IOException ignored) {
        }
        return ExifInterface.ORIENTATION_UNDEFINED;
    }

    private static void showToast(@NonNull final Context context) {
        Toaster.show(context, R.string.toast_decode_error);
    }

    private static void setImageBitmap(
            @NonNull final ImageView imageView,
            @NonNull final Bitmap bitmap) {
        ViewUtils.execAfterAllocateSize(imageView, () -> imageView.setImageBitmap(bitmap));
    }

    @Nullable
    private static Bitmap decodeBitmap(
            @NonNull final byte[] binary,
            final int width,
            final int height,
            final int orientation) {
        final Bitmap base = decodeBaseBitmap(binary, width, height, orientation);
        if (base == null) {
            return null;
        }
        final Matrix matrix = new Matrix();
        switch (orientation) {
            default:
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
                return base;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270f);
                break;
        }
        return Bitmap.createBitmap(base, 0, 0, base.getWidth(), base.getHeight(), matrix, true);
    }

    @Nullable
    private static Bitmap decodeBaseBitmap(
            @NonNull final byte[] binary,
            final int width,
            final int height,
            final int orientation) {
        switch (orientation) {
            default:
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return BitmapUtils.decodeBitmap(binary, width, height);
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSVERSE:
            case ExifInterface.ORIENTATION_ROTATE_270:
                //noinspection SuspiciousNameCombination
                return BitmapUtils.decodeBitmap(binary, height, width);
        }
    }
}
