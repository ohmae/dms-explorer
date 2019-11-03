/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import kotlinx.android.synthetic.main.dialog_sort.view.*
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.settings.SortKey

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SortDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity!!
        val settings = Settings.get()
        val parent = activity.window.decorView as ViewGroup
        val view = activity.layoutInflater.inflate(R.layout.dialog_sort, parent, false)
        val sortOrder: SwitchCompat = view.sort_order
        sortOrder.setOnCheckedChangeListener { _, isChecked ->
            val id =
                if (isChecked) R.string.dialog_sort_order_ascending else R.string.dialog_sort_order_descending
            sortOrder.setText(id)
        }
        sortOrder.isChecked = settings.isAscendingSortOrder

        val sortKey: Spinner = view.sort_key
        sortKey.adapter = ArrayAdapter<String>(
            activity,
            android.R.layout.simple_list_item_1,
            list.map { getString(it.stringRes) })
        sortKey.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                sortOrder.isEnabled = position != list.indexOfFirst { it.sortKey == SortKey.NONE }
            }
        }
        sortKey.setSelection(list.indexOfFirst { it.sortKey == settings.sortKey })
        parentFragment
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_sort)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                val key = list[sortKey.selectedItemPosition].sortKey
                val isAscending = sortOrder.isChecked
                if (key == settings.sortKey && isAscending == settings.isAscendingSortOrder) {
                    return@setPositiveButton
                }
                settings.sortKey = key
                settings.isAscendingSortOrder = isAscending
                notifyUpdateSortSettings()
            }
            .create()
    }

    private fun notifyUpdateSortSettings() {
        Repository.get().mediaServerModel?.onUpdateSortSettings()
        val activity = activity
        val parentFragment = parentFragment
        if (activity is OnUpdateSortSettings) {
            activity.onUpdateSortSettings()
        } else if (parentFragment is OnUpdateSortSettings) {
            parentFragment.onUpdateSortSettings()
        }
    }

    interface OnUpdateSortSettings {
        fun onUpdateSortSettings()
    }

    private class SpinnerItem(
        val sortKey: SortKey,
        @StringRes
        val stringRes: Int
    )

    companion object {
        private val list = listOf(
            SpinnerItem(SortKey.NONE, R.string.dialog_sort_key_none),
            SpinnerItem(SortKey.NAME, R.string.dialog_sort_key_name),
            SpinnerItem(SortKey.DATE, R.string.dialog_sort_key_date)
        )

        private fun newInstance(): SortDialog = SortDialog()

        fun show(activity: FragmentActivity) {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return
            }
            newInstance().show(activity.supportFragmentManager, "")
        }

        fun show(fragment: Fragment) {
            if (fragment.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return
            }
            newInstance().show(fragment.childFragmentManager, "")
        }
    }
}
