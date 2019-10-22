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
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.SettingsActivity
import net.mm2d.dmsexplorer.view.dialog.UpdateDialog
import net.mm2d.dmsexplorer.view.eventrouter.EventObserver
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class MainOptionsMenuDelegate(
    private val activity: BaseActivity,
    menuResId: Int
) : OptionsMenuDelegate {
    private val settings: Settings = Settings.get()
    private val updateAvailabilityObserver: EventObserver =
        EventRouter.createUpdateAvailabilityObserver()
    private val menuResId = if (menuResId != 0) menuResId else R.menu.main

    override fun onCreate(savedInstanceState: Bundle?) {
        updateAvailabilityObserver.register { activity.invalidateOptionsMenu() }
    }

    override fun onDestroy() {
        updateAvailabilityObserver.unregister()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(menuResId, menu)
        menu.findItem(R.id.action_update).isVisible = settings.isUpdateAvailable
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                UpdateDialog.show(activity)
                return true
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_update).isVisible = settings.isUpdateAvailable
        return true
    }
}
