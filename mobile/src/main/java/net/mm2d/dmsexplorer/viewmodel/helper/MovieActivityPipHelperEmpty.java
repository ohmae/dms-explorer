/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class MovieActivityPipHelperEmpty implements MovieActivityPipHelper {
    @Override
    public void register() {
    }

    @Override
    public void unregister() {
    }

    @Override
    public void setControlPanelModel(@Nullable final ControlPanelModel model) {
    }

    @Override
    public void enterPictureInPictureMode(@NonNull final View contentView) {
    }
}
