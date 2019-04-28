/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import androidx.collection.ArrayMap

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class BrowseArgument {
    private val argument: MutableMap<String, String?>

    init {
        argument = ArrayMap(6)
        setBrowseDirectChildren()
    }

    fun setObjectId(objectId: String): BrowseArgument {
        argument[OBJECT_ID] = objectId
        return this
    }

    fun setBrowseDirectChildren(): BrowseArgument {
        argument[BROWSE_FLAG] = BROWSE_DIRECT_CHILDREN
        return this
    }

    fun setBrowseMetadata(): BrowseArgument {
        argument[BROWSE_FLAG] = BROWSE_METADATA
        return this
    }

    fun setFilter(filter: String?): BrowseArgument {
        argument[FILTER] = filter
        return this
    }

    fun setSortCriteria(sortCriteria: String?): BrowseArgument {
        argument[SORT_CRITERIA] = sortCriteria
        return this
    }

    fun setStartIndex(startIndex: Int): BrowseArgument {
        argument[START_INDEX] = startIndex.toString()
        return this
    }

    fun setRequestCount(requestCount: Int): BrowseArgument {
        argument[REQUESTED_COUNT] = requestCount.toString()
        return this
    }

    fun get(): Map<String, String?> {
        return argument
    }

    companion object {
        private const val OBJECT_ID = "ObjectID"
        private const val BROWSE_FLAG = "BrowseFlag"
        private const val BROWSE_DIRECT_CHILDREN = "BrowseDirectChildren"
        private const val BROWSE_METADATA = "BrowseMetadata"
        private const val FILTER = "Filter"
        private const val SORT_CRITERIA = "SortCriteria"
        private const val START_INDEX = "StartingIndex"
        private const val REQUESTED_COUNT = "RequestedCount"
    }
}
