/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.upnp.Action;
import net.mm2d.upnp.Device;
import net.mm2d.upnp.Service;
import net.mm2d.util.Log;

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
public class MediaServer {
    private static final String TAG = "MediaServer";
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
    private final Device mDevice;
    private final Service mCdsService;
    private final Action mBrowse;

    /**
     * インスタンスを作成する。
     *
     * パッケージ外でのインスタンス化禁止
     *
     * @param device
     */
    MediaServer(@NonNull Device device) {
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            throw new IllegalArgumentException("device is not MediaServer");
        }
        mDevice = device;
        mCdsService = mDevice.findServiceById(Cds.CDS_SERVICE_ID);
        mBrowse = mCdsService.findAction(BROWSE);
    }

    /**
     * Locationに記述のIPアドレスを返す。
     *
     * 記述に異常がある場合は空文字が返る。
     *
     * @return IPアドレス
     */
    @NonNull
    public String getIpAddress() {
        return mDevice.getIpAddress();
    }

    /**
     * UDNタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return UDNタグの値
     */
    @Nullable
    public String getUdn() {
        return mDevice.getUdn();
    }

    /**
     * friendlyNameタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return friendlyNameタグの値
     */
    @Nullable
    public String getFriendlyName() {
        return mDevice.getFriendlyName();
    }

    /**
     * manufacturerタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return manufacturerタグの値
     */
    @Nullable
    public String getManufacture() {
        return mDevice.getManufacture();
    }

    /**
     * manufacturerURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return manufacturerURLタグの値
     */
    @Nullable
    public String getManufactureUrl() {
        return mDevice.getManufactureUrl();
    }

    /**
     * modelNameタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelNameタグの値
     */
    @Nullable
    public String getModelName() {
        return mDevice.getModelName();
    }

    /**
     * modelURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelURLタグの値
     */
    @Nullable
    public String getModelUrl() {
        return mDevice.getModelUrl();
    }

    /**
     * modelDescriptionタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelDescriptionタグの値
     */
    @Nullable
    public String getModelDescription() {
        return mDevice.getModelDescription();
    }

    /**
     * modelNumberタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelNumberタグの値
     */
    @Nullable
    public String getModelNumber() {
        return mDevice.getModelNumber();
    }

    /**
     * serialNumberタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return serialNumberタグの値
     */
    @Nullable
    public String getSerialNumber() {
        return mDevice.getSerialNumber();
    }

    /**
     * presentationURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return presentationURLタグの値
     */
    @Nullable
    public String getPresentationUrl() {
        return mDevice.getPresentationUrl();
    }

    /**
     * SSDPパケットに記述されているLocationヘッダの値を返す。
     *
     * @return Locationヘッダの値
     */
    @Nullable
    public String getLocation() {
        return mDevice.getLocation();
    }

    /**
     * CDSサービスを購読する。
     *
     * @return 成功時true
     */
    public boolean subscribe() {
        try {
            return mCdsService.subscribe(true);
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return false;
    }

    /**
     * CDSサービスの購読を中止する。
     *
     * @return 成功時true
     */
    public boolean unsubscribe() {
        try {
            return mCdsService.unsubscribe();
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return false;
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
    public BrowseResult browse(@NonNull String objectId, @Nullable String filter,
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
    public BrowseResult browse(@NonNull String objectId, int startingIndex, int requestedCount) {
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
    public BrowseResult browse(@NonNull String objectId, @Nullable String filter,
                               @Nullable String sortCriteria,
                               int startingIndex, int requestedCount) {
        final BrowseRequest request = new BrowseRequest(objectId, filter, sortCriteria,
                startingIndex, requestedCount);
        final BrowseResult result = new BrowseResult();
        execute(new BrowseTask(mBrowse, request, result));
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
    public BrowseMetadataResult browseMetadata(@NonNull String objectId, @Nullable String filter) {
        final BrowseMetadataRequest request = new BrowseMetadataRequest(objectId, filter);
        final BrowseMetadataResult result = new BrowseMetadataResult();
        execute(new BrowseMetadataTask(mBrowse, request, result));
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

        BrowseRequest(@NonNull String objectId, @Nullable String filter,
                      @Nullable String sortCriteria,
                      int startingIndex, int requestedCount) {
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
        private final Action mBrowse;
        private final BrowseRequest mRequest;
        private final BrowseResult mResult;

        BrowseTask(@NonNull Action browse,
                   @NonNull BrowseRequest request,
                   @NonNull BrowseResult result) {
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
            final Map<String, String> arg = new HashMap<>();
            arg.put(OBJECT_ID, mRequest.objectId);
            arg.put(BROWSE_FLAG, BROWSE_DIRECT_CHILDREN);
            arg.put(FILTER, mRequest.filter);
            arg.put(SORT_CRITERIA, mRequest.sortCriteria);
            try {
                while (!mResult.isCancelled()) {
                    final int count = request > REQUEST_MAX ? REQUEST_MAX : request;
                    arg.put(START_INDEX, String.valueOf(start));
                    arg.put(REQUESTED_COUNT, String.valueOf(count));
                    final Map<String, String> res = mBrowse.invoke(arg);
                    final List<CdsObject> result = CdsObjectFactory
                            .parseDirectChildren(res.get(RESULT));
                    final int number = parseIntSafely(res.get(NUMBER_RETURNED), -1);
                    final int total = parseIntSafely(res.get(TOTAL_MATCHES), -1);
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
                    } else {
                        mResult.setProgress(list);
                    }
                }
            } catch (final IOException e) {
                Log.w(TAG, e);
            }
            mResult.set(null);
        }
    }

    /**
     * BrowseMetadataの引数をまとめるクラス。
     */
    private static class BrowseMetadataRequest {
        private final String objectId;
        private final String filter;

        BrowseMetadataRequest(@NonNull String objectId, @Nullable String filter) {
            this.objectId = objectId;
            this.filter = filter;
        }
    }

    /**
     * BrowseMetadataを実行するクラス。
     */
    private static class BrowseMetadataTask implements Runnable {
        private final Action mBrowse;
        private final BrowseMetadataRequest mRequest;
        private final BrowseMetadataResult mResult;

        BrowseMetadataTask(@NonNull Action browse,
                           @NonNull BrowseMetadataRequest request,
                           @NonNull BrowseMetadataResult result) {
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
            final Map<String, String> arg = new HashMap<>();
            arg.put(OBJECT_ID, mRequest.objectId);
            arg.put(BROWSE_FLAG, BROWSE_METADATA);
            arg.put(FILTER, mRequest.filter);
            arg.put(SORT_CRITERIA, "");
            arg.put(START_INDEX, "0");
            arg.put(REQUESTED_COUNT, "0");
            try {
                final Map<String, String> res = mBrowse.invoke(arg);
                final CdsObject result = CdsObjectFactory.parseMetadata(res.get(RESULT));
                final int number = parseIntSafely(res.get(NUMBER_RETURNED), -1);
                final int total = parseIntSafely(res.get(TOTAL_MATCHES), -1);
                if (result == null || number < 0 || total < 0) {
                    mResult.set(null);
                } else {
                    mResult.set(result);
                }
            } catch (final IOException e) {
                Log.w(TAG, e);
                mResult.set(null);
            }
        }
    }

    private static int parseIntSafely(@Nullable String value, int defaultValue) {
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MediaServer)) {
            return false;
        }
        final MediaServer m = (MediaServer) o;
        return mDevice.equals(m.mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    @Override
    public String toString() {
        return getFriendlyName();
    }
}
