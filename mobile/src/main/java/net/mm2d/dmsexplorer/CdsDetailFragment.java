/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.android.util.AribUtils;

import java.util.List;

/**
 * CDSアイテムの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsDetailFragment extends Fragment {
    private static final String TAG = "CdsDetailFragment";

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @param udn    表示するサーバのUDN
     * @param object 表示するObject
     * @return インスタンス。
     */
    public static CdsDetailFragment newInstance(String udn, CdsObject object) {
        final CdsDetailFragment instance = new CdsDetailFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_SERVER_UDN, udn);
        arguments.putParcelable(Const.EXTRA_OBJECT, object);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.frg_cds_detail, container, false);

        final String udn = getArguments().getString(Const.EXTRA_SERVER_UDN);
        final MediaServer server = DataHolder.getInstance().getMsControlPoint().getDevice(udn);
        final CdsObject object = getArguments().getParcelable(Const.EXTRA_OBJECT);
        if (object == null || server == null) {
            getActivity().finish();
            return rootView;
        }

        final TextView titleView = (TextView) rootView.findViewById(R.id.fragment_toolbar);
        if (titleView != null) {
            final String title = object.getTitle();
            titleView.setText(AribUtils.toDisplayableString(title));
            titleView.setBackgroundColor(ThemeUtils.getAccentColor(title));
        }

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cds_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new CdsPropertyAdapter(getActivity(), object));

        setUpPlayButton(rootView, object);
        setUpSendButton(rootView, udn, object);
        return rootView;
    }

    private void setUpPlayButton(View rootView, final CdsObject object) {
        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_play);
        fab.setVisibility(hasResource(object) ? View.VISIBLE : View.GONE);
        final boolean protectedResource = hasProtectedResource(object);
        final int color = protectedResource ? Color.GRAY : ContextCompat.getColor(getContext(), R.color.accent);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));
        fab.setOnClickListener(view -> {
            if (protectedResource) {
                Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            } else {
                ItemSelectHelper.play(getActivity(), object, 0);
            }
        });
        fab.setOnLongClickListener(view -> {
            if (protectedResource) {
                Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
            } else {
                ItemSelectHelper.play(getActivity(), object);
            }
            return true;
        });
    }

    private void setUpSendButton(View rootView, final String udn, final CdsObject object) {
        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_send);
        final MrControlPoint cp = DataHolder.getInstance().getMrControlPoint();
        if (cp.getDeviceListSize() == 0) {
            fab.setVisibility(View.GONE);
            return;
        }
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> ItemSelectHelper.send(getActivity(), udn, object));
    }

    private static boolean hasResource(CdsObject object) {
        return object.getTagList(CdsObject.RES) != null;
    }

    private static boolean hasProtectedResource(CdsObject object) {
        final List<Tag> tagList = object.getTagList(CdsObject.RES);
        if (tagList == null) {
            return false;
        }
        for (final Tag tag : tagList) {
            final String protocolInfo = tag.getAttribute(CdsObject.PROTOCOL_INFO);
            final String mimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
            if (!TextUtils.isEmpty(mimeType) && mimeType.equals("application/x-dtcp1")) {
                return true;
            }
        }
        return false;
    }
}
