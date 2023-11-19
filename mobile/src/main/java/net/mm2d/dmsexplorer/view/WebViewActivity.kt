/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.AppBarLayout
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.WebViewActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class WebViewActivity : BaseActivity() {
    private lateinit var binding: WebViewActivityBinding

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Settings.get().themeParams.noActionBarThemeId)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.web_view_activity)
        val intent = intent
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(KEY_TITLE)

        Repository.get().themeModel.setThemeColor(
            this,
            AttrUtils.resolveColor(this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK),
            ContextCompat.getColor(this, R.color.defaultStatusBar)
        )

        val webView = binding.webView
        webView.settings.also {
            it.setSupportZoom(false)
            it.displayZoomControls = false
            it.javaScriptEnabled = true
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progress.progress = newProgress
                if (newProgress == 100) {
                    binding.progress.visibility = View.GONE
                }
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                binding.progress.progress = 0
                binding.progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                binding.progress.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                Repository.get()
                    .openUriModel
                    .openUri(this@WebViewActivity, url ?: return true)
                return true
            }
        }
        webView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                settleAppBar(binding.appBar)
            }
            false
        }
        if (savedInstanceState == null) {
            webView.loadUrl(intent.getStringExtra(KEY_URL) ?: "")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    private fun settleAppBar(appBar: AppBarLayout) {
        appBar.setExpanded(appBar.height / 2f + appBar.y > 0f)
    }

    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_URL = "KEY_URL"

        fun makeIntent(context: Context, title: String, url: String): Intent =
            Intent(context, WebViewActivity::class.java).also {
                it.putExtra(KEY_TITLE, title)
                it.putExtra(KEY_URL, url)
            }

        fun start(context: Context, title: String, url: String) {
            context.startActivity(makeIntent(context, title, url))
        }
    }
}
