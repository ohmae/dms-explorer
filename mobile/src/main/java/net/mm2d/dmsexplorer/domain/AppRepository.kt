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
    override val controlPointModel: ControlPointModel =
        ControlPointModel(context, this::updateMediaServer, this::updateMediaRenderer)
    override val themeModel: ThemeModelImpl = ThemeModelImpl()
    override val openUriModel: OpenUriCustomTabsModel
        get() {
            field.setUseCustomTabs(Settings.get().useCustomTabs())
            return field
        }
    override var mediaServerModel: MediaServerModel? = null
        private set
    override var mediaRendererModel: MediaRendererModel? = null
        private set
    override var playbackTargetModel: PlaybackTargetModel? = null
        private set

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

    private fun createMediaServerModel(server: MediaServer): MediaServerModel =
        MediaServerModel(context, server, this::updatePlaybackTarget)

    private fun createMediaRendererModel(renderer: MediaRenderer): MediaRendererModel =
        MediaRendererModel(context, renderer)

    private fun updatePlaybackTarget(entity: ContentEntity?) {
        playbackTargetModel = if (entity != null) PlaybackTargetModel(entity) else null
    }
}
