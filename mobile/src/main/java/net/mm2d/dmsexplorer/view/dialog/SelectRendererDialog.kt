/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.RendererSelectDialogBinding
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.util.ItemSelectUtils
import net.mm2d.dmsexplorer.view.adapter.RendererListAdapter

/**
 * 再生機器選択を行うダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SelectRendererDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val model = Repository.get().controlPointModel
        val cp = model.mrControlPoint
        val rendererList = cp.deviceList
        if (rendererList.isEmpty()) {
            dismiss()
            return builder.create()
        }
        val adapter = RendererListAdapter(activity, rendererList)
        adapter.setOnItemClickListener { _, renderer ->
            model.setSelectedMediaRenderer(renderer)
            EventLogger.sendSelectRenderer()
            ItemSelectUtils.sendSelectedRenderer(activity)
            dismiss()
        }
        val inflater = activity.layoutInflater
        val binding = DataBindingUtil.inflate<RendererSelectDialogBinding>(
            inflater,
            R.layout.renderer_select_dialog, null, false
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerView.adapter = adapter
        builder.setTitle(R.string.dialog_title_select_device)
        builder.setView(binding.root)
        return builder.create()
    }

    companion object {
        private fun newInstance(): SelectRendererDialog = SelectRendererDialog()

        fun show(activity: FragmentActivity) {
            if (activity.supportFragmentManager.isStateSaved) {
                return
            }
            newInstance().show(activity.supportFragmentManager, "")
        }
    }
}
