/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate

import android.app.ActivityOptions
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.util.Pair
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.SharedElementCallback
import net.mm2d.android.util.ActivityUtils
import net.mm2d.android.view.TransitionListenerAdapter
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding
import net.mm2d.dmsexplorer.view.ServerDetailActivity
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ServerListActivityDelegateOnePane(
    activity: BaseActivity,
    binding: ServerListActivityBinding
) : ServerListActivityDelegate(activity, binding) {
    private var hasReenterTransition: Boolean = false
    override val isTwoPane: Boolean = false

    override fun onSelect(v: View) {
        startServerDetailActivity(v)
    }

    override fun onLostSelection() {}

    override fun onExecute(v: View) {
        startCdsListActivity(activity, v)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            hasReenterTransition = it.getBoolean(KEY_HAS_REENTER_TRANSITION)
        }
        setSharedElementCallback()
    }

    private fun setSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        activity.setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements.clear()
                binding.model?.findSharedView()?.let {
                    sharedElements[Const.SHARE_ELEMENT_NAME_DEVICE_ICON] = it
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_HAS_REENTER_TRANSITION, hasReenterTransition)
    }

    override fun onStart() {
        if (hasReenterTransition) {
            hasReenterTransition = false
            execAfterTransitionOnce(Runnable { binding.model?.updateListAdapter() })
            return
        }
        binding.model?.updateListAdapter()
    }

    private fun execAfterTransitionOnce(task: Runnable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        activity.window.sharedElementExitTransition
            .addListener(object : TransitionListenerAdapter() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                override fun onTransitionEnd(transition: Transition) {
                    task.run()
                    transition.removeListener(this)
                }
            })
    }

    private fun startServerDetailActivity(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServerDetailActivityLollipop(v)
        } else {
            startServerDetailActivityJellyBean(v)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun startServerDetailActivityLollipop(v: View) {
        val intent = ServerDetailActivity.makeIntent(activity)
        val accent = v.findViewById<View>(R.id.accent)
        val option = ActivityOptions.makeSceneTransitionAnimation(
            activity, Pair(accent, Const.SHARE_ELEMENT_NAME_DEVICE_ICON)
        ).toBundle()
        activity.startActivity(intent, option)
        hasReenterTransition = true
    }

    private fun startServerDetailActivityJellyBean(v: View) {
        val intent = ServerDetailActivity.makeIntent(activity)
        activity.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v))
    }

    companion object {
        private const val KEY_HAS_REENTER_TRANSITION = "KEY_HAS_REENTER_TRANSITION"
    }
}
