/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import net.mm2d.android.util.BitmapUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.upnp.Http;
import net.mm2d.upnp.HttpClient;
import net.mm2d.upnp.HttpResponse;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ImageViewUtils {
    public interface Callback {
        void onSuccess();

        void onError();
    }

    private static final Callback CALLBACK = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError() {
        }
    };

    public static void downloadAndSetImage(
            final @NonNull ImageView imageView,
            final @NonNull String url, final @Nullable Callback cb) {
        final Callback callback = cb != null ? cb : CALLBACK;
        new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(final Void... params) {
                try {
                    final HttpResponse response = new HttpClient().download(new URL(url));
                    if (response.getStatus() == Http.Status.HTTP_OK) {
                        return response.getBodyBinary();
                    }
                } catch (final IOException ignored) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(final @Nullable byte[] data) {
                if (data == null) {
                    callback.onError();
                    return;
                }
                decodeAndSetImage(imageView, data, callback);
            }
        }.execute();
    }

    private static void decodeAndSetImage(
            final @NonNull ImageView imageView,
            final @NonNull byte[] data, final @NonNull Callback callback) {
        ViewUtils.execAfterAllocateSize(imageView, () -> {
            callback.onSuccess();
            decodeAndSetImage(imageView, data);
        });
    }

    private static void decodeAndSetImage(final ImageView imageView, byte[] data) {
        final int width = imageView.getWidth();
        final int height = imageView.getHeight();
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(final Void... params) {
                return BitmapUtils.decodeBitmap(data, width, height);
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute();
    }
}
