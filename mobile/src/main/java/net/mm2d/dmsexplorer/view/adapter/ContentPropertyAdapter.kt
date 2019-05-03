/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter

import android.content.Context

import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.util.AribUtils
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ContentPropertyAdapter(
    context: Context,
    entity: ContentEntity
) : PropertyAdapter(context) {
    init {
        setCdsObjectInfo(context, this, entity.getObject() as CdsObject)
    }

    private fun setCdsObjectInfo(
        context: Context,
        adapter: PropertyAdapter,
        cdsObject: CdsObject
    ) {
        adapter.addEntry(
            context.getString(R.string.prop_title),
            AribUtils.toDisplayableString(cdsObject.title)
        )
        adapter.addEntry(
            context.getString(R.string.prop_channel),
            CdsFormatter.makeChannel(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_date),
            CdsFormatter.makeDate(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_schedule),
            CdsFormatter.makeSchedule(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_genre),
            CdsFormatter.makeGenre(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_album),
            CdsFormatter.makeAlbum(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_artist),
            CdsFormatter.makeArtists(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_actor),
            CdsFormatter.makeActors(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_author),
            CdsFormatter.makeAuthors(cdsObject)
        )
        adapter.addEntry(
            context.getString(R.string.prop_creator),
            CdsFormatter.makeCreator(cdsObject)
        )

        adapter.addEntry(
            context.getString(R.string.prop_description),
            CdsFormatter.makeDescription(cdsObject)
        )
        val longDescriptions = CdsFormatter.parseLongDescription(cdsObject)

        if (longDescriptions.isNotEmpty()) {
            adapter.addTitleEntry(context.getString(R.string.prop_long_description))
            longDescriptions.forEach {
                adapter.addEntry(it.first, it.second, Type.DESCRIPTION)
            }
        } else {
            adapter.addEntry(
                context.getString(R.string.prop_long_description),
                CdsFormatter.makeUpnpLongDescription(cdsObject)
            )
        }
        adapter.addEntry(
            CdsObject.UPNP_CLASS + ":",
            cdsObject.upnpClass
        )
    }
}
