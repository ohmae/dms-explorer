/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.MediaServer;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * メディアサーバの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailFragment extends Fragment {

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @param udn メディアサーバーのUDN
     * @return インスタンス
     */
    public static ServerDetailFragment newInstance(@NonNull String udn) {
        final ServerDetailFragment instance = new ServerDetailFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_SERVER_UDN, udn);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.frg_server_detail, container, false);
        final String udn = getArguments().getString(Const.EXTRA_SERVER_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getDevice(udn);
        if (server == null) {
            getActivity().finish();
            return rootView;
        }
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.server_detail_toolbar);
        toolbar.setTitle(server.getFriendlyName());

        ToolbarThemeHelper.setServerDetailTheme(this, server,
                (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbar_layout));

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.server_detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ServerPropertyAdapter(getActivity(), server));

        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final Intent intent = CdsListActivity.makeIntent(getContext(), udn);
            startActivity(intent,
                    ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle());
        });
        return rootView;
    }
}
