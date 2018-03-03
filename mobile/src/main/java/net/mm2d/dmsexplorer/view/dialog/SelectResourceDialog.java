/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog;

import android.app.Dialog;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;

/**
 * マルチリソースのコンテンツの再生時にリソースの選択を促すダイアログ。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SelectResourceDialog extends DialogFragment {
    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @return インスタンス。
     */
    @NonNull
    public static SelectResourceDialog newInstance() {
        return new SelectResourceDialog();
    }

    public static void show(@NonNull final FragmentActivity activity) {
        if (activity.getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
            return;
        }
        newInstance().show(activity.getSupportFragmentManager(), "");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_select_resource);
        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        final String[] choices = targetModel.createResChoices();
        builder.setItems(choices,
                (dialog, which) -> ItemSelectUtils.play(getActivity(), which));
        return builder.create();
    }
}
