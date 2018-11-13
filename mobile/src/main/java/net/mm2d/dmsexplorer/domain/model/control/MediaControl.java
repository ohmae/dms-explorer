/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;

import androidx.annotation.Nullable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface MediaControl {
    void play();

    void pause();

    void seekTo(int position);

    void stop();

    int getCurrentPosition();

    int getDuration();

    boolean isPlaying();

    void setOnPreparedListener(@Nullable OnPreparedListener listener);

    void setOnErrorListener(@Nullable OnErrorListener listener);

    void setOnInfoListener(@Nullable OnInfoListener listener);

    void setOnCompletionListener(@Nullable OnCompletionListener listener);
}
