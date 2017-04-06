/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.support.annotation.NonNull;

import net.mm2d.android.upnp.avt.MediaRenderer;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaRendererModel {
    private final MediaRenderer mMediaRenderer;
    public MediaRendererModel(@NonNull final MediaRenderer renderer) {
        mMediaRenderer = renderer;
    }

    public MediaRenderer getMediaRenderer() {
        return mMediaRenderer;
    }

    public void initialize() {

    }

    public void terminate() {

    }
}
