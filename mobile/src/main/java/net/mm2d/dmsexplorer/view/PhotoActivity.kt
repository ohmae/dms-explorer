/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.PhotoActivityBinding
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.FullscreenHelper
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.view.view.ViewPagerAdapter
import net.mm2d.dmsexplorer.viewmodel.PhotoActivityModel
import net.mm2d.dmsexplorer.viewmodel.adapter.ImageViewBindingAdapter

/**
 * 静止画表示のActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PhotoActivity : BaseActivity() {
    private lateinit var settings: Settings
    private lateinit var fullscreenHelper: FullscreenHelper
    private lateinit var binding: PhotoActivityBinding
    private lateinit var model: PhotoActivityModel
    private lateinit var repository: Repository
    private lateinit var serverModel: MediaServerModel
    private val onPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) = Unit

        override fun onPageSelected(position: Int) = Unit

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                onScrollIdle()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.fullscreenThemeId)
        super.onCreate(savedInstanceState)
        binding = PhotoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullscreenHelper = FullscreenHelper(
            window = window,
            rootView = binding.root,
            topView = binding.toolbar,
        )
        repository = Repository.get()
        val serverModel = repository.mediaServerModel
        if (serverModel == null) {
            finish()
            return
        }
        this.serverModel = serverModel
        try {
            model = PhotoActivityModel(this, repository)
        } catch (ignored: IllegalStateException) {
            finish()
            return
        }
        model.update()
        model.adjustPanel(this)
        if (settings.shouldShowPhotoUiOnStart()) {
            fullscreenHelper.showNavigation()
        } else {
            fullscreenHelper.hideNavigationImmediately()
        }

        val inflater = LayoutInflater.from(this)
        binding.viewPager.adapter = ViewPagerAdapter().also {
            it.add(inflater.inflate(R.layout.progress_view, binding.viewPager, false))
            it.add(inflater.inflate(R.layout.transparent_view, binding.viewPager, false))
            it.add(inflater.inflate(R.layout.progress_view, binding.viewPager, false))
        }
        binding.viewPager.setCurrentItem(1, false)
        binding.viewPager.addOnPageChangeListener(onPageChangeListener)
        model.getImageBinaryFlow()
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                ImageViewBindingAdapter.setImageBinary(binding.imageView, it)
            }
            .launchIn(lifecycleScope)
        model.getIsLoadingFlow()
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                binding.progressBar.isVisible = it
            }
            .launchIn(lifecycleScope)
        binding.toolbar.setBackgroundColor(model.background)
        model.getRightNavigationSizeFlow()
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                binding.toolbar.updateLayoutParams<FrameLayout.LayoutParams> {
                    rightMargin = it
                }
            }
            .launchIn(lifecycleScope)
        binding.toolbarBack.setOnClickListener { model.onClickBack() }
        model.getTitleFlow()
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                binding.toolbarTitle.text = it
            }
            .launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenHelper.terminate()
    }

    override fun updateOrientationSettings() {
        settings.photoOrientation
            .setRequestedOrientation(this)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && event.keyCode != KeyEvent.KEYCODE_BACK) {
            fullscreenHelper.showNavigation()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val result = super.dispatchTouchEvent(ev)
        if (settings.shouldShowPhotoUiOnTouch()) {
            fullscreenHelper.showNavigation()
        }
        return result
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        model.adjustPanel(this)
    }

    private fun onScrollIdle() {
        val index = binding.viewPager.currentItem
        if (index == 1) {
            return
        }
        if (!move(index)) {
            finish()
            return
        }
        model.update()
        binding.viewPager.setCurrentItem(1, false)
        EventLogger.sendPlayContent(true)
    }

    private fun move(index: Int): Boolean {
        return if (index == 0) {
            serverModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL)
        } else {
            serverModel.selectNextEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL)
        }
    }
}
