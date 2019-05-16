/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import net.mm2d.dmsexplorer.view.eventrouter.EventObserver
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class BaseActivity @JvmOverloads constructor(
    private val mainMenu: Boolean = false
) :
    AppCompatActivity() {
    private val finishAfterTransitionLatch = AtomicBoolean()
    private val finishLatch = AtomicBoolean()
    private lateinit var delegate: OptionsMenuDelegate
    private lateinit var finishObserver: EventObserver
    private lateinit var orientationSettingsObserver: EventObserver

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = if (mainMenu)
            MainOptionsMenuDelegate(this)
        else
            BaseOptionsMenuDelegate(this)
        delegate.onCreate(savedInstanceState)
        finishObserver = EventRouter.createFinishObserver().also {
            it.register { finish() }
        }
        orientationSettingsObserver = EventRouter.createOrientationSettingsObserver().also {
            it.register { updateOrientationSettings() }
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
        finishObserver.unregister()
        orientationSettingsObserver.unregister()
    }

    open fun navigateUpTo() {
        val upIntent = NavUtils.getParentActivityIntent(this)
        if (upIntent == null) {
            onBackPressed()
            return
        }
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .startActivities()
        } else {
            NavUtils.navigateUpTo(this, upIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return delegate.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return delegate.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return delegate.onPrepareOptionsMenu(menu) || super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        try {
            super.onBackPressed()
        } catch (ignored: IllegalStateException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                finish()
            }
        }
    }

    protected open fun updateOrientationSettings() {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun finishAfterTransition() {
        if (finishAfterTransitionLatch.getAndSet(true)) {
            return
        }
        super.finishAfterTransition()
    }

    override fun finish() {
        if (finishLatch.getAndSet(true)) {
            return
        }
        super.finish()
    }
}
