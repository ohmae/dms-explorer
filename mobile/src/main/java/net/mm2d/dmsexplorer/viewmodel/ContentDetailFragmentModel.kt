/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.graphics.Color
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.android.upnp.avt.MrControlPoint
import net.mm2d.android.upnp.avt.MrControlPoint.MrDiscoveryListener
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.util.ItemSelectUtils
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentDetailFragmentModel(
    private val activity: FragmentActivity,
    repository: Repository,
) {
    val collapsedColor: Int
    val expandedColor: Int
    val title: String
    val propertyAdapter: PropertyAdapter
    val hasResource: Boolean
    private val isProtected: Boolean

    private val canDelete: Boolean

    private val canSendFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getCanSendFlow(): Flow<Boolean> = canSendFlow

    private val isDeleteEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsDeleteEnabledFlow(): Flow<Boolean> = isDeleteEnabledFlow

    private val settings: Settings
    private val mrControlPoint: MrControlPoint
    private val mrDiscoveryListener = object : MrDiscoveryListener {
        override fun onDiscover(
            server: MediaRenderer,
        ) {
            updateCanSend()
        }

        override fun onLost(
            server: MediaRenderer,
        ) {
            updateCanSend()
        }
    }

    fun getPlayBackgroundTint(): Int =
        if (isProtected) {
            AttrUtils.resolveColor(activity, R.attr.themeFabDisable, Color.BLACK)
        } else {
            AttrUtils.resolveColor(activity, androidx.appcompat.R.attr.colorAccent, Color.BLACK)
        }

    init {
        val model = repository.mediaServerModel ?: throw IllegalStateException()
        val entity = model.selectedEntity ?: throw IllegalStateException()
        settings = Settings.get()
        val rawTitle = entity.name
        title = rawTitle.toDisplayableString()
        propertyAdapter = PropertyAdapter.ofContent(activity, entity)
        val generator = settings.themeParams
            .themeColorGenerator
        if (activity.resources.getBoolean(R.bool.two_pane)) {
            collapsedColor = generator.getSubToolbarColor(rawTitle)
            expandedColor = generator.getSubToolbarColor(rawTitle)
        } else {
            collapsedColor = generator.getCollapsedToolbarColor(rawTitle)
            expandedColor = generator.getExpandedToolbarColor(rawTitle)
        }
        hasResource = entity.hasResource()
        isProtected = entity.isProtected
        canDelete = model.canDelete(entity)
        isDeleteEnabledFlow.tryEmit(settings.isDeleteFunctionEnabled && canDelete)

        mrControlPoint = repository.controlPointModel.mrControlPoint
        updateCanSend()
        mrControlPoint.addMrDiscoveryListener(mrDiscoveryListener)
    }

    fun onResume() {
        isDeleteEnabledFlow.tryEmit(settings.isDeleteFunctionEnabled && canDelete)
    }

    private fun updateCanSend() {
        canSendFlow.tryEmit(mrControlPoint.deviceListSize > 0 && hasResource)
    }

    fun terminate() {
        mrControlPoint.removeMrDiscoveryListener(mrDiscoveryListener)
    }

    fun onClickPlay(
        view: View,
    ) {
        if (isProtected) {
            showSnackbar(view)
        } else {
            ItemSelectUtils.play(activity, 0)
        }
    }

    fun onLongClickPlay(
        view: View,
    ): Boolean {
        if (isProtected) {
            showSnackbar(view)
        } else {
            ItemSelectUtils.play(activity)
        }
        return true
    }

    private fun showSnackbar(
        view: View,
    ) {
        Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show()
    }

    fun onClickSend() {
        ItemSelectUtils.send(activity)
    }

    fun onClickDelete() {
        DeleteDialog.show(activity)
    }
}
