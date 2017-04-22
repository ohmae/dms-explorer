/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.VideoView;

import net.mm2d.dmsexplorer.domain.model.control.VideoViewControl;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MoviePlayerModel extends MediaPlayerModel {
    @NonNull
    private final VideoView mVideoView;

    public MoviePlayerModel(@NonNull final VideoView videoView) {
        super(new VideoViewControl(videoView));
        mVideoView = videoView;
    }

    @Override
    public String getName() {
        return "Movie Player";
    }

    @Override
    public void setUri(@NonNull final Uri uri, @Nullable final Object metadata) {
        mVideoView.setVideoURI(uri);
    }
}
