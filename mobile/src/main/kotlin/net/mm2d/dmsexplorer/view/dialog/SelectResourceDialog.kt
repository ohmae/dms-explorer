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
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.util.ItemSelectUtils

/**
 * マルチリソースのコンテンツの再生時にリソースの選択を促すダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SelectResourceDialog : DialogFragment() {
    override fun onCreateDialog(
        savedInstanceState: Bundle?,
    ): Dialog {
        val activity = requireActivity()
        val choices = Repository.get().playbackTargetModel?.createResChoices()
            ?: return AlertDialog.Builder(activity).create()
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_select_resource)
            .setItems(choices) { _, which -> ItemSelectUtils.play(activity, which) }
            .create()
    }

    companion object {
        /**
         * インスタンスを作成する。
         *
         * Bundleの設定と読み出しをこのクラス内で完結させる。
         *
         * @return インスタンス。
         */
        private fun newInstance(): SelectResourceDialog = SelectResourceDialog()

        fun show(
            activity: FragmentActivity,
        ) {
            if (activity.supportFragmentManager.isStateSaved) {
                return
            }
            newInstance().show(activity.supportFragmentManager, "")
        }
    }
}
