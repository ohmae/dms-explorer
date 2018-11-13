/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class BrowseArgument {
    private static final String OBJECT_ID = "ObjectID";
    private static final String BROWSE_FLAG = "BrowseFlag";
    private static final String BROWSE_DIRECT_CHILDREN = "BrowseDirectChildren";
    private static final String BROWSE_METADATA = "BrowseMetadata";
    private static final String FILTER = "Filter";
    private static final String SORT_CRITERIA = "SortCriteria";
    private static final String START_INDEX = "StartingIndex";
    private static final String REQUESTED_COUNT = "RequestedCount";
    @NonNull
    private final Map<String, String> mArgument;

    BrowseArgument() {
        mArgument = new ArrayMap<>(6);
        setBrowseDirectChildren();
    }

    @NonNull
    BrowseArgument setObjectId(@NonNull final String objectId) {
        mArgument.put(OBJECT_ID, objectId);
        return this;
    }

    @NonNull
    BrowseArgument setBrowseDirectChildren() {
        mArgument.put(BROWSE_FLAG, BROWSE_DIRECT_CHILDREN);
        return this;
    }

    @NonNull
    BrowseArgument setBrowseMetadata() {
        mArgument.put(BROWSE_FLAG, BROWSE_METADATA);
        return this;
    }

    @NonNull
    BrowseArgument setFilter(@Nullable final String filter) {
        mArgument.put(FILTER, filter);
        return this;
    }

    @NonNull
    BrowseArgument setSortCriteria(@Nullable final String sortCriteria) {
        mArgument.put(SORT_CRITERIA, sortCriteria);
        return this;
    }

    @NonNull
    BrowseArgument setStartIndex(final int startIndex) {
        mArgument.put(START_INDEX, String.valueOf(startIndex));
        return this;
    }

    @NonNull
    BrowseArgument setRequestCount(final int requestCount) {
        mArgument.put(REQUESTED_COUNT, String.valueOf(requestCount));
        return this;
    }

    @NonNull
    Map<String, String> get() {
        return mArgument;
    }
}
