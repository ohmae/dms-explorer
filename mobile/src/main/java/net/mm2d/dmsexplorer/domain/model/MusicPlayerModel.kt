/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager

import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.model.control.MediaPlayerControl
import net.mm2d.log.Logger

import java.io.IOException

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MusicPlayerModel private constructor(
    private val context: Context,
    private val mediaPlayer: MediaPlayer
) : MediaPlayerModel(MediaPlayerControl(mediaPlayer)) {
    constructor(context: Context) : this(context, MediaPlayer())

    override val name: String = "Music Player"
    override fun setUri(uri: Uri, entity: ContentEntity?) {
        try {
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            Logger.w(e)
        }
    }

    override fun preparePlaying(mediaPlayer: MediaPlayer) {
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
    }
}
