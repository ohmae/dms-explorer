/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

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

    MediaServer(Device device) {
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            throw new IllegalArgumentException("device is not MediaServer");
        }
        mDevice = device;
        mCdsService = mDevice.findServiceById(Cds.CDS_SERVICE_ID);
        mBrowse = mCdsService.findAction(BROWSE);
    }

    public String getIpAddress() {
        return mDevice.getIpAddress();
    }

    public String getUdn() {
        return mDevice.getUdn();
    }

    public String getFriendlyName() {
        return mDevice.getFriendlyName();
    }

    public String getManufacture() {
        return mDevice.getManufacture();
    }

    public String getManufactureUrl() {
        return mDevice.getManufactureUrl();
    }

    public String getModelName() {
        return mDevice.getModelName();
    }

    public String getModelUrl() {
        return mDevice.getModelUrl();
    }

    public String getModelDescription() {
        return mDevice.getModelDescription();
    }

    public String getModelNumber() {
        return mDevice.getModelNumber();
    }

    public String getSerialNumber() {
        return mDevice.getSerialNumber();
    }

    public String getPresentationUrl() {
        return mDevice.getPresentationUrl();
    }

    public String getLocation() {
        return mDevice.getLocation();
    }

    public boolean subscribe() {
        try {
            return mCdsService.subscribe(true);
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return false;
    }

    public boolean unsubscribe() {
        try {
            return mCdsService.unsubscribe();
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return false;
    }

    private synchronized void execute(Runnable command) {
        new Thread(command).start();
    }

    public BrowseResult browse(String objectId) {
        return browse(objectId, "*", null);
    }

    public BrowseResult browse(String objectId, String filter, String sortCriteria) {
        return browse(objectId, filter, sortCriteria, 0, 0);
    }

    public BrowseResult browse(String objectId, int startingIndex, int requestedCount) {
        return browse(objectId, "*", null, startingIndex, requestedCount);
    }

    public BrowseResult browse(String objectId, String filter, String sortCriteria,
                               int startingIndex, int requestedCount) {
        final BrowseResult result = new BrowseResult(objectId, filter, sortCriteria,
                startingIndex, requestedCount);
        execute(new BrowseThread(mBrowse, result));
        return result;
    }

    public BrowseMetadataResult browseMetadata(String objectId) {
        return browseMetadata(objectId, "*");
    }

    public BrowseMetadataResult browseMetadata(String objectId, String filter) {
        final BrowseMetadataResult result = new BrowseMetadataResult(objectId, filter);
        execute(new BrowseMetadataThread(mBrowse, result));
        return result;
    }

    private static class BrowseThread implements Runnable {
        private static final int REQUEST_MAX = 10;
        private final Action mBrowse;
        private final BrowseResult mResult;

        BrowseThread(Action browse, BrowseResult result) {
            mBrowse = browse;
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
            int start = mResult.getStartingIndex();
            int request = mResult.getRequestedCount();
            request = request == 0 ? Integer.MAX_VALUE : request;
            final Map<String, String> arg = new HashMap<>();
            arg.put(OBJECT_ID, mResult.getObjectId());
            arg.put(BROWSE_FLAG, BROWSE_DIRECT_CHILDREN);
            arg.put(FILTER, mResult.getFilter());
            arg.put(SORT_CRITERIA, mResult.getSortCriteria());
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

    private static class BrowseMetadataThread implements Runnable {
        private final Action mBrowse;
        private final BrowseMetadataResult mResult;

        BrowseMetadataThread(Action browse, BrowseMetadataResult result) {
            mBrowse = browse;
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
            arg.put(OBJECT_ID, mResult.getObjectId());
            arg.put(BROWSE_FLAG, BROWSE_METADATA);
            arg.put(FILTER, mResult.getFilter());
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

    private static int parseIntSafely(String value, int defaultValue) {
        if (value == null) {
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
