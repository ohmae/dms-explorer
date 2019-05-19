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
import android.widget.VideoView

import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.model.control.VideoViewControl

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MoviePlayerModel(
    private val context: Context,
    private val videoView: VideoView
) : MediaPlayerModel(VideoViewControl(videoView)) {
    override val name: String = "Movie Player"
    override fun setUri(uri: Uri, entity: ContentEntity?) {
        videoView.setVideoURI(uri)
    }

    override fun preparePlaying(mediaPlayer: MediaPlayer) {
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setFlags(AudioAttributes.FLAG_HW_AV_SYNC)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
    }
}
