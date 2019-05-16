/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt

import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.DeviceWrapper
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.CdsObjectXmlConverter
import net.mm2d.upnp.Action
import net.mm2d.upnp.Device
import net.mm2d.upnp.Service
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * MediaRendererを表現するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MediaRenderer internal constructor(
    private val mrControlPoint: MrControlPoint,
    device: Device
) : DeviceWrapper(device) {
    private val avTransport: Service
    private val setAvTransportUri: Action
    private val getPositionInfo: Action
    private val getTransportInfo: Action
    private val play: Action
    private val stop: Action
    private val seek: Action
    private val pause: Action?

    init {
        if (!device.deviceType.startsWith(Avt.MR_DEVICE_TYPE)) {
            throw IllegalArgumentException("device is not MediaRenderer")
        }
        avTransport = findService(device, Avt.AVT_SERVICE_ID)
        setAvTransportUri = findAction(avTransport, SET_AV_TRANSPORT_URI)
        getPositionInfo = findAction(avTransport, GET_POSITION_INFO)
        getTransportInfo = findAction(avTransport, GET_TRANSPORT_INFO)
        play = findAction(avTransport, PLAY)
        stop = findAction(avTransport, STOP)
        seek = findAction(avTransport, SEEK)
        pause = avTransport.findAction(PAUSE)
    }

    fun isSupportPause(): Boolean = pause != null

    fun getPositionInfo(): Single<Map<String, String>> =
        invoke(getPositionInfo, Collections.singletonMap(INSTANCE_ID, "0"))

    fun getTransportInfo(): Single<Map<String, String>> =
        invoke(getTransportInfo, Collections.singletonMap(INSTANCE_ID, "0"))

    /**
     * AVTransportサービスを購読する。
     */
    fun subscribe() {
        avTransport.subscribe(true, null)
    }

    /**
     * AVTransportサービスの購読を中止する。
     */
    fun unsubscribe() {
        avTransport.unsubscribe(null)
    }

    fun setAVTransportURI(
        cdsObject: CdsObject,
        uri: String
    ): Single<Map<String, String>> {
        val metadata = CdsObjectXmlConverter.convert(cdsObject)
        if (metadata.isNullOrEmpty()) {
            return Single.error(IllegalStateException("empty meta data"))
        }
        return invoke(
            setAvTransportUri, mapOf(
                INSTANCE_ID to "0",
                CURRENT_URI to uri,
                CURRENT_URI_META_DATA to metadata
            )
        )
    }

    fun clearAVTransportURI(): Single<Map<String, String>> {
        return invoke(
            setAvTransportUri, mapOf(
                INSTANCE_ID to "0",
                CURRENT_URI to null,
                CURRENT_URI_META_DATA to null
            )
        )
    }

    fun play(): Single<Map<String, String>> {
        val argument = HashMap<String, String>()
        argument[INSTANCE_ID] = "0"
        argument[SPEED] = "1"
        return invoke(play, argument)
    }

    fun stop(): Single<Map<String, String>> {
        return invoke(stop, Collections.singletonMap(INSTANCE_ID, "0"))
    }

    fun pause(): Single<Map<String, String>> {
        return if (pause == null) {
            Single.error(IllegalStateException("pause is not supported"))
        } else invoke(pause, Collections.singletonMap(INSTANCE_ID, "0"))
    }

    fun seek(time: Long): Single<Map<String, String>> {
        val unitArg = seek.findArgument(UNIT)
            ?: return Single.error(IllegalStateException("no unit argument"))
        val argument = HashMap<String, String>()
        argument[INSTANCE_ID] = "0"
        val timeText = makeTimeText(time)
        val unit = unitArg.relatedStateVariable
        val list = unit.allowedValueList
        when {
            list.contains(UNIT_REL_TIME) -> argument[UNIT] = UNIT_REL_TIME
            list.contains(UNIT_ABS_TIME) -> argument[UNIT] = UNIT_ABS_TIME
            else -> return Single.error(IllegalStateException("no supported unit"))
        }
        argument[TARGET] = timeText
        return invoke(seek, argument)
    }

    private operator fun invoke(
        action: Action,
        argument: Map<String, String?>
    ): Single<Map<String, String>> {
        return Single.create { emitter: SingleEmitter<Map<String, String>> ->
            try {
                val result = action.invokeSync(argument, false)
                emitter.onSuccess(result)
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    companion object {
        private const val SET_AV_TRANSPORT_URI = "SetAVTransportURI"
        private const val GET_MEDIA_INFO = "GetMediaInfo"
        private const val GET_TRANSPORT_INFO = "GetTransportInfo"
        private const val GET_POSITION_INFO = "GetPositionInfo"
        private const val GET_DEVICE_CAPABILITIES = "GetDeviceCapabilities"
        private const val GET_TRANSPORT_SETTINGS = "GetTransportSettings"
        private const val PLAY = "Play"
        private const val PAUSE = "Pause"
        private const val STOP = "Stop"
        private const val SEEK = "Seek"
        private const val NEXT = "Next"
        private const val PREVIOUS = "Previous"
        private const val GET_CURRENT_TRANSPORT_ACTIONS = "GetCurrentTransportActions"

        private const val INSTANCE_ID = "InstanceID"
        private const val CURRENT_URI = "CurrentURI"
        private const val CURRENT_URI_META_DATA = "CurrentURIMetaData"
        private const val NR_TRACKS = "NrTracks"
        private const val MEDIA_DURATION = "MediaDuration"
        private const val NEXT_URI = "NextURI"
        private const val NEXT_URI_META_DATA = "NextURIMetaData"
        private const val PLAY_MEDIUM = "PlayMedium"
        private const val RECORD_MEDIUM = "RecordMedium"
        private const val WRITE_STATUS = "WriteStatus"
        private const val CURRENT_TRANSPORT_STATE = "CurrentTransportState"
        private const val CURRENT_TRANSPORT_STATUS = "CurrentTransportStatus"
        private const val CURRENT_SPEED = "CurrentSpeed"
        private const val TRACK = "Track"
        private const val TRACK_DURATION = "TrackDuration"
        private const val TRACK_META_DATA = "TrackMetaData"
        private const val TRACK_URI = "TrackURI"
        private const val REL_TIME = "RelTime"
        private const val ABS_TIME = "AbsTime"
        private const val REL_COUNT = "RelCount"
        private const val ABS_COUNT = "AbsCount"
        private const val PLAY_MEDIA = "PlayMedia"
        private const val REC_MEDIA = "RecMedia"
        private const val REC_QUALITY_MODES = "RecQualityModes"
        private const val PLAY_MODE = "PlayMode"
        private const val REC_QUALITY_MODE = "RecQualityMode"
        private const val SPEED = "Speed"
        private const val UNIT = "Unit"
        private const val TARGET = "Target"
        private const val ACTIONS = "Actions"

        private const val NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
        private const val UNIT_REL_TIME = "REL_TIME"
        private const val UNIT_ABS_TIME = "ABS_TIME"

        private val ONE_SECOND = TimeUnit.SECONDS.toMillis(1)
        private val ONE_MINUTE = TimeUnit.MINUTES.toMillis(1)
        private val ONE_HOUR = TimeUnit.HOURS.toMillis(1)

        private fun findService(
            device: Device,
            id: String
        ): Service {
            return device.findServiceById(id)
                ?: throw IllegalArgumentException("Device doesn't have $id")
        }

        private fun findAction(
            service: Service,
            name: String
        ): Action {
            return service.findAction(name)
                ?: throw IllegalArgumentException("Service doesn't have $name")
        }

        @JvmStatic
        fun getCurrentTransportState(result: Map<String, String>): TransportState {
            return TransportState.of(result[CURRENT_TRANSPORT_STATE])
        }

        @JvmStatic
        fun getDuration(result: Map<String, String>): Int {
            return parseCount(result[TRACK_DURATION])
        }

        @JvmStatic
        fun getProgress(result: Map<String, String>): Int {
            val progress = parseCount(result[REL_TIME])
            return if (progress >= 0) {
                progress
            } else parseCount(result[ABS_TIME])
        }

        /**
         * 00:00:00.000形式の時間をミリ秒に変換する。小数点以下がない場合も想定する。
         *
         * @param count 変換する文字列
         * @return 変換したミリ秒時間
         */
        private fun parseCount(count: String?): Int {
            if (count.isNullOrEmpty() || count == NOT_IMPLEMENTED) {
                return -1
            }
            val section = count.split(':')
            if (section.size != 3) {
                return -1
            }
            val hours = section[0].toIntOrNull() ?: return -1
            val minutes = section[1].toIntOrNull() ?: return -1
            val seconds = section[2].toFloatOrNull() ?: return -1
            return (hours * ONE_HOUR + minutes * ONE_MINUTE + seconds * ONE_SECOND).toInt()
        }

        private fun makeTimeText(millisecond: Long): String {
            val second = millisecond / ONE_SECOND % 60
            val minute = millisecond / ONE_MINUTE % 60
            val hour = millisecond / ONE_HOUR
            return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second)
        }
    }
}
