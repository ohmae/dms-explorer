/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.android.upnp.DeviceWrapper;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.CdsObjectXmlConverter;
import net.mm2d.upnp.Action;
import net.mm2d.upnp.Argument;
import net.mm2d.upnp.Device;
import net.mm2d.upnp.Service;
import net.mm2d.upnp.StateVariable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * MediaRendererを表現するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaRenderer extends DeviceWrapper {
    private static final String SET_AV_TRANSPORT_URI = "SetAVTransportURI";
    private static final String GET_MEDIA_INFO = "GetMediaInfo";
    private static final String GET_TRANSPORT_INFO = "GetTransportInfo";
    private static final String GET_POSITION_INFO = "GetPositionInfo";
    private static final String GET_DEVICE_CAPABILITIES = "GetDeviceCapabilities";
    private static final String GET_TRANSPORT_SETTINGS = "GetTransportSettings";
    private static final String PLAY = "Play";
    private static final String PAUSE = "Pause";
    private static final String STOP = "Stop";
    private static final String SEEK = "Seek";
    private static final String NEXT = "Next";
    private static final String PREVIOUS = "Previous";
    private static final String GET_CURRENT_TRANSPORT_ACTIONS = "GetCurrentTransportActions";

    private static final String INSTANCE_ID = "InstanceID";
    private static final String CURRENT_URI = "CurrentURI";
    private static final String CURRENT_URI_META_DATA = "CurrentURIMetaData";
    private static final String NR_TRACKS = "NrTracks";
    private static final String MEDIA_DURATION = "MediaDuration";
    private static final String NEXT_URI = "NextURI";
    private static final String NEXT_URI_META_DATA = "NextURIMetaData";
    private static final String PLAY_MEDIUM = "PlayMedium";
    private static final String RECORD_MEDIUM = "RecordMedium";
    private static final String WRITE_STATUS = "WriteStatus";
    private static final String CURRENT_TRANSPORT_STATE = "CurrentTransportState";
    private static final String CURRENT_TRANSPORT_STATUS = "CurrentTransportStatus";
    private static final String CURRENT_SPEED = "CurrentSpeed";
    private static final String TRACK = "Track";
    private static final String TRACK_DURATION = "TrackDuration";
    private static final String TRACK_META_DATA = "TrackMetaData";
    private static final String TRACK_URI = "TrackURI";
    private static final String REL_TIME = "RelTime";
    private static final String ABS_TIME = "AbsTime";
    private static final String REL_COUNT = "RelCount";
    private static final String ABS_COUNT = "AbsCount";
    private static final String PLAY_MEDIA = "PlayMedia";
    private static final String REC_MEDIA = "RecMedia";
    private static final String REC_QUALITY_MODES = "RecQualityModes";
    private static final String PLAY_MODE = "PlayMode";
    private static final String REC_QUALITY_MODE = "RecQualityMode";
    private static final String SPEED = "Speed";
    private static final String UNIT = "Unit";
    private static final String TARGET = "Target";
    private static final String ACTIONS = "Actions";

    private static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    private static final String UNIT_REL_TIME = "REL_TIME";
    private static final String UNIT_ABS_TIME = "ABS_TIME";

    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);

    @NonNull
    private final MrControlPoint mMrControlPoint;
    @NonNull
    private final Service mAvTransport;
    @NonNull
    private final Action mSetAvTransportUri;
    @NonNull
    private final Action mGetPositionInfo;
    @NonNull
    private final Action mGetTransportInfo;
    @NonNull
    private final Action mPlay;
    @NonNull
    private final Action mStop;
    @NonNull
    private final Action mSeek;
    @Nullable
    private final Action mPause;

    MediaRenderer(
            @NonNull final MrControlPoint cp,
            @NonNull final Device device) {
        super(device);
        mMrControlPoint = cp;
        if (!device.getDeviceType().startsWith(Avt.MR_DEVICE_TYPE)) {
            throw new IllegalArgumentException("device is not MediaRenderer");
        }
        mAvTransport = findService(device, Avt.AVT_SERVICE_ID);
        mSetAvTransportUri = findAction(mAvTransport, SET_AV_TRANSPORT_URI);
        mGetPositionInfo = findAction(mAvTransport, GET_POSITION_INFO);
        mGetTransportInfo = findAction(mAvTransport, GET_TRANSPORT_INFO);
        mPlay = findAction(mAvTransport, PLAY);
        mStop = findAction(mAvTransport, STOP);
        mSeek = findAction(mAvTransport, SEEK);
        mPause = mAvTransport.findAction(PAUSE);
    }

    @NonNull
    private static Service findService(
            @NonNull final Device device,
            @NonNull final String id) {
        final Service service = device.findServiceById(id);
        if (service == null) {
            throw new IllegalArgumentException("Device doesn't have " + id);
        }
        return service;
    }

    @NonNull
    private static Action findAction(
            @NonNull final Service service,
            @NonNull final String name) {
        final Action action = service.findAction(name);
        if (action == null) {
            throw new IllegalArgumentException("Service doesn't have " + name);
        }
        return action;
    }

    public boolean isSupportPause() {
        return mPause != null;
    }


    /**
     * AVTransportサービスを購読する。
     */
    public void subscribe() {
        Completable.create(emitter -> mAvTransport.subscribe(true))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * AVTransportサービスの購読を中止する。
     */
    public void unsubscribe() {
        Completable.create(emitter -> mAvTransport.unsubscribe())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @NonNull
    public Single<Map<String, String>> setAVTransportURI(
            @NonNull final CdsObject object,
            @NonNull final String uri) {
        final String metadata = CdsObjectXmlConverter.convert(object);
        if (TextUtils.isEmpty(metadata)) {
            return Single.error(new IllegalStateException("empty meta data"));
        }
        final Map<String, String> argument = new HashMap<>();
        argument.put(INSTANCE_ID, "0");
        argument.put(CURRENT_URI, uri);
        argument.put(CURRENT_URI_META_DATA, metadata);
        return invoke(mSetAvTransportUri, argument);
    }

    @NonNull
    public Single<Map<String, String>> clearAVTransportURI() {
        final Map<String, String> argument = new HashMap<>();
        argument.put(INSTANCE_ID, "0");
        argument.put(CURRENT_URI, null);
        argument.put(CURRENT_URI_META_DATA, null);
        return invoke(mSetAvTransportUri, argument);
    }

    @NonNull
    public Single<Map<String, String>> play() {
        final Map<String, String> argument = new HashMap<>();
        argument.put(INSTANCE_ID, "0");
        argument.put(SPEED, "1");
        return invoke(mPlay, argument);
    }

    @NonNull
    public Single<Map<String, String>> stop() {
        return invoke(mStop, Collections.singletonMap(INSTANCE_ID, "0"));
    }

    @NonNull
    public Single<Map<String, String>> pause() {
        if (mPause == null) {
            return Single.error(new IllegalStateException("pause is not supported"));
        }
        return invoke(mPause, Collections.singletonMap(INSTANCE_ID, "0"));
    }

    @NonNull
    public Single<Map<String, String>> seek(final long time) {
        final Argument unitArg = mSeek.findArgument(UNIT);
        if (unitArg == null) {
            return Single.error(new IllegalStateException("no unit argument"));
        }
        final Map<String, String> argument = new HashMap<>();
        argument.put(INSTANCE_ID, "0");
        final String timeText = makeTimeText(time);
        final StateVariable unit = unitArg.getRelatedStateVariable();
        final List<String> list = unit.getAllowedValueList();
        if (list.contains(UNIT_REL_TIME)) {
            argument.put(UNIT, UNIT_REL_TIME);
        } else if (list.contains(UNIT_ABS_TIME)) {
            argument.put(UNIT, UNIT_ABS_TIME);
        } else {
            return Single.error(new IllegalStateException("no supported unit"));
        }
        argument.put(TARGET, timeText);
        return invoke(mSeek, argument);
    }

    @NonNull
    public Single<Map<String, String>> getPositionInfo() {
        return invoke(mGetPositionInfo, Collections.singletonMap(INSTANCE_ID, "0"));
    }

    @NonNull
    public Single<Map<String, String>> getTransportInfo() {
        return invoke(mGetTransportInfo, Collections.singletonMap(INSTANCE_ID, "0"));
    }

    @NonNull
    private Single<Map<String, String>> invoke(
            @NonNull final Action action,
            @NonNull final Map<String, String> argument) {
        return Single.create((SingleOnSubscribe<Map<String, String>>) emitter -> {
            try {
                final Map<String, String> result = action.invoke(argument);
                emitter.onSuccess(result);
            } catch (final IOException ignored) {
                emitter.onError(ignored);
            }
        }).subscribeOn(Schedulers.io());
    }

    @NonNull
    public static TransportState getCurrentTransportState(@NonNull final Map<String, String> result) {
        return TransportState.of(result.get(CURRENT_TRANSPORT_STATE));
    }

    public static int getDuration(@NonNull final Map<String, String> result) {
        return parseCount(result.get(TRACK_DURATION));
    }

    public static int getProgress(@NonNull final Map<String, String> result) {
        final int progress = parseCount(result.get(REL_TIME));
        if (progress >= 0) {
            return progress;
        }
        return parseCount(result.get(ABS_TIME));
    }

    /**
     * 00:00:00.000形式の時間をミリ秒に変換する。小数点以下がない場合も想定する。
     *
     * @param count 変換する文字列
     * @return 変換したミリ秒時間
     */
    public static int parseCount(@Nullable final String count) {
        if (TextUtils.isEmpty(count) || count.equals(NOT_IMPLEMENTED)) {
            return -1;
        }
        try {
            final int point = count.indexOf('.');
            final int milliseconds = (point > 0 && count.length() - point == 4) ?
                    Integer.parseInt(count.substring(point + 1)) : 0;
            final String[] section = (point > 0 ? count.substring(0, point) : count).split(":");
            if (section.length != 3) {
                return -1;
            }
            return (int) (milliseconds
                    + TimeUnit.HOURS.toMillis(Integer.parseInt(section[0]))
                    + TimeUnit.MINUTES.toMillis(Integer.parseInt(section[1]))
                    + TimeUnit.SECONDS.toMillis(Integer.parseInt(section[2])));
        } catch (final NumberFormatException ignored) {
        }
        return -1;
    }

    private static String makeTimeText(final long millisecond) {
        final long second = (millisecond / ONE_SECOND) % 60;
        final long minute = (millisecond / ONE_MINUTE) % 60;
        final long hour = millisecond / ONE_HOUR;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second);
    }
}
