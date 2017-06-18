/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.dmsexplorer.domain.model.control.MediaPlayerControl;
import net.mm2d.util.Log;

import java.io.IOException;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MusicPlayerModel extends MediaPlayerModel {
    private final Context mContext;
    private final MediaPlayer mMediaPlayer;

    public MusicPlayerModel(@NonNull final Context context) {
        this(context, new MediaPlayer());
    }

    private MusicPlayerModel(@NonNull final Context context,
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
    public void setUri(@NonNull final Uri uri, @Nullable final Object metadata) {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            Log.w(e);
        }
    }
}
