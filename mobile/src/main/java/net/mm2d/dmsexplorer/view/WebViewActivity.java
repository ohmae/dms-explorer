/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.WebViewActivityBinding;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.AttrUtils;
import net.mm2d.dmsexplorer.view.base.BaseActivity;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class WebViewActivity extends BaseActivity {
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_URL = "KEY_URL";

    public static Intent makeIntent(
            @NonNull final Context context,
            @NonNull final String title,
            @NonNull final String url) {
        final Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_URL, url);
        return intent;
    }

    public static void start(
            @NonNull final Context context,
            @NonNull final String title,
            @NonNull final String url) {
        context.startActivity(makeIntent(context, title, url));
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.get().getThemeParams().getNoActionBarThemeId());
        super.onCreate(savedInstanceState);
        final WebViewActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.web_view_activity);
        final Intent intent = getIntent();
        setSupportActionBar(binding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(intent.getStringExtra(KEY_TITLE));

        Repository.get().getThemeModel().setThemeColor(this,
                AttrUtils.resolveColor(this, R.attr.colorPrimary, Color.BLACK),
                ContextCompat.getColor(this, R.color.defaultStatusBar));

        final WebView webView = binding.webView;
        final WebSettings settings = webView.getSettings();
        settings.setSupportZoom(false);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(
                    final WebView view,
                    final int newProgress) {
                binding.progress.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.progress.setVisibility(View.GONE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(
                    final WebView view,
                    final String url,
                    final Bitmap favicon) {
                binding.progress.setProgress(0);
                binding.progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(
                    final WebView view,
                    final String url) {
                binding.progress.setVisibility(View.GONE);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(
                    final WebView view,
                    final String url) {
                Repository.get().getOpenUriModel().openUri(WebViewActivity.this, url);
                return true;
            }
        });
        webView.loadUrl(intent.getStringExtra(KEY_URL));
        webView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                settleAppBar(binding.appBar);
            }
            return false;
        });
    }

    private void settleAppBar(@NonNull final AppBarLayout appBar) {
        appBar.setExpanded(appBar.getHeight() / 2f + appBar.getY() > 0f);
    }
}
