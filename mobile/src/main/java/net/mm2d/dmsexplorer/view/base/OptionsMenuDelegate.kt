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
internal interface OptionsMenuDelegate {
    fun onCreate(savedInstanceState: Bundle?)
    fun onDestroy()
    fun onCreateOptionsMenu(menu: Menu): Boolean
    fun onOptionsItemSelected(item: MenuItem): Boolean
    fun onPrepareOptionsMenu(menu: Menu): Boolean
}
