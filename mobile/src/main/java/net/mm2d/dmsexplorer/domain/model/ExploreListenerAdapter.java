/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import net.mm2d.dmsexplorer.domain.entity.ContentEntity;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ExploreListenerAdapter implements ExploreListener {
    @Override
    public void onStart() {
    }

    @Override
    public void onUpdate(@NonNull final List<ContentEntity> list) {
    }

    @Override
    public void onComplete() {
    }
}
