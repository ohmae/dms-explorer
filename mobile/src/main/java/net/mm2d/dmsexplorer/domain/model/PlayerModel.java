/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface PlayerModel {
    interface StatusListener {
        void notifyDuration(int duration);

        void notifyProgress(int progress);

        void notifyPlayingState(boolean playing);

        boolean onError(MediaPlayer mp, int what, int extra);

        boolean onInfo(MediaPlayer mp, int what, int extra);

        void onCompletion(MediaPlayer mp);
    }

    void terminate();

    void setStatusListener(@NonNull StatusListener listener);

    void setUri(@NonNull final Uri uri);

    void restoreSaveProgress(int progress);

    int getProgress();

    int getDuration();

    boolean isPlaying();

    void play();

    void pause();

    void seekTo(int position);

    void next();

    void previous();
}
