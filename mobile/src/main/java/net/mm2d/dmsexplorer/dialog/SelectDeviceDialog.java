/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;

import java.util.List;

/**
 * 再生機器選択を行うダイアログ。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SelectDeviceDialog extends DialogFragment {
    private static final String KEY_OBJECT = "KEY_OBJECT";
    private static final String KEY_UDN = "KEY_UDN";

    public static SelectDeviceDialog newInstance(
            final @NonNull String udn, final @NonNull CdsObject object) {
        final Bundle arguments = new Bundle();
        arguments.putString(KEY_UDN, udn);
        arguments.putParcelable(KEY_OBJECT, object);
        final SelectDeviceDialog dialog = new SelectDeviceDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_select_device);
        final MrControlPoint cp = Repository.getInstance().getControlPointModel().getMrControlPoint();
        final List<MediaRenderer> rendererList = cp.getDeviceList();
        final String[] choices = new String[rendererList.size()];
        for (int i = 0; i < rendererList.size(); i++) {
            choices[i] = rendererList.get(i).getFriendlyName();
        }
        final String udn = getArguments().getString(KEY_UDN);
        final CdsObject object = getArguments().getParcelable(KEY_OBJECT);
        builder.setItems(choices, (dialog, which) ->
                ItemSelectUtils.send(getActivity(), udn, object, rendererList.get(which).getUdn()));
        return builder.create();
    }
}
