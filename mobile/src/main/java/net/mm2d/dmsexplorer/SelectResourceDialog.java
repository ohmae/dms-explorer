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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import net.mm2d.android.cds.CdsObject;
import net.mm2d.android.cds.Tag;

import java.util.ArrayList;
import java.util.List;

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
    public static SelectResourceDialog newInstance(CdsObject object) {
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
        final String[] selection = makeSelection(object);
        builder.setItems(selection,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Tag res = object.getTag(CdsObject.RES, which);
                        launch(res);
                    }
                });
        if (selection.length == 1) {
            launch(object.getTag(CdsObject.RES, 0));
        }
        return builder.create();
    }

    private void launch(Tag res) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final CdsObject object = getArguments().getParcelable(KEY_OBJECT);
        final String protocolInfo = res.getAttribute(CdsObject.PROTOCOL_INFO);
        final String mimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
        final Uri uri = Uri.parse(res.getValue());
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        switch (object.getType()) {
            case CdsObject.TYPE_VIDEO:
                if (pref.getBoolean(Const.LAUNCH_APP_MOVIE, true)) {
                    intent.setClass(getActivity(), MovieActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_AUDIO:
                if (pref.getBoolean(Const.LAUNCH_APP_MUSIC, true)) {
                    intent.setClass(getActivity(), MusicActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            case CdsObject.TYPE_IMAGE:
                if (pref.getBoolean(Const.LAUNCH_APP_PHOTO, true)) {
                    intent.setClass(getActivity(), PhotoActivity.class);
                    intent.putExtra(Const.EXTRA_OBJECT, object);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            default:
                dismiss();
                return;
        }
        try {
            startActivity(intent);
        } catch (final ActivityNotFoundException ignored) {
        }
        dismiss();
    }

    private String[] makeSelection(CdsObject object) {
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
