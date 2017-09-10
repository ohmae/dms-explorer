/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.upnp.Http;
import net.mm2d.upnp.HttpClient;
import net.mm2d.upnp.HttpResponse;

import java.io.IOException;
import java.net.URL;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Downloader {
    private Downloader() {
        throw new AssertionError();
    }

    @NonNull
    public static Single<byte[]> create(@NonNull final String url) {
        return Single.create((SingleOnSubscribe<byte[]>) emitter -> {
            final byte[] binary = download(url);
            if (binary != null) {
                emitter.onSuccess(binary);
            } else {
                emitter.onError(new IOException());
            }
        }).subscribeOn(Schedulers.io());
    }

    @Nullable
    private static byte[] download(@NonNull final String url) {
        try {
            final HttpResponse response = new HttpClient(false).download(new URL(url));
            if (response.getStatus() == Http.Status.HTTP_OK) {
                return response.getBodyBinary();
            }
        } catch (final IOException ignored) {
        } catch (final OutOfMemoryError ignored) {
        }
        return null;
    }
}
