/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface DirectoryEntity {
    String getParentName();

    boolean isInProgress();

    List<ContentEntity> getEntities();

    void setSelectedEntity(@Nullable ContentEntity entity);

    ContentEntity getSelectedEntity();
}
