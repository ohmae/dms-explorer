/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
    // XML関係の定義
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

    // オブジェクト種別の定義
    /**
     * 未定義
     */
    public static final int TYPE_UNKNOWN = 0;
    /**
     * 動画
     */
    public static final int TYPE_VIDEO = 1;
    /**
     * 音楽
     */
    public static final int TYPE_AUDIO = 2;
    /**
     * 画像
     */
    public static final int TYPE_IMAGE = 3;
    /**
     * コンテナ
     */
    public static final int TYPE_CONTAINER = 4;

    /**
     * このオブジェクトがitemか否か、itemのときtrue
     */
    private final boolean mItem;
    /**
     * XMLのタグ情報。
     *
     * タグ名をKeyとして、TagのListを保持する。
     * 同一のタグが複数ある場合はListに出現順に格納する。
     */
    private final Map<String, List<Tag>> mTagMap;
    /**
     * \@idの値。
     */
    private String mObjectId;
    /**
     * \@parentIDの値。
     */
    private String mParentId;
    /**
     * dc:titleの値
     */
    private String mTitle;
    /**
     * upnp:classの値
     */
    private String mUpnpClass;
    /**
     * upnp:classのint値表現。
     *
     * @see #TYPE_UNKNOWN
     * @see #TYPE_VIDEO
     * @see #TYPE_AUDIO
     * @see #TYPE_IMAGE
     * @see #TYPE_CONTAINER
     */
    private int mType;

    /**
     * elementをもとにインスタンス作成
     *
     * @param element objectを示すelement
     */
    CdsObject(@NonNull Element element) {
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
        parseElement(element);
    }

    /**
     * 子要素の情報をパースし、格納する。
     *
     * @param element objectを示すelement
     */
    private void parseElement(@NonNull Element element) {
        putTag("", new Tag(element, true));
        Node node = element.getFirstChild();
        for (; node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final String name = node.getNodeName();
            putTag(name, new Tag((Element) node));
        }
        prepareCache();
    }

    /**
     * タグ情報を格納する。
     *
     * @param name タグ名
     * @param tag  格納するタグ情報
     */
    private void putTag(@NonNull String name, @NonNull Tag tag) {
        List<Tag> tags = mTagMap.get(name);
        if (tags == null) {
            tags = new ArrayList<>(1);
            mTagMap.put(name, tags);
        }
        tags.add(tag);
    }

    /**
     * パース結果からパラメータの初期化を行う。
     */
    private void prepareCache() {
        mObjectId = getValue(ID);
        mParentId = getValue(PARENT_ID);
        mTitle = getValue(DC_TITLE);
        mUpnpClass = getValue(UPNP_CLASS);
        if (mObjectId == null || mParentId == null || mTitle == null || mUpnpClass == null) {
            throw new IllegalArgumentException("Malformed item");
        }
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

    /**
     * コンテナであるか否かを返す。
     *
     * @return trueのときコンテナ。
     */
    public boolean isContainer() {
        return !mItem;
    }

    /**
     * アイテムであるか否かを返す。
     *
     * @return trueのときアイテム。
     */
    public boolean isItem() {
        return mItem;
    }

    /**
     * Typeの値を返す。
     *
     * @return Type値
     */
    public int getType() {
        return mType;
    }

    /**
     * \@idの値を返す。
     *
     * @return \@idの値
     */
    @NonNull
    public String getObjectId() {
        return mObjectId;
    }

    /**
     * \@parentIDの値を返す。
     *
     * @return \@parentIDの値
     */
    @NonNull
    public String getParentId() {
        return mParentId;
    }

    /**
     * upnp:classの値を返す。
     *
     * @return upnp:classの値
     */
    @NonNull
    public String getUpnpClass() {
        return mUpnpClass;
    }

    /**
     * dc:titleの値を返す。
     *
     * @return dc:titleの値
     */
    @NonNull
    public String getTitle() {
        return mTitle;
    }

    /**
     * XPATH風の指定で示された値を返す。
     *
     * XPATHはitemもしくはcontainerをルートとして指定する。
     * 名前空間はシンボルをそのまま記述する。
     * 例えば'item@id'及び'container@id'はともに'@id'を指定する。
     * 'item/dc:title'であれば'dc:title'を指定する。
     *
     * 複数同一のタグがあった場合は最初に現れた要素の値を返す。
     * {@link #getValue(String, int)}で第二引数に0を指定するのと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @return 指定された値。見つからない場合はnull
     * @see #getValue(String, int)
     */
    @Nullable
    public String getValue(@NonNull String xpath) {
        return getValue(xpath, 0);
    }

    /**
     * XPATH風の指定で示された値を返す。
     *
     * XPATHはitemもしくはcontainerをルートとして指定する。
     * 名前空間はシンボルをそのまま記述する。
     * 例えば'item@id'及び'container@id'はともに'@id'を指定する。
     * 'item/dc:title'であれば'dc:title'を指定する。
     *
     * 複数同一のタグがあった場合は現れた順にインデックスが付けられる。
     * 属性値はタグと同じインデックスに格納される。
     * 例えば、aというタグが複数あるが、
     * 一つ目に現れたタグにはbという属性はなく、
     * 二つ目に現れたタグにbという属性がある状態で、
     * 'a@b'を指定したとすると、インデックス0の値はnullとなる。
     * 二つ目に現れた属性bの値を取り出すには、インデックス1の値を取り出す必要がある。
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @param index インデックス値
     * @return 指定された値。見つからない場合はnull
     */
    @Nullable
    public String getValue(@NonNull String xpath, int index) {
        final int pos = xpath.indexOf('@');
        if (pos < 0) {
            return getValue(xpath, null, index);
        }
        final String tagName = xpath.substring(0, pos);
        final String attrName = xpath.substring(pos + 1);
        return getValue(tagName, attrName, index);
    }

    /**
     * タグ名と属性名を指定して値を取り出す。
     *
     * 複数の同一タグがある場合は最初に現れたタグの情報を返す。
     * {@link #getValue(String, String, int)}の第三引数に0を指定したものと等価。
     *
     * @param tagName  タグ名
     * @param attrName 属性名、タグの値を取得するときはnullを指定する。
     * @return 指定された値。見つからない場合はnull
     * @see #getValue(String, String, int)
     */
    @Nullable
    public String getValue(@Nullable String tagName, @Nullable String attrName) {
        return getValue(tagName, attrName, 0);
    }

    /**
     * タグ名と属性名を指定して値を取り出す。
     *
     * 複数同一のタグがあった場合は現れた順にインデックスが付けられる。
     * 属性値はタグと同じインデックスに格納される。
     *
     * @param tagName  タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param attrName 属性名、タグの値を取得するときはnullを指定する。
     * @param index    インデックス値
     * @return 指定された値。見つからない場合はnull
     */
    @Nullable
    public String getValue(@Nullable String tagName, @Nullable String attrName, int index) {
        final Tag tag = getTag(tagName, index);
        if (tag == null) {
            return null;
        }
        if (TextUtils.isEmpty(attrName)) {
            return tag.getValue();
        }
        return tag.getAttribute(attrName);
    }

    /**
     * 指定したタグ名のTagインスタンスを返す。
     *
     * 複数同一タグが存在した場合は最初に現れたタグ。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンス、見つからない場合はnull
     */
    @Nullable
    public Tag getTag(@Nullable String tagName) {
        return getTag(tagName, 0);
    }

    /**
     * 指定したタグ名、インデックスのTagインスタンスを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param index   インデックス値
     * @return Tagインスタンス、見つからない場合はnull
     */
    @Nullable
    public Tag getTag(@Nullable String tagName, int index) {
        final List<Tag> list = getTagList(tagName);
        if (list == null || list.size() <= index) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 指定したタグ名のTagインスタンスリストを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンスリスト
     */
    @Nullable
    public List<Tag> getTagList(@Nullable String tagName) {
        if (tagName == null) {
            tagName = "";
        }
        return mTagMap.get(tagName);
    }

    /**
     * XPATH風の指定で示された値をInt値として返す。
     *
     * {@link #getValue(String)} の結果を {@link #parseIntSafely(String, int)} に渡すことと等価
     *
     * @param xpath        パラメータの位置を表現するXPATH風の指定
     * @param defaultValue 値が見つからない場合、Int値にパースできない値だった場合のデフォルト値
     * @return 指定された値
     * @see #getValue(String)
     */
    public int getIntValue(@NonNull String xpath, int defaultValue) {
        return parseIntSafely(getValue(xpath), defaultValue);
    }

    /**
     * XPATH風の指定で示された値をInt値として返す。
     *
     * {@link #getValue(String, int)} の結果を {@link #parseIntSafely(String, int)} に渡すことと等価
     *
     * @param xpath        パラメータの位置を表現するXPATH風の指定
     * @param index        インデックス値
     * @param defaultValue 値が見つからない場合、Int値にパースできない値だった場合のデフォルト値
     * @return 指定された値
     * @see #getValue(String, int)
     */
    public int getIntValue(@NonNull String xpath, int index, int defaultValue) {
        return parseIntSafely(getValue(xpath, index), defaultValue);
    }

    /**
     * XPATH風の指定で示された値をDateとして返す。
     *
     * {@link #getValue(String)} の結果を {@link #parseDate(String)} に渡すことと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @return 指定された値。値が見つからない場合、パースできない値の場合null
     * @see #getValue(String, int)
     */
    @Nullable
    public Date getDateValue(@NonNull String xpath) {
        return parseDate(getValue(xpath));
    }

    /**
     * XPATH風の指定で示された値をDateとして返す。
     *
     * {@link #getValue(String, int)} の結果を {@link #parseDate(String)} に渡すことと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @param index インデックス値
     * @return 指定された値。値が見つからない場合、パースできない値の場合null
     * @see #getValue(String, int)
     */
    @Nullable
    public Date getDateValue(@NonNull String xpath, int index) {
        return parseDate(getValue(xpath, index));
    }

    /**
     * 与えられた文字列を10進数としてパースする。
     *
     * @param value        パースする文字列
     * @param defaultValue パースできない場合のデフォルト値
     * @return パース結果
     */
    public static int parseIntSafely(@Nullable String value, int defaultValue) {
        return parseIntSafely(value, 10, defaultValue);
    }

    /**
     * 与えられた文字列をradix進数としてパースする。
     *
     * @param value        パースする文字列
     * @param radix        パースする文字列の基数
     * @param defaultValue パースできない場合のデフォルト値
     * @return パース結果
     */
    public static int parseIntSafely(@Nullable String value, int radix, int defaultValue) {
        if (TextUtils.isEmpty(value)) {
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

    /**
     * 与えられた文字列をパースしてDateとして戻す。
     *
     * CDSで使用される日付フォーマットにはいくつかバリエーションがあるが、
     * 該当するフォーマットでパースを行う。
     *
     * @param value パースする文字列
     * @return パース結果、パースできない場合null
     */
    @Nullable
    public static Date parseDate(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
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

    /**
     * protocolInfoの文字列からMimeTypeの文字列を抽出する。
     *
     * @param protocolInfo protocolInfo
     * @return MimeTypeの文字列。抽出に失敗した場合null
     */
    @Nullable
    public static String extractMimeTypeFromProtocolInfo(@Nullable String protocolInfo) {
        if (TextUtils.isEmpty(protocolInfo)) {
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

    /**
     * protocolInfoの文字列からProtocolの文字列を抽出する。
     *
     * @param protocolInfo protocolInfo
     * @return Protocolの文字列。抽出に失敗した場合null
     */
    @Nullable
    public static String extractProtocolFromProtocolInfo(@Nullable String protocolInfo) {
        if (TextUtils.isEmpty(protocolInfo)) {
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
    @NonNull
    public String toString() {
        return getTitle();
    }

    /**
     * 全情報をダンプした文字列を返す。
     *
     * @return ダンプ文字列
     */
    @NonNull
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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CdsObject)) {
            return false;
        }
        final CdsObject obj = (CdsObject) o;
        return mTagMap.equals(obj.mTagMap);
    }

    /**
     * Parcelable用のコンストラクタ。
     *
     * @param in Parcel
     */
    protected CdsObject(@NonNull Parcel in) {
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
        prepareCache();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
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

    /**
     * Parcelableのためのフィールド
     */
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
