/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class WebViewDialog extends DialogFragment {
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_URL = "KEY_URL";

    public static WebViewDialog newInstance(String title, String url) {
        final Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_URL, url);
        final WebViewDialog instance = new WebViewDialog();
        instance.setArguments(args);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        builder.setTitle(args.getString(KEY_TITLE));
        final WebView webView = new WebView(getActivity());
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadUrl(args.getString(KEY_URL));
        builder.setView(webView);
        return builder.create();
    }
}
