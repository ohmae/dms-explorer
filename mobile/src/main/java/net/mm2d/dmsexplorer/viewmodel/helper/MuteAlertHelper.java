/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.R;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MuteAlertHelper {
    private final Context mContext;
    private final AudioManager mAudioManager;

    public MuteAlertHelper(@NonNull final Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void alertIfMuted() {
        if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toaster.show(mContext, R.string.toast_currently_muted);
        }
    }
}
