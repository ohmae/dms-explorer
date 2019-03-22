/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.net.Uri;

import net.mm2d.dmsexplorer.domain.entity.ContentEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface PlayerModel {
    interface StatusListener {
        void notifyDuration(int duration);

        void notifyProgress(int progress);

        void notifyPlayingState(boolean playing);

        void notifyChapterList(@NonNull List<Integer> chapterList);

        boolean onError(
                int what,
                int extra);

        boolean onInfo(
                int what,
                int extra);

        void onCompletion();
    }

    String getName();

    boolean canPause();

    void terminate();

    void setStatusListener(@NonNull StatusListener listener);

    void setUri(
            @NonNull final Uri uri,
            @Nullable final ContentEntity entity);

    void restoreSaveProgress(int progress);

    int getProgress();

    int getDuration();

    boolean isPlaying();

    void play();

    void pause();

    void seekTo(int position);

    boolean next();

    boolean previous();
}
