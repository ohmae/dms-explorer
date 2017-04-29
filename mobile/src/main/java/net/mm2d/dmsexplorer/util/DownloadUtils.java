/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.upnp.Http;
import net.mm2d.upnp.HttpClient;
import net.mm2d.upnp.HttpResponse;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DownloadUtils {
    public interface Callback {
        void onResult(byte[] data);
    }

    private static final Callback CALLBACK = data -> {
    };

    public static void async(
            @NonNull final String url, @Nullable final Callback cb) {
        final Callback callback = cb != null ? cb : CALLBACK;
        new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(final Void... params) {
                return download(url);
            }

            @Override
            protected void onPostExecute(@Nullable final byte[] data) {
                callback.onResult(data);
            }
        }.execute();
    }

    private static byte[] download(@NonNull final String url) {
        try {
            final HttpResponse response = new HttpClient(false).download(new URL(url));
            if (response.getStatus() == Http.Status.HTTP_OK) {
                return response.getBodyBinary();
            }
        } catch (final IOException ignored) {
        }
        return null;
    }
}
