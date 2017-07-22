/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import net.mm2d.android.util.LaunchUtils;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class UpdateDialog extends DialogFragment {
    @NonNull
    public static UpdateDialog newInstance() {
        return new UpdateDialog();
    }

    public static void show(@NonNull final Activity activity) {
        newInstance().show(activity.getFragmentManager(), "");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getActivity();
        return new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_update)
                .setMessage(R.string.dialog_message_update)
                .setPositiveButton(R.string.dialog_button_ok, (dialog, which) ->
                        LaunchUtils.openGooglePlay(context, Const.PACKAGE_NAME))
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .create();
    }
}
