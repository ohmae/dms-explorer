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
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.util.UpdateChecker
import net.mm2d.dmsexplorer.view.SettingsActivity
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class MainOptionsMenuDelegate(
    private val activity: BaseActivity,
    menuResId: Int,
) : OptionsMenuDelegate {
    private val menuResId = if (menuResId != 0) menuResId else R.menu.main

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        EventRouter.observeUpdateAvailable(activity) {
            activity.invalidateOptionsMenu()
        }
    }

    override fun onDestroy() {
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
    ): Boolean {
        activity.menuInflater.inflate(menuResId, menu)
        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem,
    ): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.navigateUpTo()
                return true
            }

            R.id.action_settings -> {
                SettingsActivity.start(activity)
                return true
            }

            R.id.action_update -> {
                UpdateChecker.tryToUpdate(activity)
                return true
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(
        menu: Menu,
    ): Boolean {
        menu.findItem(R.id.action_update).isVisible = EventRouter.updateAvailable()
        return true
    }
}
