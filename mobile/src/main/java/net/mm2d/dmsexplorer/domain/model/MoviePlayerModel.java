/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.VideoView;

import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.model.control.VideoViewControl;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MoviePlayerModel extends MediaPlayerModel {
    @NonNull
    private final Context mContext;
    @NonNull
    private final VideoView mVideoView;

    public MoviePlayerModel(
            @NonNull final Context context,
            @NonNull final VideoView videoView) {
        super(new VideoViewControl(videoView));
        mContext = context;
        mVideoView = videoView;
    }

    @Override
    public String getName() {
        return "Movie Player";
    }

    @Override
    public void setUri(
            @NonNull final Uri uri,
            @Nullable final ContentEntity entity) {
        mVideoView.setVideoURI(uri);
    }

    @Override
    protected void preparePlaying(@NonNull final MediaPlayer mediaPlayer) {
        mediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setFlags(AudioAttributes.FLAG_HW_AV_SYNC)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build());
        } else {
            //noinspection deprecation
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }
}
