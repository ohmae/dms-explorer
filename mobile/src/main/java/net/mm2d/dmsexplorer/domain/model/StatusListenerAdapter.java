/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.media.MediaPlayer;

import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class StatusListenerAdapter implements StatusListener {
    @Override
    public void notifyDuration(final int duration) {
    }

    @Override
    public void notifyProgress(final int progress) {
    }

    @Override
    public void notifyPlayingState(final boolean playing) {
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        return false;
    }

    @Override
    public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
        return false;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
    }
}
