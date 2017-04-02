/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.ActivityUtils;
import net.mm2d.dmsexplorer.adapter.ServerPropertyAdapter;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * メディアサーバの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailFragment extends Fragment {
    private static final String KEY_TWO_PANE = "KEY_TWO_PANE";

    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @return インスタンス
     */
    public static ServerDetailFragment newInstance() {
        final ServerDetailFragment instance = new ServerDetailFragment();
        final Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_TWO_PANE, true);
        instance.setArguments(arguments);
        return instance;
    }

    private boolean isTwoPane() {
        final Bundle arguments = getArguments();
        if (arguments == null) {
            return false;
        }
        return arguments.getBoolean(KEY_TWO_PANE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final View rootView = inflater.inflate(R.layout.server_detail_fragment, container, false);
        final ControlPointModel model = DataHolder.getInstance().getControlPointModel();
        final MediaServer server = model.getSelectedMediaServer();
        if (server == null) {
            activity.finish();
            return rootView;
        }
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.serverDetailToolbar);
        toolbar.setTitle(server.getFriendlyName());

        ToolbarThemeUtils.setServerDetailTheme(getActivity(), server,
                (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbarLayout), !isTwoPane());

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.serverDetail);
        recyclerView.setAdapter(new ServerPropertyAdapter(activity, server));

        setUpGoButton(activity, rootView.findViewById(R.id.fab), server.getUdn());
        return rootView;
    }

    public void setUpGoButton(@NonNull final Context context,
                              @NonNull final View button,
                              @NonNull final String udn) {
        button.setOnClickListener(view -> {
            final Intent intent = CdsListActivity.makeIntent(context, udn);
            context.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(view));
        });
    }
}
