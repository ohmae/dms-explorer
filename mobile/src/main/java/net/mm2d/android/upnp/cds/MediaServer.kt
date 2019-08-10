/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import net.mm2d.android.upnp.DeviceWrapper
import net.mm2d.log.Logger
import net.mm2d.upnp.Action
import net.mm2d.upnp.Device
import net.mm2d.upnp.Service
import java.util.*

/**
 * MediaServerを表現するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MediaServer
/**
 * インスタンスを作成する。
 *
 * パッケージ外でのインスタンス化禁止
 *
 * @param device デバイス
 */
internal constructor(device: Device) : DeviceWrapper(device) {
    private val cdsService: Service
    private val browse: Action
    private val destroyObject: Action?

    init {
        if (!device.deviceType.startsWith(Cds.MS_DEVICE_TYPE)) {
            throw IllegalArgumentException("device is not MediaServer")
        }
        cdsService = device.findServiceById(Cds.CDS_SERVICE_ID)
            ?: throw IllegalArgumentException("Device don't have cds service")
        browse = cdsService.findAction(BROWSE)
            ?: throw IllegalArgumentException("Device don't have browse action")
        destroyObject = cdsService.findAction(DESTROY_OBJECT)
    }

    /**
     * CDSサービスを購読する。
     */
    fun subscribe() {
        cdsService.subscribe(true, null)
    }

    /**
     * CDSサービスの購読を中止する。
     */
    fun unsubscribe() {
        cdsService.unsubscribe(null)
    }

    fun hasDestroyObject(): Boolean = destroyObject != null

    fun destroyObject(objectId: String): Single<Int> = if (destroyObject == null) {
        Single.never()
    } else Single.create { emitter: SingleEmitter<Int> ->
        val result =
            destroyObject.invokeSync(Collections.singletonMap(OBJECT_ID, objectId), false)
        val errorDescription = result[Action.ERROR_DESCRIPTION_KEY]
        if (!errorDescription.isNullOrEmpty()) {
            Logger.e { errorDescription }
        }
        emitter.onSuccess(result[Action.ERROR_CODE_KEY]?.toIntOrNull() ?: NO_ERROR)
    }

    /**
     * Broseを実行する。
     *
     * @param objectId       ObjectID
     * @param startingIndex  startIndex
     * @param requestedCount requestedCount
     * @return 結果
     */
    fun browse(
        objectId: String,
        startingIndex: Int,
        requestedCount: Int
    ): Observable<CdsObject> = browse(objectId, "*", null, startingIndex, requestedCount)

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
    fun browse(
        objectId: String,
        filter: String? = "*",
        sortCriteria: String? = null,
        startingIndex: Int = 0,
        requestedCount: Int = 0
    ): Observable<CdsObject> {
        val argument = BrowseArgument()
            .setObjectId(objectId)
            .setBrowseDirectChildren()
            .setFilter(filter)
            .setSortCriteria(sortCriteria)
        val request = if (requestedCount == 0) Integer.MAX_VALUE else requestedCount
        return Observable.create { emitter: ObservableEmitter<List<CdsObject>> ->
            var start = startingIndex
            while (!emitter.isDisposed) {
                argument.setStartIndex(start)
                    .setRequestCount(minOf(request - start, REQUEST_MAX))
                val response = BrowseResponse(browse.invokeSync(argument.get(), false))
                val number = response.numberReturned
                val total = response.totalMatches
                if (number == 0 || total == 0) {
                    emitter.onComplete()
                    return@create
                }
                val result = CdsObjectFactory.parseDirectChildren(udn, response.result)
                if (result.isEmpty() || number < 0 || total < 0) {
                    emitter.onError(IllegalStateException())
                    return@create
                }
                start += number
                emitter.onNext(result)
                if (start >= total || start >= request) {
                    break
                }
            }
            emitter.onComplete()
        }.flatMap { Observable.fromIterable(it) }
    }

    /**
     * BrowseMetadataを実行する。
     *
     * @param objectId ObjectID
     * @param filter   filter
     * @return 結果
     */
    fun browseMetadata(
        objectId: String,
        filter: String? = "*"
    ): Single<CdsObject> = Single.create { emitter: SingleEmitter<CdsObject> ->
        val argument = BrowseArgument()
            .setObjectId(objectId)
            .setBrowseMetadata()
            .setFilter(filter)
            .setSortCriteria("")
            .setStartIndex(0)
            .setRequestCount(0)
        val response = BrowseResponse(browse.invokeSync(argument.get(), false))
        val result = CdsObjectFactory.parseMetadata(udn, response.result)
        if (result == null || response.numberReturned < 0 || response.totalMatches < 0) {
            emitter.onError(IllegalStateException())
            return@create
        }
        emitter.onSuccess(result)
    }

    companion object {
        const val NO_ERROR = 0
        private const val BROWSE = "Browse"
        private const val DESTROY_OBJECT = "DestroyObject"
        private const val OBJECT_ID = "ObjectID"
        private const val REQUEST_MAX = 10
    }
}
