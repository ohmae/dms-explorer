/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding
import net.mm2d.dmsexplorer.viewmodel.ServerDetailFragmentModel

/**
 * メディアサーバの詳細情報を表示するFragment。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerDetailFragment : Fragment(R.layout.server_detail_fragment) {
    lateinit var binding: ServerDetailFragmentBinding
    lateinit var model: ServerDetailFragmentModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding = ServerDetailFragmentBinding.bind(view)
        val activity = requireActivity()
        model = try {
            ServerDetailFragmentModel(activity, Repository.get())
        } catch (ignored: IllegalStateException) {
            activity.finish()
            return
        }
        binding.toolbar.setContentScrimColor(model.collapsedColor)
        binding.toolbarBackground.setBackgroundColor(model.expandedColor)
        binding.toolbarIcon.setImageDrawable(model.icon)
        binding.serverDetailToolbar.title = model.title
        binding.serverDetail.adapter = model.propertyAdapter
        binding.fab.setOnClickListener { model.onClickFab(it) }
    }

    companion object {
        /**
         * インスタンスを作成する。
         *
         * Bundleの設定と読み出しをこのクラス内で完結させる。
         *
         * @return インスタンス
         */
        fun newInstance(): ServerDetailFragment = ServerDetailFragment()
    }
}
