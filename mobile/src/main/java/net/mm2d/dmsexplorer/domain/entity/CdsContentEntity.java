package net.mm2d.dmsexplorer.domain.entity;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter;
import net.mm2d.dmsexplorer.util.StringJoiner;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsContentEntity implements ContentEntity {
    @NonNull
    private final CdsObject mObject;
    @NonNull
    private final ContentType mType;
    private Tag mSelectedRes;
    private Uri mUri;
    private String mMimeType;

    public CdsContentEntity(@NonNull final CdsObject object) {
        mObject = object;
        mType = getType(object);
        mSelectedRes = object.getTag(CdsObject.RES);
        updateUri();
    }

    private static ContentType getType(@NonNull final CdsObject object) {
        switch (object.getType()) {
            case CdsObject.TYPE_VIDEO:
                return ContentType.MOVIE;
            case CdsObject.TYPE_AUDIO:
                return ContentType.MUSIC;
            case CdsObject.TYPE_IMAGE:
                return ContentType.PHOTO;
            case CdsObject.TYPE_CONTAINER:
                return ContentType.CONTAINER;
            case CdsObject.TYPE_UNKNOWN:
                return ContentType.UNKNOWN;
        }
        return ContentType.UNKNOWN;
    }

    private void updateUri() {
        if (mSelectedRes == null || TextUtils.isEmpty(mSelectedRes.getValue())) {
            mUri = null;
            mMimeType = null;
            return;
        }
        mUri = Uri.parse(mSelectedRes.getValue());
        final String protocolInfo = mSelectedRes.getAttribute(CdsObject.PROTOCOL_INFO);
        mMimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
    }

    @NonNull
    @Override
    public CdsObject getObject() {
        return mObject;
    }

    @NonNull
    @Override
    public String getName() {
        return mObject.getTitle();
    }

    @NonNull
    @Override
    public String getDescription() {
        final StringJoiner stringJoiner = new StringJoiner();
        switch (mType) {
            case MOVIE:
                stringJoiner.join(CdsFormatter.makeChannel(mObject));
                stringJoiner.join(CdsFormatter.makeScheduleOrDate(mObject));
                break;
            case MUSIC:
                stringJoiner.join(CdsFormatter.makeArtistsSimple(mObject));
                stringJoiner.join(CdsFormatter.makeAlbum(mObject), " / ");
                break;
            case PHOTO:
                stringJoiner.join(CdsFormatter.makeDate(mObject));
                break;
            case CONTAINER:
                stringJoiner.join(CdsFormatter.makeChannel(mObject));
                stringJoiner.join(CdsFormatter.makeDate(mObject), ' ');
                break;
            default:
                break;
        }
        if (stringJoiner.length() != 0) {
            return stringJoiner.toString();
        }
        return "";
    }

    @NonNull
    @Override
    public Uri getArtUri() {
        final String uri = mObject.getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (TextUtils.isEmpty(uri)) {
            return Uri.EMPTY;
        }
        return Uri.parse(uri);
    }

    @NonNull
    @Override
    public Uri getIconUri() {
        return Uri.EMPTY;
    }

    @NonNull
    @Override
    public ContentType getType() {
        return mType;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return mUri;
    }

    @Nullable
    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public int getResourceCount() {
        return mObject.getResourceCount();
    }

    @Override
    public boolean hasResource() {
        return getResourceCount() != 0;
    }

    @Override
    public boolean isProtected() {
        return mObject.hasProtectedResource();
    }

    @Override
    public boolean canDelete() {
        return mObject.getIntValue(CdsObject.RESTRICTED, -1) == 0;
    }

    @Override
    public void selectResource(final int index) {
        if (index < 0 || index >= getResourceCount()) {
            throw new IndexOutOfBoundsException();
        }
        mSelectedRes = mObject.getTag(CdsObject.RES, index);
        updateUri();
    }
}
