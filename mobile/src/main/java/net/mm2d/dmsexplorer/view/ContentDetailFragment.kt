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
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding
import net.mm2d.dmsexplorer.viewmodel.ContentDetailFragmentModel

/**
 * CDSアイテムの詳細情報を表示するFragment。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentDetailFragment : Fragment() {
    private var model: ContentDetailFragmentModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<ContentDetailFragmentBinding>(
            inflater,
            R.layout.content_detail_fragment,
            container,
            false
        )
        val activity = activity!!
        try {
            model = ContentDetailFragmentModel(activity, Repository.get())
            binding.model = model
        } catch (ignored: IllegalStateException) {
            activity.finish()
            return binding.root
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        model?.terminate()
    }

    override fun onResume() {
        super.onResume()
        model?.onResume()
    }

    companion object {
        /**
         * インスタンスを作成する。
         *
         * Bundleの設定と読み出しをこのクラス内で完結させる。
         *
         * @return インスタンス。
         */
        fun newInstance(): ContentDetailFragment = ContentDetailFragment()
    }
}
