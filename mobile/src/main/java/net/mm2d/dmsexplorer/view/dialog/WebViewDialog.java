/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.WebViewDialogBinding;

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
    public static WebViewDialog newInstance(@NonNull final String title, @NonNull final String url) {
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
        final WebViewDialogBinding binding
                = WebViewDialogBinding.inflate(LayoutInflater.from(getActivity()));
        final WebView webView = binding.webView;
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new AppWebViewClient(getActivity()));

        final Bundle args = getArguments();
        webView.loadUrl(args.getString(KEY_URL));
        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(KEY_TITLE))
                .setView(binding.getRoot())
                .create();
    }

    private static class AppWebViewClient extends WebViewClient {
        @NonNull
        private final Context mContext;

        AppWebViewClient(@NonNull Context context) {
            mContext = context;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            Repository.get().getOpenUriModel().openUri(mContext, url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }
    }
}
