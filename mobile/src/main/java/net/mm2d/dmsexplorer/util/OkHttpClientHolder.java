/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import okhttp3.OkHttpClient;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class OkHttpClientHolder {
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    public static OkHttpClient get() {
        return OK_HTTP_CLIENT;
    }
}
