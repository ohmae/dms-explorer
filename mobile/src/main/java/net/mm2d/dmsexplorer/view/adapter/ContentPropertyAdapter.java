/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter;

import android.content.Context;
import android.util.Pair;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ContentPropertyAdapter extends PropertyAdapter {
    ContentPropertyAdapter(
            @NonNull final Context context,
            @NonNull final ContentEntity entity) {
        super(context);
        setCdsObjectInfo(context, this, (CdsObject) entity.getObject());
    }

    private static void setCdsObjectInfo(
            @NonNull final Context context,
            @NonNull final PropertyAdapter adapter,
            @NonNull final CdsObject object) {
        adapter.addEntry(context.getString(R.string.prop_title),
                AribUtils.toDisplayableString(object.getTitle()));
        adapter.addEntry(context.getString(R.string.prop_channel),
                CdsFormatter.makeChannel(object));
        adapter.addEntry(context.getString(R.string.prop_date),
                CdsFormatter.makeDate(object));
        adapter.addEntry(context.getString(R.string.prop_schedule),
                CdsFormatter.makeSchedule(object));
        adapter.addEntry(context.getString(R.string.prop_genre),
                CdsFormatter.makeGenre(object));
        adapter.addEntry(context.getString(R.string.prop_album),
                CdsFormatter.makeAlbum(object));
        adapter.addEntry(context.getString(R.string.prop_artist),
                CdsFormatter.makeArtists(object));
        adapter.addEntry(context.getString(R.string.prop_actor),
                CdsFormatter.makeActors(object));
        adapter.addEntry(context.getString(R.string.prop_author),
                CdsFormatter.makeAuthors(object));
        adapter.addEntry(context.getString(R.string.prop_creator),
                CdsFormatter.makeCreator(object));

        adapter.addEntry(context.getString(R.string.prop_description),
                CdsFormatter.makeDescription(object));
        final List<Pair<String, String>> longDescriptions = CdsFormatter.parseLongDescription(object);

        if (longDescriptions.size() != 0) {
            adapter.addTitleEntry(context.getString(R.string.prop_long_description));
            for (final Pair<String, String> pair : longDescriptions) {
                adapter.addEntry(pair.first, pair.second, Type.DESCRIPTION);
            }
        } else {
            adapter.addEntry(context.getString(R.string.prop_long_description),
                    CdsFormatter.makeUpnpLongDescription(object));
        }

        adapter.addEntry(CdsObject.UPNP_CLASS + ":",
                object.getUpnpClass());
    }
}
