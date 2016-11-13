/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mm2d.cds.MediaServer;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailFragment extends Fragment
        implements PropertyAdapter.OnItemLinkClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.frg_server_detail, container, false);
        final String udn = getArguments().getString(Const.EXTRA_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getMediaServer(udn);
        if (server == null) {
            getActivity().finish();
            return rootView;
        }
        final TextView titleView = (TextView) rootView.findViewById(R.id.title);
        if (titleView != null) {
            titleView.setText(server.getFriendlyName());
            titleView.setBackgroundColor(ThemeUtils.getAccentColor(server.getFriendlyName()));
        }
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.server_detail);
        final PropertyAdapter adapter = new PropertyAdapter(getContext());
        setupPropertyAdapter(adapter, server);
        adapter.setOnItemLinkClickListener(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if (fab == null) {
            fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        }
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(getContext(), CdsListActivity.class);
                    intent.putExtra(Const.EXTRA_UDN, udn);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final TransitionSet ts = new TransitionSet();
                        ts.addTransition(new Slide(Gravity.START));
                        ts.addTransition(new Fade());
                        getActivity().getWindow().setExitTransition(ts);
                        startActivity(intent, ActivityOptions
                                .makeSceneTransitionAnimation(getActivity(), view, "share")
                                .toBundle());
                    } else {
                        startActivity(intent);
                    }
                }
            });
        }
        return rootView;
    }

    private void setupPropertyAdapter(PropertyAdapter adapter, MediaServer server) {
        adapter.addEntry("FriendlyName:", server.getFriendlyName());
        adapter.addEntry("SerialNumber:", server.getSerialNumber());
        adapter.addEntry("IP Address:", server.getIpAddress());
        adapter.addEntry("UDN:", server.getUdn());
        adapter.addEntry("Manufacture:", server.getManufacture());
        adapter.addEntry("ManufactureUrl:", server.getManufactureUrl(), true);
        adapter.addEntry("ModelName:", server.getModelName());
        adapter.addEntry("ModelUrl:", server.getModelUrl(), true);
        adapter.addEntry("ModelDescription:", server.getModelDescription());
        adapter.addEntry("ModelNumber:", server.getModelNumber());
        adapter.addEntry("PresentationUrl:", server.getPresentationUrl(), true);
        adapter.addEntry("Location:", server.getLocation());
        adapter.addEntry(" ", " ");
    }

    @Override
    public void onItemLinkClick(String link) {
        final Uri uri = Uri.parse(link);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
