/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.os.Parcel;
import android.os.Parcelable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ContentDirectoryServiceのObjectを表現するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsObject implements Parcelable {
    public static final String CONTAINER = "container";
    public static final String ITEM = "item";
    public static final String ID = "@id";
    public static final String PARENT_ID = "@parentID";
    public static final String RESTRICTED = "@restricted";
    public static final String CHILD_COUNT = "@childCount";
    public static final String SEARCHABLE = "@searchable";
    public static final String DC_TITLE = "dc:title";
    public static final String DC_DATE = "dc:date";
    public static final String DC_CREATOR = "dc:creator";
    public static final String DC_DESCRIPTION = "dc:description";
    public static final String IMAGE_ITEM = "object.item.imageItem";
    public static final String AUDIO_ITEM = "object.item.audioItem";
    public static final String VIDEO_ITEM = "object.item.videoItem";
    public static final String UPNP_CLASS = "upnp:class";
    public static final String UPNP_GENRE = "upnp:genre";
    public static final String UPNP_ACTOR = "upnp:actor";
    public static final String UPNP_ARTIST = "upnp:artist";
    public static final String UPNP_AUTHOR = "upnp:author";
    public static final String UPNP_ALBUM = "upnp:album";
    public static final String UPNP_ALBUM_ART_URI = "upnp:albumArtURI";
    public static final String UPNP_CHANNEL_NAME = "upnp:channelName";
    public static final String UPNP_CHANNEL_NR = "upnp:channelNr";
    public static final String UPNP_SCHEDULED_START_TIME = "upnp:scheduledStartTime";
    public static final String UPNP_SCHEDULED_END_TIME = "upnp:scheduledEndTime";
    public static final String RES = "res";
    public static final String RES_DURATION = "res@duration";
    public static final String DURATION = "duration";
    public static final String RES_RESOLUTION = "res@resolution";
    public static final String RESOLUTION = "resolution";
    public static final String RES_PROTOCOL_INFO = "res@protocolInfo";
    public static final String PROTOCOL_INFO = "protocolInfo";
    public static final String RES_BITRATE = "res@bitrate";
    public static final String BITRATE = "bitrate";
    public static final String ARIB_OBJECT_TYPE = "arib:objectType";
    public static final String ARIB_AUDIO_COMPONENT_TYPE = "arib:audioComponentType";
    public static final String ARIB_AUDIO_COMPONENT_TYPE_QI = "arib:audioComponentType@qualityIndicator";
    public static final String ARIB_CA_PROGRAM_INFO = "arib:caProgramInfo";
    public static final String ARIB_CAPTION_INFO = "arib:captionInfo";
    public static final String ARIB_DATE_PROGRAM_INFO = "arib:dataProgramInfo";
    public static final String ARIB_DATE_PROGRAM_INFO_SYNC = "arib:dataProgramInfo@sync";
    public static final String ARIB_LONG_DESCRIPTION = "arib:longDescription";
    public static final String ARIB_MULTI_ES_INFO = "arib:multiESInfo";
    public static final String ARIB_MULTI_VIEW_INFO = "arib:multiViewInfo";
    public static final String ARIB_VIDEO_COMPONENT_TYPE = "arib:videoComponentType";

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_CONTAINER = 4;

    private final boolean mItem;
    private final Map<String, List<Tag>> mTagMap;
    private String mObjectId;
    private String mParentId;
    private String mTitle;
    private String mUpnpClass;
    private int mType;

    CdsObject(Element element) {
        mTagMap = new LinkedHashMap<>();
        final String tagName = element.getTagName();
        switch (tagName) {
            case ITEM:
                mItem = true;
                break;
            case CONTAINER:
                mItem = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
        setElement(element);
    }

    private void setElement(Element element) {
        putTag("", new Tag(element, true));
        Node node = element.getFirstChild();
        for (; node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final String name = node.getNodeName();
            putTag(name, new Tag((Element) node));
        }
        setCache();
    }

    private void putTag(String name, Tag tag) {
        List<Tag> tags = mTagMap.get(name);
        if (tags == null) {
            tags = new ArrayList<>(1);
            mTagMap.put(name, tags);
        }
        tags.add(tag);
    }

    private void setCache() {
        mObjectId = getValue(ID);
        mParentId = getValue(PARENT_ID);
        mTitle = getValue(DC_TITLE);
        mUpnpClass = getValue(UPNP_CLASS);
        if (!mItem) {
            mType = TYPE_CONTAINER;
        } else if (mUpnpClass.startsWith(IMAGE_ITEM)) {
            mType = TYPE_IMAGE;
        } else if (mUpnpClass.startsWith(AUDIO_ITEM)) {
            mType = TYPE_AUDIO;
        } else if (mUpnpClass.startsWith(VIDEO_ITEM)) {
            mType = TYPE_VIDEO;
        } else {
            mType = TYPE_UNKNOWN;
        }
    }

    public boolean isContainer() {
        return !mItem;
    }

    public boolean isItem() {
        return mItem;
    }

    public int getType() {
        return mType;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public String getParentId() {
        return mParentId;
    }

    public String getUpnpClass() {
        return mUpnpClass;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getValue(String xpath) {
        return getValue(xpath, 0);
    }

    public String getValue(String xpath, int index) {
        final int pos = xpath.indexOf('@');
        if (pos < 0) {
            return getValue(xpath, null, index);
        }
        final String tagName = xpath.substring(0, pos);
        final String attrName = xpath.substring(pos + 1);
        return getValue(tagName, attrName);
    }

    public String getValue(String tagName, String attrName) {
        return getValue(tagName, attrName, 0);
    }

    public String getValue(String tagName, String attrName, int index) {
        final Tag tag = getTag(tagName, index);
        if (tag == null) {
            return null;
        }
        if (attrName == null) {
            return tag.getValue();
        }
        return tag.getAttribute(attrName);
    }

    public Tag getTag(String tagName) {
        return getTag(tagName, 0);
    }

    public Tag getTag(String tagName, int index) {
        final List<Tag> list = getTagList(tagName);
        if (list == null || list.size() <= index) {
            return null;
        }
        return list.get(index);
    }

    public List<Tag> getTagList(String tagName) {
        if (tagName == null) {
            tagName = "";
        }
        return mTagMap.get(tagName);
    }

    public int getIntValue(String xpath, int defaultValue) {
        return parseInt(getValue(xpath), defaultValue);
    }

    public int getIntValue(String xpath, int index, int defaultValue) {
        return parseInt(getValue(xpath, index), defaultValue);
    }

    public Date getDateValue(String xpath) {
        return parseDate(getValue(xpath));
    }

    public Date getDateValue(String xpath, int index) {
        return parseDate(getValue(xpath, index));
    }

    public static int parseInt(String value, int defaultValue) {
        return parseInt(value, 10, defaultValue);
    }

    public static int parseInt(String value, int radix, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value, radix);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    private static final DateFormat FORMAT_D = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
    private static final DateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN);
    private static final DateFormat FORMAT_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.JAPAN);

    public static Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            if (value.length() <= 10) {
                synchronized (FORMAT_D) {
                    return FORMAT_D.parse(value);
                }
            }
            if (value.length() <= 19) {
                synchronized (FORMAT_T) {
                    return FORMAT_T.parse(value);
                }
            } else {
                if (value.lastIndexOf(':') == 22) {
                    value = value.substring(0, 22) + value.substring(23);
                }
                synchronized (FORMAT_Z) {
                    return FORMAT_Z.parse(value);
                }
            }
        } catch (final ParseException e) {
            return null;
        }
    }

    public static String getMimeTypeFromProtocolInfo(String protocolInfo) {
        if (protocolInfo == null) {
            return null;
        }
        final String[] protocols = protocolInfo.split(";");
        if (protocols.length == 0) {
            return null;
        }
        final String[] sections = protocols[0].split(":");
        if (sections.length < 3) {
            return null;
        }
        return sections[2];
    }

    public static String getProtocolFromProtocolInfo(String protocolInfo) {
        if (protocolInfo == null) {
            return null;
        }
        final String[] protocols = protocolInfo.split(";");
        if (protocols.length == 0) {
            return null;
        }
        final String[] sections = protocols[0].split(":");
        if (sections.length < 3) {
            return null;
        }
        return sections[0];
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public String toDumpString() {
        final StringBuilder sb = new StringBuilder();
        for (final Entry<String, List<Tag>> entry : mTagMap.entrySet()) {
            final List<Tag> tags = entry.getValue();
            for (int i = 0; i < tags.size(); i++) {
                final Tag tag = tags.get(i);
                sb.append(entry.getKey());
                if (tags.size() == 1) {
                    sb.append(" => ");
                } else {
                    sb.append("[");
                    sb.append(String.valueOf(i));
                    sb.append("] => ");
                }
                sb.append(tag.getValue());
                sb.append("\n");
                final Map<String, String> attrs = tag.getAttributes();
                for (final Entry<String, String> e : attrs.entrySet()) {
                    sb.append("      @");
                    sb.append(e.getKey());
                    sb.append(" => ");
                    sb.append(e.getValue());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return mTagMap.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CdsObject)) {
            return false;
        }
        final CdsObject obj = (CdsObject) o;
        return mTagMap.equals(obj.mTagMap);
    }

    protected CdsObject(Parcel in) {
        mItem = in.readByte() != 0;
        final int size = in.readInt();
        mTagMap = new LinkedHashMap<>(size);
        final ClassLoader classLoader = Tag.class.getClassLoader();
        for (int i = 0; i < size; i++) {
            final String name = in.readString();
            final int length = in.readInt();
            final List<Tag> list = new ArrayList<>(length);
            for (int j = 0; j < length; j++) {
                final Tag tag = in.readParcelable(classLoader);
                list.add(tag);
            }
            mTagMap.put(name, list);
        }
        setCache();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mItem ? 1 : 0));
        dest.writeInt(mTagMap.size());
        for (final Entry<String, List<Tag>> entry : mTagMap.entrySet()) {
            dest.writeString(entry.getKey());
            final List<Tag> list = entry.getValue();
            dest.writeInt(list.size());
            for (final Tag tag : list) {
                dest.writeParcelable(tag, flags);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CdsObject> CREATOR = new Creator<CdsObject>() {
        @Override
        public CdsObject createFromParcel(Parcel in) {
            return new CdsObject(in);
        }

        @Override
        public CdsObject[] newArray(int size) {
            return new CdsObject[size];
        }
    };

}
