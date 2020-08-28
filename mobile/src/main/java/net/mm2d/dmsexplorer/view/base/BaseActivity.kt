/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class BaseActivity @JvmOverloads constructor(
    private val mainMenu: Boolean = false,
    private val menuResId: Int = 0
) :
    AppCompatActivity() {
    private val finishAfterTransitionLatch = AtomicBoolean()
    private val finishLatch = AtomicBoolean()
    private lateinit var delegate: OptionsMenuDelegate

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        updateOrientationSettings()
        super.onCreate(savedInstanceState)
        delegate = if (mainMenu)
            MainOptionsMenuDelegate(this, menuResId)
        else
            BaseOptionsMenuDelegate(this)
        delegate.onCreate(savedInstanceState)
        EventRouter.observeFinish(this) {
            finish()
        }
        EventRouter.observeOrientationSettings(this) {
            updateOrientationSettings()
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean =
        delegate.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        delegate.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onPrepareOptionsMenu(menu: Menu): Boolean =
        delegate.onPrepareOptionsMenu(menu) || super.onPrepareOptionsMenu(menu)

    override fun onBackPressed() {
        try {
            super.onBackPressed()
        } catch (ignored: IllegalStateException) {
            finishAfterTransition()
        }
    }

    protected open fun updateOrientationSettings() {}

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
