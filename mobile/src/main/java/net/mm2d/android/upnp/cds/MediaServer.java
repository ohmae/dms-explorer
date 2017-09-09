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

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * MediaServerを表現するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MediaServer extends DeviceWrapper {
    private static final String BROWSE = "Browse";
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
        Completable.create(emitter -> mCdsService.subscribe(true))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Log::w);
    }

    /**
     * CDSサービスの購読を中止する。
     */
    public void unsubscribe() {
        Completable.create(emitter -> mCdsService.unsubscribe())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Log::w);
    }

    /**
     * Browseを実行する。
     *
     * @param objectId ObjectID
     * @return 結果
     */
    @NonNull
    public Observable<CdsObject> browse(@NonNull String objectId) {
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
    public Observable<CdsObject> browse(
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
    public Observable<CdsObject> browse(
            @NonNull String objectId,
            int startingIndex,
            int requestedCount) {
        return browse(objectId, "*", null, startingIndex, requestedCount);
    }

    private static final int REQUEST_MAX = 10;

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
    public Observable<CdsObject> browse(
            @NonNull final String objectId,
            @Nullable final String filter,
            @Nullable final String sortCriteria,
            final int startingIndex,
            final int requestedCount) {
        return browseInner(objectId, filter, sortCriteria, startingIndex, requestedCount)
                .flatMap(Observable::fromIterable)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    private Observable<List<CdsObject>> browseInner(
            @NonNull final String objectId,
            @Nullable final String filter,
            @Nullable final String sortCriteria,
            final int startingIndex,
            final int requestedCount) {
        final BrowseArgument argument = new BrowseArgument()
                .setObjectId(objectId)
                .setBrowseDirectChildren()
                .setFilter(filter)
                .setSortCriteria(sortCriteria);
        final int request = requestedCount == 0 ? Integer.MAX_VALUE : requestedCount;
        return Observable.create(emitter -> {
            int start = startingIndex;
            while (!emitter.isDisposed()) {
                argument.setStartIndex(start)
                        .setRequestCount(Math.min(request - start, REQUEST_MAX));
                final BrowseResponse response = new BrowseResponse(mBrowse.invoke(argument.get()));
                final int number = response.getNumberReturned();
                final int total = response.getTotalMatches();
                if (number == 0 || total == 0) {
                    emitter.onComplete();
                    return;
                }
                final List<CdsObject> result = CdsObjectFactory.parseDirectChildren(getUdn(), response.getResult());
                if (result.size() == 0 || number < 0 || total < 0) {
                    emitter.onError(new IllegalStateException());
                    return;
                }
                start += number;
                emitter.onNext(result);
                if (start >= total || start >= request) {
                    break;
                }
            }
            emitter.onComplete();
        });
    }

    /**
     * BrowseMetadataを実行する。
     *
     * @param objectId ObjectID
     * @return 結果
     */
    @NonNull
    public Single<CdsObject> browseMetadata(@NonNull final String objectId) {
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
    public Single<CdsObject> browseMetadata(
            @NonNull final String objectId,
            @Nullable final String filter) {
        final BrowseArgument argument = new BrowseArgument()
                .setObjectId(objectId)
                .setBrowseMetadata()
                .setFilter(filter)
                .setSortCriteria("")
                .setStartIndex(0)
                .setRequestCount(0);
        return Single.create((SingleOnSubscribe<CdsObject>) emitter -> {
            final BrowseResponse response = new BrowseResponse(mBrowse.invoke(argument.get()));
            final CdsObject result = CdsObjectFactory.parseMetadata(getUdn(), response.getResult());
            if (result == null || response.getNumberReturned() < 0 || response.getTotalMatches() < 0) {
                emitter.onError(new IllegalStateException());
                return;
            }
            emitter.onSuccess(result);
        }).subscribeOn(Schedulers.io());
    }
}
