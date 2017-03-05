/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * マルチリソースのコンテンツの再生時にリソースの選択を促すダイアログ。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class SelectResourceDialog extends DialogFragment {
    private static final String KEY_OBJECT = "KEY_OBJECT";
    private static final String TAG = "SelectResourceDialog";

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @param object 対象コンテンツのObject情報
     * @return インスタンス。
     */
    public static SelectResourceDialog newInstance(@Nonnull CdsObject object) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(KEY_OBJECT, object);
        final SelectResourceDialog instance = new SelectResourceDialog();
        instance.setArguments(arguments);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CdsObject object = getArguments().getParcelable(KEY_OBJECT);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_select_resource);
        final String[] choices = makeChoices(object);
        builder.setItems(choices,
                (dialog, which) -> ItemSelectHelper.play(getActivity(), object, which));
        return builder.create();
    }

    private String[] makeChoices(CdsObject object) {
        final List<String> itemList = new ArrayList<>();
        final List<Tag> tagList = object.getTagList(CdsObject.RES);
        for (final Tag tag : tagList) {
            final String bitrate = tag.getAttribute(CdsObject.BITRATE);
            final String resolution = tag.getAttribute(CdsObject.RESOLUTION);
            final String protocolInfo = tag.getAttribute(CdsObject.PROTOCOL_INFO);
            final String mimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
            final String protocol = CdsObject.extractProtocolFromProtocolInfo(protocolInfo);
            final StringBuilder sb = new StringBuilder();
            if (protocol != null) {
                sb.append(protocol);
            }
            if (mimeType != null) {
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(mimeType);
            }
            if (bitrate != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append("bitrate: ");
                sb.append(bitrate);
            }
            if (resolution != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append("resolution: ");
                sb.append(resolution);
            }
            itemList.add(sb.toString());
        }
        return itemList.toArray(new String[itemList.size()]);
    }
}
