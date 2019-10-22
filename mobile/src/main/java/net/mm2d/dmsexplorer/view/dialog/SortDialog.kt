package net.mm2d.dmsexplorer.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.settings.SortKey
import net.mm2d.dmsexplorer.settings.SortKey.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SortDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity!!
        val settings = Settings.get()
        val parent = activity.window.decorView as ViewGroup
        val view = activity.layoutInflater.inflate(R.layout.dialog_sort, parent, false)
        val sortOrder: SwitchCompat = view.findViewById(R.id.sort_order)
        sortOrder.setOnCheckedChangeListener { _, isChecked ->
            val id =
                if (isChecked) R.string.dialog_sort_order_ascending else R.string.dialog_sort_order_descending
            sortOrder.setText(id)
        }
        sortOrder.isChecked = settings.isAscendingSortOrder
        val sortKey: RadioGroup = view.findViewById(R.id.sort_key)
        sortKey.setOnCheckedChangeListener { _, checkedId ->
            sortOrder.isEnabled = checkedId != R.id.sort_key_none
        }
        sortKey.check(settings.sortKey.toId())
        parentFragment
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_sort)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                val key = sortKey.checkedRadioButtonId.toSortKey()
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

    private fun SortKey.toId(): Int = when (this) {
        NONE -> R.id.sort_key_none
        NAME -> R.id.sort_key_name
        DATE -> R.id.sort_key_date
    }

    private fun Int.toSortKey(): SortKey = when (this) {
        R.id.sort_key_none -> NONE
        R.id.sort_key_name -> NAME
        R.id.sort_key_date -> DATE
        else -> NONE
    }

    interface OnUpdateSortSettings {
        fun onUpdateSortSettings()
    }

    companion object {
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
