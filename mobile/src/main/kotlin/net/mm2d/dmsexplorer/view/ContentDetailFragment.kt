/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.viewmodel.ContentDetailFragmentModel

/**
 * CDSアイテムの詳細情報を表示するFragment。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentDetailFragment : Fragment(R.layout.content_detail_fragment) {
    lateinit var binding: ContentDetailFragmentBinding
    var model: ContentDetailFragmentModel? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding = ContentDetailFragmentBinding.bind(view)
        val activity = requireActivity()
        val model = try {
            ContentDetailFragmentModel(activity, Repository.get())
        } catch (ignored: IllegalStateException) {
            activity.finish()
            return
        }
        this.model = model
        binding.toolbar.setContentScrimColor(model.collapsedColor)
        binding.toolbarBackground.setBackgroundColor(model.expandedColor)
        binding.cdsDetailToolbar.title = model.title
        binding.cdsDetail.adapter = model.propertyAdapter
        binding.fabDelete.setOnClickListener { model.onClickDelete() }
        model.getIsDeleteEnabledFlow().observe(viewLifecycleOwner) {
            if (it) binding.fabDelete.show() else binding.fabDelete.hide()
        }
        binding.fabSend.setOnClickListener { model.onClickSend() }
        model.getCanSendFlow().observe(viewLifecycleOwner) {
            if (it) binding.fabSend.show() else binding.fabSend.hide()
        }
        binding.fabPlay.setOnClickListener { model.onClickPlay(it) }
        binding.fabPlay.setOnLongClickListener { model.onLongClickPlay(it) }
        binding.fabPlay.isVisible = model.hasResource
        binding.fabPlay.backgroundTintList = ColorStateList.valueOf(model.getPlayBackgroundTint())
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
