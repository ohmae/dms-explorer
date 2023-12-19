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

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class BaseOptionsMenuDelegate(
    private val activity: BaseActivity,
) : OptionsMenuDelegate {
    override fun onCreate(savedInstanceState: Bundle?) = Unit
    override fun onDestroy() = Unit
    override fun onCreateOptionsMenu(menu: Menu): Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.navigateUpTo()
                return true
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean = false
}
