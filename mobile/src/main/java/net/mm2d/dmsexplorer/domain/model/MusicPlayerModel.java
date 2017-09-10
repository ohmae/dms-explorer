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

import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.model.control.MediaPlayerControl;
import net.mm2d.util.Log;

import java.io.IOException;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MusicPlayerModel extends MediaPlayerModel {
    @NonNull
    private final Context mContext;
    @NonNull
    private final MediaPlayer mMediaPlayer;

    public MusicPlayerModel(@NonNull final Context context) {
        this(context, new MediaPlayer());
    }

    private MusicPlayerModel(
            @NonNull final Context context,
            @NonNull final MediaPlayer mediaPlayer) {
        super(new MediaPlayerControl(mediaPlayer));
        mContext = context;
        mMediaPlayer = mediaPlayer;
    }

    @Override
    public String getName() {
        return "Music Player";
    }

    @Override
    public void setUri(
            @NonNull final Uri uri,
            @Nullable final ContentEntity entity) {
        try {
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            Log.w(e);
        }
    }

    @Override
    protected void preparePlaying(@NonNull final MediaPlayer mediaPlayer) {
        mediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
        } else {
            //noinspection deprecation
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }
}
