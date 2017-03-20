/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * コンテンツ内容をWebViewで表示するダイアログ。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class WebViewDialog extends DialogFragment {
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_URL = "KEY_URL";

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @param title ダイアログのタイトル
     * @param url   ダイアログに表示するコンテンツを指すURL
     * @return インスタンス
     */
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
        final Bundle args = getArguments();
        final WebView webView = new WebView(getActivity());
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadUrl(args.getString(KEY_URL));
        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(KEY_TITLE))
                .setView(webView)
                .create();
    }
}
