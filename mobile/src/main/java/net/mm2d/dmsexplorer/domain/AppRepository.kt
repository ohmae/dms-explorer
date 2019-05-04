/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain

import android.app.Application
import android.content.Context
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter
import net.mm2d.dmsexplorer.domain.model.*
import net.mm2d.dmsexplorer.domain.tabs.CustomTabsBinder
import net.mm2d.dmsexplorer.domain.tabs.CustomTabsHelper
import net.mm2d.dmsexplorer.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class AppRepository(application: Application) : Repository() {
    private val context: Context = application
    private val controlPointModel: ControlPointModel =
        ControlPointModel(context, this::updateMediaServer, this::updateMediaRenderer)
    private val themeModel: ThemeModelImpl = ThemeModelImpl()
    private val openUriModel: OpenUriCustomTabsModel
    private var mediaServerModel: MediaServerModel? = null
    private var mediaRendererModel: MediaRendererModel? = null
    private var playbackTargetModel: PlaybackTargetModel? = null

    init {
        Completable.fromAction { CdsFormatter.initialize(application) }
            .subscribeOn(Schedulers.io())
            .subscribe()
        val helper = CustomTabsHelper(context)
        openUriModel = OpenUriCustomTabsModel(helper, themeModel)
        application.registerActivityLifecycleCallbacks(CustomTabsBinder(helper))
        application.registerActivityLifecycleCallbacks(themeModel)
    }

    private fun updateMediaServer(server: MediaServer?) {
        mediaServerModel?.terminate()
        mediaServerModel = null
        if (server != null) {
            mediaServerModel = createMediaServerModel(server).also {
                it.initialize()
            }
        }
    }

    private fun updateMediaRenderer(renderer: MediaRenderer?) {
        mediaRendererModel?.terminate()
        mediaRendererModel = null
        if (renderer != null) {
            mediaRendererModel = createMediaRendererModel(renderer)
        }
    }

    private fun createMediaServerModel(server: MediaServer): MediaServerModel {
        return MediaServerModel(context, server, this::updatePlaybackTarget)
    }

    private fun createMediaRendererModel(renderer: MediaRenderer): MediaRendererModel {
        return MediaRendererModel(context, renderer)
    }

    private fun updatePlaybackTarget(entity: ContentEntity?) {
        playbackTargetModel = if (entity != null) PlaybackTargetModel(entity) else null
    }

    override fun getThemeModel(): ThemeModel {
        return themeModel
    }

    override fun getOpenUriModel(): OpenUriModel {
        openUriModel.setUseCustomTabs(Settings.get().useCustomTabs())
        return openUriModel
    }

    override fun getControlPointModel(): ControlPointModel {
        return controlPointModel
    }

    override fun getMediaServerModel(): MediaServerModel? {
        return mediaServerModel
    }

    override fun getMediaRendererModel(): MediaRendererModel? {
        return mediaRendererModel
    }

    override fun getPlaybackTargetModel(): PlaybackTargetModel? {
        return playbackTargetModel
    }
}
