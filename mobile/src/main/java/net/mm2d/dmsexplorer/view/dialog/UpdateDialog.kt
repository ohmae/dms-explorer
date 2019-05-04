/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import net.mm2d.android.util.LaunchUtils
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class UpdateDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity!!
        return AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_update)
            .setMessage(R.string.dialog_message_update)
            .setPositiveButton(R.string.ok) { _, _ ->
                LaunchUtils.openGooglePlay(
                    context,
                    Const.PACKAGE_NAME
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    companion object {
        private fun newInstance(): UpdateDialog {
            return UpdateDialog()
        }

        @JvmStatic
        fun show(activity: FragmentActivity) {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return
            }
            newInstance().show(activity.supportFragmentManager, "")
        }
    }
}
