/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.DeviceWrapper;
import net.mm2d.upnp.Action;
import net.mm2d.upnp.Device;
import net.mm2d.upnp.Service;
import net.mm2d.util.Log;
import net.mm2d.util.TextParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaServerを表現するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MediaServer extends DeviceWrapper {
    private static final String BROWSE = "Browse";
    private static final String OBJECT_ID = "ObjectID";
    private static final String BROWSE_FLAG = "BrowseFlag";
    private static final String BROWSE_DIRECT_CHILDREN = "BrowseDirectChildren";
    private static final String BROWSE_METADATA = "BrowseMetadata";
    private static final String FILTER = "Filter";
    private static final String SORT_CRITERIA = "SortCriteria";
    private static final String START_INDEX = "StartingIndex";
    private static final String REQUESTED_COUNT = "RequestedCount";
    private static final String RESULT = "Result";
    private static final String NUMBER_RETURNED = "NumberReturned";
    private static final String TOTAL_MATCHES = "TotalMatches";
    @NonNull
    private final Service mCdsService;
    @NonNull
    private final Action mBrowse;

    /**
     * インスタンスを作成する。
     *
     * パッケージ外でのインスタンス化禁止
     *
     * @param device デバイス
     */
    MediaServer(@NonNull Device device) {
        super(device);
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            throw new IllegalArgumentException("device is not MediaServer");
        }
        final Service cdsService = device.findServiceById(Cds.CDS_SERVICE_ID);
        if (cdsService == null) {
            throw new IllegalArgumentException("Device don't have cds service");
        }
        final Action browse = cdsService.findAction(BROWSE);
        if (browse == null) {
            throw new IllegalArgumentException("Device don't have browse action");
        }
        mCdsService = cdsService;
        mBrowse = browse;
    }

    /**
     * CDSサービスを購読する。
     */
    public void subscribe() {
        new Thread(() -> {
            try {
                mCdsService.subscribe(true);
            } catch (final IOException e) {
                Log.w(e);
            }
        }).start();
    }

    /**
     * CDSサービスの購読を中止する。
     */
    public void unsubscribe() {
        new Thread(() -> {
            try {
                mCdsService.unsubscribe();
            } catch (final IOException e) {
                Log.w(e);
            }
        }).start();
    }

    private synchronized void execute(@NonNull Runnable command) {
        new Thread(command).start();
    }

    /**
     * Browseを実行する。
     *
     * @param objectId ObjectID
     * @return 結果
     */
    @NonNull
    public BrowseResult browse(@NonNull String objectId) {
        return browse(objectId, "*", null);
    }

    /**
     * Browseを実行する。
     *
     * @param objectId     ObjectID
     * @param filter       filter
     * @param sortCriteria sortCriteria
     * @return 結果
     */
    @NonNull
    public BrowseResult browse(
            @NonNull String objectId,
            @Nullable String filter,
            @Nullable String sortCriteria) {
        return browse(objectId, filter, sortCriteria, 0, 0);
    }

    /**
     * Broseを実行する。
     *
     * @param objectId       ObjectID
     * @param startingIndex  startIndex
     * @param requestedCount requestedCount
     * @return 結果
     */
    @NonNull
    public BrowseResult browse(
            @NonNull String objectId,
            int startingIndex,
            int requestedCount) {
        return browse(objectId, "*", null, startingIndex, requestedCount);
    }

    /**
     * Browseを実行する。
     *
     * @param objectId       ObjectID
     * @param filter         filter
     * @param sortCriteria   sortCriteria
     * @param startingIndex  startIndex
     * @param requestedCount requestedCount
     * @return 結果
     */
    @NonNull
    public BrowseResult browse(
            @NonNull String objectId,
            @Nullable String filter,
            @Nullable String sortCriteria,
            int startingIndex,
            int requestedCount) {
        final BrowseRequest request = new BrowseRequest(objectId, filter, sortCriteria,
                startingIndex, requestedCount);
        final BrowseResult result = new BrowseResult();
        execute(new BrowseTask(getUdn(), mBrowse, request, result));
        return result;
    }

    /**
     * BrowseMetadataを実行する。
     *
     * @param objectId ObjectID
     * @return 結果
     */
    @NonNull
    public BrowseMetadataResult browseMetadata(@NonNull String objectId) {
        return browseMetadata(objectId, "*");
    }

    /**
     * BrowseMetadataを実行する。
     *
     * @param objectId ObjectID
     * @param filter   filter
     * @return 結果
     */
    @NonNull
    public BrowseMetadataResult browseMetadata(
            @NonNull String objectId,
            @Nullable String filter) {
        final BrowseMetadataRequest request = new BrowseMetadataRequest(objectId, filter);
        final BrowseMetadataResult result = new BrowseMetadataResult();
        execute(new BrowseMetadataTask(getUdn(), mBrowse, request, result));
        return result;
    }

    /**
     * Browseの引数をまとめるためのクラス。
     */
    private static class BrowseRequest {
        private final String objectId;
        private final String filter;
        private final String sortCriteria;
        private final int startingIndex;
        private final int requestedCount;

        BrowseRequest(
                @NonNull String objectId,
                @Nullable String filter,
                @Nullable String sortCriteria,
                int startingIndex,
                int requestedCount) {
            this.objectId = objectId;
            this.filter = filter;
            this.sortCriteria = sortCriteria;
            this.startingIndex = startingIndex;
            this.requestedCount = requestedCount;
        }
    }

    /**
     * Browseを実行するタスク。
     */
    private static class BrowseTask implements Runnable {
        private static final int REQUEST_MAX = 10;
        @NonNull
        private final String mUdn;
        @NonNull
        private final Action mBrowse;
        @NonNull
        private final BrowseRequest mRequest;
        @NonNull
        private final BrowseResult mResult;

        BrowseTask(
                @NonNull final String udn,
                @NonNull final Action browse,
                @NonNull final BrowseRequest request,
                @NonNull final BrowseResult result) {
            mUdn = udn;
            mBrowse = browse;
            mRequest = request;
            mResult = result;
        }

        @Override
        public void run() {
            mResult.setThread(Thread.currentThread());
            if (mResult.isCancelled()) {
                mResult.set(null);
                return;
            }
            final List<CdsObject> list = new ArrayList<>();
            int start = mRequest.startingIndex;
            int request = mRequest.requestedCount;
            request = request == 0 ? Integer.MAX_VALUE : request;
            final Map<String, String> argument = makeArgument();
            try {
                while (!mResult.isCancelled()) {
                    final int count = request > REQUEST_MAX ? REQUEST_MAX : request;
                    final Map<String, String> res = mBrowse.invoke(setCount(argument, start, count));
                    final List<CdsObject> result = CdsObjectFactory
                            .parseDirectChildren(mUdn, res.get(RESULT));
                    final int number = TextParseUtils.parseIntSafely(res.get(NUMBER_RETURNED), -1);
                    final int total = TextParseUtils.parseIntSafely(res.get(TOTAL_MATCHES), -1);
                    if (number == 0 || total == 0) {
                        mResult.set(list);
                        return;
                    }
                    if (result.size() == 0 || number < 0 || total < 0) {
                        mResult.set(null);
                        return;
                    }
                    list.addAll(result);
                    start += number;
                    request -= number;
                    if (start >= total || request == 0) {
                        mResult.set(list);
                        return;
                    }
                    mResult.setProgress(list);
                }
            } catch (final IOException e) {
                Log.w(e);
            }
            mResult.set(null);
        }

        private Map<String, String> makeArgument() {
            final Map<String, String> argument = new HashMap<>();
            argument.put(OBJECT_ID, mRequest.objectId);
            argument.put(BROWSE_FLAG, BROWSE_DIRECT_CHILDREN);
            argument.put(FILTER, mRequest.filter);
            argument.put(SORT_CRITERIA, mRequest.sortCriteria);
            return argument;
        }

        private Map<String, String> setCount(
                Map<String, String> argument,
                int start,
                int count) {
            argument.put(START_INDEX, String.valueOf(start));
            argument.put(REQUESTED_COUNT, String.valueOf(count));
            return argument;
        }
    }

    /**
     * BrowseMetadataの引数をまとめるクラス。
     */
    private static class BrowseMetadataRequest {
        @NonNull
        private final String objectId;
        @Nullable
        private final String filter;

        BrowseMetadataRequest(
                @NonNull final String objectId,
                @Nullable final String filter) {
            this.objectId = objectId;
            this.filter = filter;
        }
    }

    /**
     * BrowseMetadataを実行するクラス。
     */
    private static class BrowseMetadataTask implements Runnable {
        @NonNull
        private final String mUdn;
        @NonNull
        private final Action mBrowse;
        @NonNull
        private final BrowseMetadataRequest mRequest;
        @NonNull
        private final BrowseMetadataResult mResult;

        BrowseMetadataTask(
                @NonNull final String udn,
                @NonNull final Action browse,
                @NonNull final BrowseMetadataRequest request,
                @NonNull final BrowseMetadataResult result) {
            mUdn = udn;
            mBrowse = browse;
            mRequest = request;
            mResult = result;
        }

        @Override
        public void run() {
            mResult.setThread(Thread.currentThread());
            if (mResult.isCancelled()) {
                mResult.set(null);
                return;
            }
            try {
                final Map<String, String> res = mBrowse.invoke(makeArgument());
                final CdsObject result = CdsObjectFactory.parseMetadata(mUdn, res.get(RESULT));
                final int number = TextParseUtils.parseIntSafely(res.get(NUMBER_RETURNED), -1);
                final int total = TextParseUtils.parseIntSafely(res.get(TOTAL_MATCHES), -1);
                if (result == null || number < 0 || total < 0) {
                    mResult.set(null);
                } else {
                    mResult.set(result);
                }
            } catch (final IOException e) {
                Log.w(e);
                mResult.set(null);
            }
        }

        private Map<String, String> makeArgument() {
            final Map<String, String> argument = new HashMap<>();
            argument.put(OBJECT_ID, mRequest.objectId);
            argument.put(BROWSE_FLAG, BROWSE_METADATA);
            argument.put(FILTER, mRequest.filter);
            argument.put(SORT_CRITERIA, "");
            argument.put(START_INDEX, "0");
            argument.put(REQUESTED_COUNT, "0");
            return argument;
        }
    }
}
