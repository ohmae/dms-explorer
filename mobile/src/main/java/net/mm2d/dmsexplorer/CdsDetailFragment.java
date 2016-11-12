/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mm2d.cds.CdsObject;
import net.mm2d.cds.MediaServer;
import net.mm2d.cds.Tag;
import net.mm2d.util.Arib;
import net.mm2d.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsDetailFragment extends Fragment
        implements PropertyAdapter.OnItemLinkClickListener {
    private static final String TAG = "CdsDetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.frg_cds_detail, container, false);
        final String udn = getArguments().getString(Const.EXTRA_UDN);
        final DataHolder dataHolder = DataHolder.getInstance();
        final MediaServer server = dataHolder.getMsControlPoint().getMediaServer(udn);
        final CdsObject object = getArguments().getParcelable(Const.EXTRA_OBJECT);
        if (object == null || server == null) {
            getActivity().finish();
            return rootView;
        }
        final String title = object.getTitle();
        final TextView titleView = (TextView) rootView.findViewById(R.id.title);
        if (titleView != null) {
            titleView.setText(Arib.toDisplayableString(title));
            titleView.setBackgroundColor(Utils.getAccentColor(title));
        }
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cds_detail);
        final PropertyAdapter adapter = new PropertyAdapter(getContext());
        adapter.setOnItemLinkClickListener(this);
        setupPropertyAdapter(getActivity(), adapter, object);
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if (fab == null) {
            fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        }
        if (fab != null) {
            final boolean hasResource = hasResource(object);
            if (hasResource) {
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.GONE);
            }
            final boolean protectedResource = hasProtectedResource(object);
            if (protectedResource) {
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else {
                fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.accent)));
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (protectedResource) {
                        Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
                    } else {
                        final SelectResourceDialog dialog = SelectResourceDialog.newInstance(object);
                        dialog.show(getActivity().getFragmentManager(), "");
                    }
                }
            });
        }
        return rootView;
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
            final String mimeType = CdsObject.getMimeTypeFromProtocolInfo(protocolInfo);
            if (mimeType.equals("application/x-dtcp1")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemLinkClick(String link) {
        final Uri uri = Uri.parse(link);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            Log.w(TAG, e);
        }
    }

    private static String sTb;
    private static String sBs;
    private static String sCs;

    private static void setupString(Context context) {
        if (sTb != null) {
            return;
        }
        sTb = context.getString(R.string.network_tb);
        sBs = context.getString(R.string.network_bs);
        sCs = context.getString(R.string.network_cs);
    }

    static void setupPropertyAdapter(Context context, PropertyAdapter adapter, CdsObject object) {
        Log.d(TAG, object.toDumpString());
        setupString(context);
        adapter.addEntry(context.getString(R.string.prop_title),
                Arib.toDisplayableString(object.getTitle()));
        adapter.addEntry(context.getString(R.string.prop_channel),
                getChannel(object));
        adapter.addEntry(context.getString(R.string.prop_date),
                getDate(object));
        adapter.addEntry(context.getString(R.string.prop_schedule),
                getSchedule(object));
        adapter.addEntry(context.getString(R.string.prop_genre),
                object.getValue(CdsObject.UPNP_GENRE));

        adapter.addEntry(context.getString(R.string.prop_album),
                object.getValue(CdsObject.UPNP_ALBUM));
        adapter.addEntry(context.getString(R.string.prop_artist),
                jointMembers(object, CdsObject.UPNP_ARTIST));
        adapter.addEntry(context.getString(R.string.prop_actor),
                jointMembers(object, CdsObject.UPNP_ACTOR));
        adapter.addEntry(context.getString(R.string.prop_author),
                jointMembers(object, CdsObject.UPNP_AUTHOR));
        adapter.addEntry(context.getString(R.string.prop_creator),
                object.getValue(CdsObject.DC_CREATOR));

        adapter.addEntry(context.getString(R.string.prop_description),
                jointTagValue(object, CdsObject.DC_DESCRIPTION));
        adapter.addEntryAutoLink(context.getString(R.string.prop_long_description),
                jointTagValue(object, CdsObject.ARIB_LONG_DESCRIPTION));
        adapter.addEntry(CdsObject.UPNP_CLASS + ":",
                object.getUpnpClass());
    }

    private static String jointTagValue(CdsObject object, String tagName) {
        final List<Tag> tagList = object.getTagList(tagName);
        if (tagList == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (final Tag tag : tagList) {
            if (sb.length() != 0) {
                sb.append('\n');
            }
            sb.append(tag.getValue());
        }
        return Arib.toDisplayableString(sb.toString());
    }

    private static String jointMembers(CdsObject object, String tagName) {
        final List<Tag> tagList = object.getTagList(tagName);
        if (tagList == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (final Tag tag : tagList) {
            if (sb.length() != 0) {
                sb.append('\n');
            }
            sb.append(tag.getValue());
            final String role = tag.getAttribute("role");
            if (role != null) {
                sb.append(" : ");
                sb.append(role);
            }
        }
        return sb.toString();
    }

    private static String getChannel(CdsObject object) {
        final StringBuilder sb = new StringBuilder();
        final String network = getNetworkString(object);
        if (network != null) {
            sb.append(network);
        }
        final String channelNr = object.getValue(CdsObject.UPNP_CHANNEL_NR);
        if (channelNr != null) {
            if (sb.length() == 0) {
                sb.append(channelNr);
            } else {
                try {
                    final int channel = Integer.parseInt(channelNr);
                    final String nr = String.format(Locale.US, "%1$06d", channel);
                    sb.append(nr.substring(2, 5));
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        final String name = object.getValue(CdsObject.UPNP_CHANNEL_NAME);
        if (name != null) {
            if (sb.length() != 0) {
                sb.append("   ");
            }
            sb.append(name);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static String getNetworkString(CdsObject object) {
        final String net = object.getValue(CdsObject.ARIB_OBJECT_TYPE);
        if (net == null) {
            return null;
        }
        switch (net) {
            case "ARIB_TB":
                return sTb;
            case "ARIB_BS":
                return sBs;
            case "ARIB_CS":
                return sCs;
            default:
                return null;
        }
    }

    private static String getDate(CdsObject object) {
        final String str = object.getValue(CdsObject.DC_DATE);
        final Date date = CdsObject.parseDate(str);
        if (date == null) {
            return null;
        }
        if (str.length() <= 10) {
            return DateFormat.format("yyyy/MM/dd (E)", date).toString();
        }
        return DateFormat.format("yyyy/M/d (E) kk:mm:ss", date).toString();
    }

    private static String getSchedule(CdsObject object) {
        final Date start = object.getDateValue(CdsObject.UPNP_SCHEDULED_START_TIME);
        final Date end = object.getDateValue(CdsObject.UPNP_SCHEDULED_END_TIME);
        if (start == null || end == null) {
            return null;
        }
        final String startString = DateFormat.format("yyyy/M/d (E) kk:mm", start).toString();
        final String endString;
        if (end.getTime() - start.getTime() > 12 * 3600 * 1000) {
            endString = DateFormat.format("yyyy/M/d (E) kk:mm", end).toString();
        } else {
            endString = DateFormat.format("kk:mm", end).toString();
        }
        return startString + " ～ " + endString;
    }
}
