/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentType
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.DmcActivity
import net.mm2d.dmsexplorer.view.MovieActivity
import net.mm2d.dmsexplorer.view.MusicActivity
import net.mm2d.dmsexplorer.view.PhotoActivity
import net.mm2d.dmsexplorer.view.dialog.SelectRendererDialog
import net.mm2d.dmsexplorer.view.dialog.SelectResourceDialog

/**
 * Item選択後の処理をまとめるクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ItemSelectUtils {
    fun play(activity: FragmentActivity) {
        val targetModel = Repository.get().playbackTargetModel ?: return
        when (targetModel.resCount) {
            0 -> Unit
            1 -> play(activity, 0)
            else -> SelectResourceDialog.show(activity)
        }
    }

    fun play(activity: Activity, index: Int) {
        val targetModel = Repository.get().playbackTargetModel ?: return
        targetModel.setResIndex(index)
        if (targetModel.uri === Uri.EMPTY) {
            return
        }
        val type = targetModel.contentEntity.type
        val player = getPlayerClass(type) ?: return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(targetModel.uri, targetModel.mimeType)
        val isPlayMyself = Settings.get().isPlayMyself(type)
        if (isPlayMyself) {
            intent.setClass(activity, player)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
            EventLogger.sendPlayContent(isPlayMyself)
        } catch (ignored: Exception) {
            Toaster.show(activity, R.string.toast_launch_error)
        }
    }

    private fun getPlayerClass(type: ContentType): Class<*>? {
        return when (type) {
            ContentType.MOVIE -> MovieActivity::class.java
            ContentType.MUSIC -> MusicActivity::class.java
            ContentType.PHOTO -> PhotoActivity::class.java
            else -> null
        }
    }

    fun send(activity: FragmentActivity) {
        SelectRendererDialog.show(activity)
    }

    fun sendSelectedRenderer(context: Context) {
        try {
            context.startActivity(DmcActivity.makeIntent(context))
            EventLogger.sendSendContent()
        } catch (ignored: Exception) {
            Toaster.show(context, R.string.toast_launch_error)
        }
    }
}
