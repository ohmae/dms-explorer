/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
class ServerDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity!!
        val binding: ServerDetailFragmentBinding = DataBindingUtil
            .inflate(inflater, R.layout.server_detail_fragment, container, false)
        try {
            binding.model = ServerDetailFragmentModel(activity, Repository.get())
        } catch (ignored: IllegalStateException) {
            activity.finish()
            return binding.root
        }

        return binding.root
    }

    companion object {
        /**
         * インスタンスを作成する。
         *
         * Bundleの設定と読み出しをこのクラス内で完結させる。
         *
         * @return インスタンス
         */
        fun newInstance(): ServerDetailFragment {
            return ServerDetailFragment()
        }
    }
}
