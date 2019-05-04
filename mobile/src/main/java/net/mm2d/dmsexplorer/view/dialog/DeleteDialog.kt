/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import net.mm2d.android.util.AribUtils
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DeleteDialog : DialogFragment() {
    private var onDeleteListener: OnDeleteListener? = null

    interface OnDeleteListener {
        fun onDelete()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnDeleteListener) {
            onDeleteListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity !!
        val builder = AlertDialog.Builder(context)
        val model = Repository.get().mediaServerModel
        if (model == null) {
            dismiss()
            return builder.create()
        }
        val entity = model.selectedEntity
        if (entity == null) {
            dismiss()
            return builder.create()
        }
        val applicationContext = context.applicationContext
        return builder
            .setIcon(R.drawable.ic_warning)
            .setTitle(R.string.dialog_title_delete)
            .setMessage(
                getString(
                    R.string.dialog_message_delete,
                    AribUtils.toDisplayableString(entity.name)
                )
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ ->
                model.delete(entity, {
                    Toaster.show(applicationContext, R.string.toast_delete_succeed)
                    onDeleteListener?.onDelete()
                }, {
                    Toaster.show(applicationContext, R.string.toast_delete_error)
                })
            }.create()
    }

    companion object {
        private fun newInstance(): DeleteDialog {
            return DeleteDialog()
        }

        fun show(activity: FragmentActivity) {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return
            }
            newInstance().show(activity.supportFragmentManager, "")
        }
    }
}
