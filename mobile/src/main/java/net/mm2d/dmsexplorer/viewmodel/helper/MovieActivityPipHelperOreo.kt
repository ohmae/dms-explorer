/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper

import android.app.Activity
import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel
import net.mm2d.log.Logger

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@RequiresApi(api = Build.VERSION_CODES.O)
internal class MovieActivityPipHelperOreo(
    private val activity: Activity,
) : MovieActivityPipHelper {
    private var controlPanelModel: ControlPanelModel? = null
    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            val action = intent.action
            val model = controlPanelModel
            if (action.isNullOrEmpty() || model == null) {
                return
            }
            when (action) {
                Const.ACTION_PLAY -> {
                    model.onClickPlayPause()
                    activity.setPictureInPictureParams(
                        PictureInPictureParams.Builder()
                            .setActions(makeActions(model.isPlaying))
                            .build(),
                    )
                }
                Const.ACTION_NEXT -> model.onClickNext()
                Const.ACTION_PREV -> model.onClickPrevious()
            }
        }
    }

    private fun isPictureInPictureAllowed(): Boolean {
        val appOps = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    activity.packageName,
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    activity.packageName,
                )
            } == AppOpsManager.MODE_ALLOWED
        } catch (ignored: Exception) {
            false
        }
    }

    override fun register() {
        ContextCompat.registerReceiver(
            activity,
            controlReceiver,
            makeIntentFilter(),
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    private fun makeIntentFilter(): IntentFilter = IntentFilter().also {
        it.addAction(Const.ACTION_PLAY)
        it.addAction(Const.ACTION_NEXT)
        it.addAction(Const.ACTION_PREV)
    }

    override fun unregister() {
        activity.unregisterReceiver(controlReceiver)
    }

    override fun setControlPanelModel(model: ControlPanelModel?) {
        controlPanelModel = model
    }

    override fun enterPictureInPictureMode(contentView: View) {
        if (!isPictureInPictureAllowed()) {
            val intent = Intent(ACTION_PICTURE_IN_PICTURE_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
            return
        }
        val builder = PictureInPictureParams.Builder()
        builder.setActions(controlPanelModel?.isPlaying?.let { makeActions(it) })
        val rect = makeViewRect(contentView)
        if (rect.width() > 0 && rect.height() > 0) {
            builder.setSourceRectHint(rect)
                .setAspectRatio(Rational(rect.width(), rect.height()))
        }
        try {
            activity.enterPictureInPictureMode(builder.build())
        } catch (e: Exception) {
            Logger.w(e)
        }
    }

    private fun makeViewRect(v: View): Rect = Rect().also {
        v.getGlobalVisibleRect(it)
    }

    private fun makeActions(isPlaying: Boolean): List<RemoteAction>? {
        val max = activity.maxNumPictureInPictureActions
        if (max <= 0) {
            return null
        }
        return if (max >= 3) {
            listOf(makePreviousAction(), makePlayAction(isPlaying), makeNextAction())
        } else {
            listOf(makePlayAction(isPlaying))
        }
    }

    private fun makePlayAction(isPlaying: Boolean): RemoteAction = RemoteAction(
        makeIcon(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
        getString(R.string.action_play_title),
        getString(R.string.action_play_description),
        makePlayPendingIntent(activity),
    )

    private fun makeNextAction(): RemoteAction = RemoteAction(
        makeIcon(R.drawable.ic_skip_next),
        getString(R.string.action_next_title),
        getString(R.string.action_next_description),
        makeNextPendingIntent(activity),
    )

    private fun makePreviousAction(): RemoteAction = RemoteAction(
        makeIcon(R.drawable.ic_skip_previous),
        getString(R.string.action_previous_title),
        getString(R.string.action_previous_description),
        makePreviousPendingIntent(activity),
    )

    private fun getString(@StringRes resId: Int): String =
        activity.resources.getText(resId, "").toString()

    private fun makeIcon(@DrawableRes resId: Int): Icon = Icon.createWithResource(activity, resId)

    companion object {
        private const val ACTION_PICTURE_IN_PICTURE_SETTINGS =
            "android.settings.PICTURE_IN_PICTURE_SETTINGS"

        private fun makePlayPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                Const.REQUEST_CODE_ACTION_PLAY,
                Intent(Const.ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun makeNextPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                Const.REQUEST_CODE_ACTION_NEXT,
                Intent(Const.ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun makePreviousPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                Const.REQUEST_CODE_ACTION_PREVIOUS,
                Intent(Const.ACTION_PREV),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
    }
}
