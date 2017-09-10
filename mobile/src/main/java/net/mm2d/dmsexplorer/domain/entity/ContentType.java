/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public enum ContentType {
    MOVIE(true, true),
    MUSIC(true, true),
    PHOTO(true, false),
    CONTAINER(false, false),
    UNKNOWN(false, false),;
    private final boolean mPlayable;
    private final boolean mHasDuration;

    ContentType(
            final boolean playable,
            final boolean hasDuration) {
        mPlayable = playable;
        mHasDuration = hasDuration;
    }

    public boolean isPlayable() {
        return mPlayable;
    }

    public boolean hasDuration() {
        return mHasDuration;
    }
}
