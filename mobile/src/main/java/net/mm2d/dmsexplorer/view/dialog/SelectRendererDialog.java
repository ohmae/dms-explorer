/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.RendererSelectDialogBinding;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
import net.mm2d.dmsexplorer.view.adapter.RendererListAdapter;

import java.util.List;

/**
 * 再生機器選択を行うダイアログ。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SelectRendererDialog extends DialogFragment {
    public static SelectRendererDialog newInstance() {
        return new SelectRendererDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ControlPointModel model = Repository.get().getControlPointModel();
        final MrControlPoint cp = model.getMrControlPoint();
        final List<MediaRenderer> rendererList = cp.getDeviceList();
        if (rendererList.isEmpty()) {
            dismiss();
            return builder.create();
        }
        final RendererListAdapter adapter = new RendererListAdapter(getActivity(), rendererList);
        adapter.setOnItemClickListener((v, renderer) -> {
            model.setSelectedMediaRenderer(renderer);
            ItemSelectUtils.sendSelectedRenderer(getActivity());
            dismiss();
        });
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final RendererSelectDialogBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.renderer_select_dialog, null, false);
        binding.rendererList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rendererList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        binding.rendererList.setAdapter(adapter);
        builder.setTitle(R.string.title_dialog_select_device);
        builder.setView(binding.getRoot());
        return builder.create();
    }
}
