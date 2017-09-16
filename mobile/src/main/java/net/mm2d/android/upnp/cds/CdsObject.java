/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ContentDirectoryServiceのObjectを表現するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsObject implements Parcelable {
    // XML関係の定義

    public static final String DIDL_LITE = "DIDL-Lite";
    /**
     * "item".
     *
     * <p>item is a first-level class derived directly from object.
     * An item most often represents a single piece of AV data, such as a CD track, a movie or an audio file.
     * Items may be playable, meaning they have information that can be played on a rendering device.
     * Any object which derives from the item class is expressed via the DIDL-Lite item structure.
     */
    public static final String ITEM = "item";
    /**
     * "container".
     *
     * <p>container is a first-level class derived directly from object.
     * A container represents a collection of objects.
     * Containers can represent the physical organization of objects (storage containers) or logical collections.
     * Logical collections can have formal definitions of their contents or they can be arbitrary collections.
     * Containers can be either homogeneous, containing objects that are all of the same class,
     * or heterogeneous, containing objects of mixed class.
     * Containers can contain other containers.
     * Any object derived from the container class is expressed via the DIDL-Lite container structure.
     * A CDS is required to maintain a ContainerUpdateID for each of its containers.
     * This value is maintained internally, does not appear in any XML expression of the container,
     * and cannot be used in a search or sort criterion.
     */
    public static final String CONTAINER = "container";
    /**
     * "@id".
     *
     * <p>An identifier for the object.
     * The value of each object id property must be unique with respect to the Content Directory.
     *
     * <p>Namespace: DIDL-Lite / Type: string
     */
    public static final String ID = "@id";
    /**
     * "@parentID".
     *
     * <p>id property of object's parent.
     * The parentID of the Content Directory 'root' container must be set to the reserved value of "-1".
     * No other parentID attribute of any other Content Directory object may take this value.
     *
     * <p>Namespace: DIDL-Lite / Type: string
     */
    public static final String PARENT_ID = "@parentID";
    /**
     * "@restricted".
     *
     * <p>When true, ability to modify a given object is confined to the Content Directory Service.
     * Control point metadata write access is disabled.
     *
     * <p>Namespace: DIDL-Lite / Type: boolean
     */
    public static final String RESTRICTED = "@restricted";
    /**
     * "@childCount".
     *
     * <p>Child count for the object.
     * Applies to containers only.
     *
     * <p>Namespace: DIDL-Lite / Type: integer
     */
    public static final String CHILD_COUNT = "@childCount";
    /**
     * "@searchable".
     *
     * <p>When true, the ability to perform a Search() action under a container is enabled,
     * otherwise a Search() under that container will return no results.
     * The default value of this attribute when it is absent on a container is false
     *
     * <p>Namespace: DIDL-Lite / Type: boolean
     */
    public static final String SEARCHABLE = "@searchable";
    /**
     * "dc:title".
     *
     * <p>Name of the object.
     *
     * <p><b>ARIB:</b>
     * イベント名を表す。原則として、EIT のイベントループに含まれる短形式イベント記述子の、第1 ループに含まれる
     * event_name_char の情報を挿入する。その場合、挿入する情報はEIT の情報の変更に合わせて更新することが望ましい。
     * event_name_char が不明な場合、DLNA ガイドラインの規定に従い、upnp:channelName と同一の文字列を挿入すること。
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_TITLE = "dc:title";
    /**
     * "dc:date".
     *
     * <p>ISO 8601, of the form "YYYY-MM-DD".
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_DATE = "dc:date";
    /**
     * "dc:creator".
     *
     * <p>Primary content creator or owner of the object.
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_CREATOR = "dc:creator";
    /**
     * "dc:description".
     *
     * <p>Description may include but is not limited to: an abstract, a table of contents,
     * a graphical representation, or a free-text account of the resource.
     *
     * <p><b>ARIB:</b>
     * 番組記述を表す。
     * 原則として、EITのイベントループに含まれる短形式イベント記述子の、第2ループに含まれる text_char を挿入する。
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_DESCRIPTION = "dc:description";
    /**
     * "object.item.imageItem".
     *
     * <p>upnp:class value.
     * represents a piece of content that, when rendered, generates some still image.
     */
    public static final String IMAGE_ITEM = "object.item.imageItem";
    /**
     * "object.item.audioItem".
     *
     * <p>upnp:class value.
     * represents a piece of content that, when rendered, generates some audio.
     */
    public static final String AUDIO_ITEM = "object.item.audioItem";
    /**
     * "object.item.videoItem".
     *
     * <p>upnp:class value.
     * represents a piece of content that, when rendered, generates some video.
     */
    public static final String VIDEO_ITEM = "object.item.videoItem";
    /**
     * "upnp:class".
     *
     * <p>Class of the object.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_CLASS = "upnp:class";
    /**
     * "upnp:genre".
     *
     * <p>Name of the genre to which an object belongs.
     *
     * <p><b>ARIB:</b>
     * dc:title で表されるイベントが属するジャンルを表す。
     * 原則として、EIT のイベントループに含まれるコンテント記述子に含まれる
     * content_nibble_level_1 （大分類）の値から
     * 「第四編第4 部 付録[付録A]」のジャンル大分類の表の「記述内容」に記載されている記述（文字列）に変換して挿入する。
     * upnp:genre は複数持つことができる。但し content_nibble_level_1 が 0xC, 0xD, 0xE の場合は、
     * "未定義"という文字列を挿入すること。また content_nibble_level_1 が不明な場合、"不明"という文字列を挿入すること。
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_GENRE = "upnp:genre";
    /**
     * "upnp:actor".
     *
     * <p>Name of an actor appearing in a video item.
     *
     * <p>Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
     */
    public static final String UPNP_ACTOR = "upnp:actor";
    /**
     * "upnp:actor@role".
     *
     * <p>Role of the actor in the work.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_ACTOR_ROLE = "upnp:actor@role";
    /**
     * "upnp:artist".
     *
     * <p>Name of an artist.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_ARTIST = "upnp:artist";
    /**
     * "upnp:artist@role".
     *
     * <p>Role of the artist in the work.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_ARTIST_ROLE = "upnp:artist@role";
    /**
     * "upnp:author".
     *
     * <p>Name of an author of a text item.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_AUTHOR = "upnp:author";
    /**
     * "upnp:author@role".
     *
     * <p>Role of the author in the work.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_AUTHOR_ROLE = "upnp:author@role";
    /**
     * "upnp:producer".
     *
     * <p>Name of producer of e.g., a movie or CD
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_PRODUCER = "upnp:producer";
    /**
     * "upnp:director".
     *
     * <p>Name of the director of the video content item (e.g., the movie).
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_DIRECTOR = "upnp:director";
    /**
     * "dc:publisher".
     *
     * <p>An entity responsible for making the resource available.
     * Examples of a Publisher include a person, an organization, or a service.
     * Typically, the name of a Publisher should be used to indicate the entity.
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_PUBLISHER = "dc:publisher";
    /**
     * "dc:contributor".
     *
     * <p>An entity responsible for making contributions to the resource.
     * Examples of a Contributor include a person, an organization, or a service.
     * Typically, the name of a Contributor should be used to indicate the entity.
     *
     * <p>Namespace: Dublin Core / Type: string
     */
    public static final String DC_CONTRIBUTOR = "dc:contributor";

    /**
     * "upnp:album".
     *
     * <p>Title of the album to which the item belongs.
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_ALBUM = "upnp:album";
    /**
     * "upnp:albumArtURI".
     *
     * <p>Reference to album art.
     * Values must be properly escaped URIs as described in [RFC 2396].
     *
     * <p>Namespace: UPnP / Type: URI
     */
    public static final String UPNP_ALBUM_ART_URI = "upnp:albumArtURI";
    /**
     * "upnp:channelName".
     *
     * <p>Used for identification of channels themselves,
     * or information associated with a piece of recorded content.
     *
     * <p><b>ARIB:</b>
     * サービス（編成チャンネル）名を表す。
     * 原則として、SDT のサービスループに含まれるサービス記述子の、第2 ループに含まれる char の情報を挿入する。
     * char が不明な場合、以前設定されていた編成チャンネル名を挿入することが望ましい。
     *
     * <p>Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
     */
    public static final String UPNP_CHANNEL_NAME = "upnp:channelName";
    /**
     * "upnp:channelNr".
     *
     * <p>Used for identification of tuner channels themselves,
     * or information associated with a piece of recorded content.
     *
     * <p><b>ARIB:</b>
     * 以下の式で示されるチャンネル番号を表す。<br>
     * upnp:channelNr = ワンタッチ選局番号×10000＋3桁番号×10＋枝番[ワンタッチ選局番号]<br>
     * 受信機でサービス（編成チャンネル）に対して設定されている値を利用する。
     * サービスにワンタッチ選局番号が割り振られていない場合には0とする。
     * [3桁番号]原則としてNITの第2ループ（TSループ）に含まれるサービスリスト記述子の
     * service_id と、同じTS 情報記述子の remote_control_key_id から、
     * 「第7編9.1.3(d)」で規定される計算式によって生成される値を利用する。
     * NIT が取得できない場合、以前設定されていた３桁番号を挿入することが望ましい。
     *
     * <p>Namespace: UPnP / Type: integer
     */
    public static final String UPNP_CHANNEL_NR = "upnp:channelNr";
    /**
     * "upnp:scheduledStartTime".
     *
     * <p>ISO 8601, of the form "yyyy-mm-ddThh:mm:ss".
     * Used to indicate the start time of a schedule program, indented for use by tuners.
     *
     * <p><b>ARIB:</b>
     * 対象イベントの開始時刻を表す。
     * 原則として、EITのイベントループ内にある対象イベントの開始時刻を表す
     * start_time を MJD+BCD 表記からDLNAガイドラインで規定されている dc:date と同じ以下の書式に変換して挿入する。
     * TOTが無い場合は以下のように記載する。<br>
     * CCYY-MM-DDTHH:MM:SS<br>
     * サマータイムの導入によりTOTにサマータイム実施時の時間オフセット値が設定されている場合には、
     * 以下のようにTimeOffset(±HH:MM)を付加する記載とする。<br>
     * CCYY-MM-DDTHH:MM:SS+09:00 → サマータイムなし<br>
     * CCYY-MM-DDTHH:MM:SS+10:00 → サマータイムあり（１時間）<br>
     * start_timeが不定な値の場合にはこのpropertyを配置しない。
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_SCHEDULED_START_TIME = "upnp:scheduledStartTime";
    /**
     * "upnp:scheduledEndTime".
     *
     * <p>ISO 8601, of the form "yyyy-mm-ddThh:mm:ss".
     * Used to indicate the end time of a scheduled program, indented for use by tuners.
     *
     * <p><b>ARIB:</b>
     * 対象イベントの開始時刻を表す。
     * 対象イベントの終了時刻を表す。原則として、EIT のイベントループ内の
     * start_time 及び対象イベントの時間の長さを表す duration から終了時刻を生成し、
     * BCD表記からDLNAガイドラインで規定されている dc:date と同じ以下の書式に変換して挿入する。
     * TOTが無い場合は以下のように記載する。<br>
     * CCYY-MM-DDTHH:MM:SS<br>
     * サマータイムの導入によりTOTにサマータイム実施時の時間オフセット値が設定されている場合には、
     * 以下のようにTimeOffset(±HH:MM)を付加する記載とする。<br>
     * CCYY-MM-DDTHH:MM:SS+09:00 → サマータイムなし<br>
     * CCYY-MM-DDTHH:MM:SS+10:00 → サマータイムあり（１時間）<br>
     * start_time もしくは duration が不定なために
     * upnp:scheduledEndTime が生成できない場合にはこの property を配置しない。
     *
     * <p>Namespace: UPnP / Type: string
     */
    public static final String UPNP_SCHEDULED_END_TIME = "upnp:scheduledEndTime";
    /**
     * "upnp:icon".
     *
     * <p>Some icon that a control point can use in its UI to display the content,
     * e.g. a CNN logo for a Tuner channel.
     * Recommend same format as the icon element in the UPnP device description document schema. (PNG).
     * Values must be properly escaped URIs as described in [RFC 2396].
     *
     * <p><b>ARIB</b>
     * 当該サービス（編成チャンネル）のロゴのURLを表す。
     *
     * <p>Namespace: UPnP / Type: URI
     */
    public static final String UPNP_ICON = "upnp:icon";
    /**
     * "upnp:icon@arib:resolution".
     *
     * <p><b>ARIB</b>
     * 当該サービス（編成チャンネル）のロゴのサイズを表す。
     * 書式はres@resolution に従う。
     * また、挿入する値は「第一編 4 (3)ロゴデータの更新」の表4-1送出するロゴマークのサイズパターン
     * に記載されている中の該当するロゴの横ドット数と縦ドット数を用いる。
     * (横ドット数) x(縦ドット数)
     * 例）64x36
     *
     * <p>Namespace: ARIB / Type: pattern string
     */
    public static final String UPNP_ICON_ARIB_RESOLUTION = "upnp:icon@arib:resolution";
    /**
     * "upnp:rating".
     *
     * <p>Rating of the object's resource, for 'parental control' filtering purposes,
     * such as "R", "PG-13", "X", etc.,.
     *
     * <p><b>ARIB</b>
     * 視聴年齢の制限を表す。
     * 原則として、パレンタルレート記述子のratingの値を0xXXという16進数文字列に変換して挿入する。
     * 例えばratingの値が"10"の時には"0x10"として挿入する。
     * このpropertyはあくまでも表示のためであり、それ以外での使用は保証されない。
     *
     * <p>Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
     */
    public static final String UPNP_RATING = "upnp:rating";
    /**
     * "res".
     *
     * <p>Resource, typically a media file, associated with the object.
     * Values must be properly escaped URIs as described in [RFC 2396].
     *
     * <p>Namespace: DIDL-Lite / Type: URI
     */
    public static final String RES = "res";
    /**
     * "res@size".
     *
     * <p>Size in bytes of the resource.
     *
     * <p>Namespace: DIDL-Lite / Type: unsigned long
     */
    public static final String RES_SIZE = "res@size";
    /**
     * "size".
     *
     * @see #RES_SIZE
     */
    public static final String SIZE = "size";
    /**
     * "res@duration".
     *
     * <p>Time duration of the playback of the resource, at normal speed.
     *
     * <p>Namespace: DIDL-Lite / Type: duration string
     *
     * <pre>
     * The form of the duration string is:
     * H+:MM:SS[.F+], or H+:MM:SS[.F0/F1]
     * where :
     * H+ : number of digits (including no digits) to indicate elapsed hours,
     * MM : exactly 2 digits to indicate minutes (00 to 59),
     * SS : exactly 2 digits to indicate seconds (00 to 59),
     * F+ : any number of digits (including no digits) to indicate fractions of seconds,
     * F0/F1 : a fraction, with F0 and F1 at least one digit long, and F0 < F1.
     * The string may be preceded by an optional + or – sign,
     * and the decimal point itself may be omitted if there are no fractional second digits.
     * <pre>
     */
    public static final String RES_DURATION = "res@duration";
    /**
     * "duration".
     *
     * @see #RES_DURATION
     */
    public static final String DURATION = "duration";
    /**
     * "res@resolution".
     *
     * <p>XxY resolution of the resource in pixels (typically image item or video item).
     * String pattern is of the form: [0-9]+x[0-9]+ (one or more digits,'x', followed by one or more digits).
     *
     * <p><b>ARIB</b>
     * 出力するコンテンツの解像度を表す。
     * 解像度は、水平画素数と垂直画素数を用いて、半角文字列で以下のように表す。<br>
     * (水平画素数) x(垂直画素数)<br>
     * 例）1920x1080
     *
     * <p>Namespace: DIDL-Lite / Type: pattern string
     */
    public static final String RES_RESOLUTION = "res@resolution";
    /**
     * "resolution".
     *
     * @see #RES_RESOLUTION
     */
    public static final String RESOLUTION = "resolution";
    /**
     * "res@protocolInfo".
     *
     * <p>A string that identifies the recommended HTTP protocol for transmitting the resource
     * (see also UPnP A/V Connection Manager Service template, section 2.5.2).
     * If not present, then the content has not yet been fully imported by CDS and is not yet accessible for playback purposes.
     *
     * <p>Namespace: DIDL-Lite / Type: string
     */
    public static final String RES_PROTOCOL_INFO = "res@protocolInfo";
    /**
     * "protocolInfo".
     *
     * @see #RES_PROTOCOL_INFO
     */
    public static final String PROTOCOL_INFO = "protocolInfo";
    /**
     * "res@bitrate".
     *
     * <p>Bitrate in bytes/seconds of the encoding of the resource.
     *
     * <p>Namespace: DIDL-Lite / Type: unsigned integer
     */
    public static final String RES_BITRATE = "res@bitrate";
    /**
     * "bitrate".
     *
     * @see #RES_BITRATE
     */
    public static final String BITRATE = "bitrate";
    /**
     * "arib:objectType".
     *
     * <p><b>ARIB</b>
     * 「ARIB STD-B21 9.2 IP インタフェース仕様」および｢8.3 IP インタフェース運用仕様｣に準拠していることを表す。
     * 地上デジタルは"ARIB_TB"を、BSデジタルは"ARIB_BS"を、CSデジタルは"ARIB_CS"を、半角文字列で記述する。
     *
     * <p>Namespace: ARIB / Type: string
     */
    public static final String ARIB_OBJECT_TYPE = "arib:objectType";
    /**
     * "arib:audioComponentType".
     *
     * <p><b>ARIB</b>
     * 音声コンポーネントの種別を表す。
     * 原則として、EITの音声コンポーネント記述子のcomponent_typeの値を10進数に変えて挿入する。
     * 一つのservice_idが複数の音声ES を含む時にはこのpropertyを複数持つことができ、
     * この場合デフォルトESを最初に記述する。
     *
     * <p>Namespace: ARIB / Type: unsigned integer
     */
    public static final String ARIB_AUDIO_COMPONENT_TYPE = "arib:audioComponentType";
    /**
     * "arib:audioComponentType@qualityIndicator".
     *
     * <p><b>ARIB</b>
     * 音声コンポーネントの音質モードを表す。
     * 原則として、EITの音声コンポーネント記述子のquality_indicatorの値を10進数に変えて挿入する。
     *
     * <p>Namespace: ARIB / Type: unsigned integer
     */
    public static final String ARIB_AUDIO_COMPONENT_TYPE_QI = "arib:audioComponentType@qualityIndicator";
    /**
     * "arib:caProgramInfo".
     *
     * <p><b>ARIB</b>
     * 番組が有料か無料かを表す。
     * 原則として、EITのfree_CA_mode="1"の時には有料番組であるとして"1"を挿入し、
     * free_CA_mode="0"の時には無料番組であるとして"0"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_CA_PROGRAM_INFO = "arib:caProgramInfo";
    /**
     * "arib:caProgramInfo@price".
     *
     * <p><b>ARIB</b>
     * 有料番組の購入金額(視聴のみ、録画購入)を表す。
     * arib:caProgramInfo="1"(有料番組)の場合、購入金額を表す。
     * arib:caProgramInfo="0"(無料番組)の場合はこのattributeは持たない。
     * 購入金額を円単位の半角数字で、「視聴のみ」、「録画視聴」の順で挿入する。<br>
     * 例）「視聴のみ」のみの場合<br>
     * \@price="500"<br>
     * 「録画購入」ありの場合<br>
     * \@price="500,700"<br>
     *
     * <p>Namespace: ARIB / Type: CSV string
     */
    public static final String ARIB_CA_PROGRAM_INFO_PRICE = "arib:caProgramInfo@price";
    /**
     * "arib:caProgramInfo@available".
     *
     * <p><b>ARIB</b>
     * 有料番組の契約（購入）を表す。
     * arib:caProgramInfo="1"(有料番組)の場合、デフォルトES群が契約(購入)済かどうかを表す。
     * arib:caProgramInfo="0"(無料番組)の場合はこのattributeは持たない。
     * デフォルトES 群が契約(購入)済の時には"1"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_CA_PROGRAM_INFO_AVAILABLE = "arib:caProgramInfo@available";
    /**
     * "arib:captionInfo".
     *
     * <p><b>ARIB</b>
     * 字幕・文字スーパーの運用を表す。
     * 原則として、データコンテンツ記述子のdata_component_id="0008"、
     * または当該字幕ESがある場合に"1"を挿入する。
     * そうでない場合には"0"を挿入する。
     * 一つのservice_idが複数の字幕ESを含む時にはこのpropertyを複数持つことができ、
     * この場合デフォルトESを最初に記述する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_CAPTION_INFO = "arib:captionInfo";
    /**
     * "arib:copyControlInfo".
     *
     * <p><b>ARIB</b>
     * 番組の記録や出力の制御に関する情報を表す。
     * 原則として、コンテント利用記述子のencryption_modeとデジタルコピー制御記述子の
     * digital_recording_control_data、APS_control_dataと
     * 出力可/不可を表すビットの値を文字列にしてカンマで区切って挿入する。
     * 出力可/不可を表すビットは、"1"の時出力不可を表し、"0"の時出力可を表す。
     * このpropertyはあくまでも表示のためであり、それ以外での使用は保証されない。
     *
     * <p>Namespace: ARIB / Type: CSV string
     */
    public static final String ARIB_COPY_CONTROL_INFO = "arib:copyControlInfo";
    /**
     * "arib:dataProgramInfo".
     *
     * <p><b>ARIB</b>
     * データ放送の存在を表す。
     * 原則として、データコンテンツ記述子のdata_component_id="000C"かつ
     * entry_component="当該字幕ESのcomponent_tag値(0x40～0x7F)"である時、
     * または他の方法でデータ放送が有ることを判別できる時に"1"を挿入し、
     * そうではない時に"0"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_DATE_PROGRAM_INFO = "arib:dataProgramInfo";
    /**
     * "arib:dataProgramInfo@sync".
     *
     * <p><b>ARIB</b>
     * データ放送の番組連動を表す。
     * 原則として、arib:dataProgramInfo="1"に相当し、かつデータコンテンツ記述子のセレクタ領域で
     * associated_contents_flag="1"の時に"1"を挿入し、そうではない時に"0"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_DATE_PROGRAM_INFO_SYNC = "arib:dataProgramInfo@sync";
    /**
     * "arib:longDescription".
     *
     * <p><b>ARIB</b>
     * イベントの詳細説明を表す。
     * 原則として、EITのイベントループに含まれる拡張形式イベント記述子の item_description_char
     * で表される項目名および item_char で表される項目記述を使用する。
     * arib:longDescription は複数持つことができ、各拡張形式イベント記述子に対して
     * descriptor_number の昇順で arib:longDescription を挿入する。
     * 但し、1つの項目名に対して複数の項目記述が継続して記述されている場合、
     * それらを連結し1つの arib:longDescription として挿入する。
     * また、項目名は arib:longDescription の先頭から24バイトに記載し、25バイト目以降に項目記述を記載する。
     * 項目名が24バイトに満たないときは空白文字を挿入すること。
     *
     * <p>Namespace: ARIB / Type: string
     */
    public static final String ARIB_LONG_DESCRIPTION = "arib:longDescription";
    /**
     * "arib:multiESInfo".
     *
     * <p><b>ARIB</b>
     * 映像ESまたは音声ESが複数であることを表す。
     * 原則として、EITでのコンポーネント/音声コンポーネント記述子が
     * 複数配置されている時に"1"を挿入し、複数ではない時に"0"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_MULTI_ES_INFO = "arib:multiESInfo";
    /**
     * "arib:multiViewInfo".
     *
     * <p><b>ARIB</b>
     * マルチビューテレビ(MVTV)の運用を表す。
     * 原則として、component_group_type="000"であるコンポーネントグループ記述子が
     * EITにある時に"1"を挿入し、無い場合には"0"を挿入する。
     *
     * <p>Namespace: ARIB / Type: boolean
     */
    public static final String ARIB_MULTI_VIEW_INFO = "arib:multiViewInfo";
    /**
     * "arib:videoComponentType".
     *
     * <p><b>ARIB</b>
     * 映像コンポーネントの種別を表す。
     * 原則として、EIT のコンポーネント記述子のcomponent_typeの値を10進数に変えて挿入する。
     * 一つのservice_idが複数の映像ESを含む時にはこのpropertyを複数持つことができ、
     * この場合デフォルトESを最初に記述する。
     *
     * <p>Namespace: ARIB / Type: unsigned integer
     */
    public static final String ARIB_VIDEO_COMPONENT_TYPE = "arib:videoComponentType";

    // オブジェクト種別の定義
    @IntDef({TYPE_UNKNOWN, TYPE_VIDEO, TYPE_AUDIO, TYPE_IMAGE, TYPE_CONTAINER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentType {
    }

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
     * DIDL-Liteノードの情報
     */
    @NonNull
    private final Tag mRootTag;

    /**
     * XMLのタグ情報。
     *
     * <p>タグ名をKeyとして、TagのListを保持する。
     * 同一のタグが複数ある場合はListに出現順に格納する。
     */
    @NonNull
    private final TagMap mTagMap;

    /**
     * MediaServerのUDN
     */
    @NonNull
    private final String mUdn;
    /**
     * \@idの値。
     */
    @NonNull
    private final String mObjectId;
    /**
     * \@parentIDの値。
     */
    @NonNull
    private final String mParentId;
    /**
     * dc:titleの値
     */
    @NonNull
    private final String mTitle;
    /**
     * upnp:classの値
     */
    @NonNull
    private final String mUpnpClass;
    /**
     * upnp:classのint値表現。
     *
     * @see #TYPE_UNKNOWN
     * @see #TYPE_VIDEO
     * @see #TYPE_AUDIO
     * @see #TYPE_IMAGE
     * @see #TYPE_CONTAINER
     */
    @ContentType
    private final int mType;

    private static class Param {
        @NonNull
        private final String mObjectId;
        @NonNull
        private final String mParentId;
        @NonNull
        private final String mTitle;
        @NonNull
        private final String mUpnpClass;

        Param(TagMap map) {
            final String objectId = map.getValue(ID);
            final String parentId = map.getValue(PARENT_ID);
            final String title = map.getValue(DC_TITLE);
            final String upnpClass = map.getValue(UPNP_CLASS);
            if (objectId == null || parentId == null || title == null || upnpClass == null) {
                throw new IllegalArgumentException("Malformed item");
            }
            mObjectId = objectId;
            mParentId = parentId;
            mTitle = title;
            mUpnpClass = upnpClass;
        }
    }

    /**
     * elementをもとにインスタンス作成
     *
     * @param udn     MediaServerのUDN
     * @param element objectを示すelement
     * @param rootTag DIDL-Liteノードの情報
     */
    CdsObject(
            @NonNull final String udn,
            @NonNull final Element element,
            @NonNull final Tag rootTag) {
        mUdn = udn;
        mItem = isItem(element.getTagName());
        mRootTag = rootTag;
        mTagMap = parseElement(element);
        final Param param = new Param(mTagMap);
        mObjectId = param.mObjectId;
        mParentId = param.mParentId;
        mTitle = param.mTitle;
        mUpnpClass = param.mUpnpClass;
        mType = getType(mItem, mUpnpClass);
    }

    private static boolean isItem(String tagName) {
        switch (tagName) {
            case ITEM:
                return true;
            case CONTAINER:
                return false;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 子要素の情報をパースし、格納する。
     *
     * @param element objectを示すelement
     */
    @NonNull
    private static TagMap parseElement(@NonNull final Element element) {
        final TagMap map = new TagMap();
        map.putTag("", new Tag(element, true));
        Node node = element.getFirstChild();
        for (; node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            map.putTag(node.getNodeName(), new Tag((Element) node));
        }
        return map;
    }

    @ContentType
    private static int getType(
            final boolean isItem,
            final String upnpClass) {
        if (!isItem) {
            return TYPE_CONTAINER;
        } else if (upnpClass.startsWith(IMAGE_ITEM)) {
            return TYPE_IMAGE;
        } else if (upnpClass.startsWith(AUDIO_ITEM)) {
            return TYPE_AUDIO;
        } else if (upnpClass.startsWith(VIDEO_ITEM)) {
            return TYPE_VIDEO;
        }
        return TYPE_UNKNOWN;
    }

    /**
     * MediaServerのUDNを返す。
     *
     * @return MediaServerのUDN
     */
    @NonNull
    public String getUdn() {
        return mUdn;
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
    @ContentType
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
     * <p>XPATHはitemもしくはcontainerをルートとして指定する。
     * 名前空間はシンボルをそのまま記述する。
     * 例えば'item@id'及び'container@id'はともに'@id'を指定する。
     * 'item/dc:title'であれば'dc:title'を指定する。
     *
     * <p>複数同一のタグがあった場合は最初に現れた要素の値を返す。
     * {@link #getValue(String, int)}で第二引数に0を指定するのと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @return 指定された値。見つからない場合はnull
     * @see #getValue(String, int)
     */
    @Nullable
    public String getValue(@NonNull final String xpath) {
        return mTagMap.getValue(xpath);
    }

    /**
     * XPATH風の指定で示された値を返す。
     *
     * <p>XPATHはitemもしくはcontainerをルートとして指定する。
     * 名前空間はシンボルをそのまま記述する。
     * 例えば'item@id'及び'container@id'はともに'@id'を指定する。
     * 'item/dc:title'であれば'dc:title'を指定する。
     *
     * <p>複数同一のタグがあった場合は現れた順にインデックスが付けられる。
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
    public String getValue(
            @NonNull final String xpath,
            final int index) {
        return mTagMap.getValue(xpath, index);
    }

    /**
     * タグ名と属性名を指定して値を取り出す。
     *
     * <p>複数の同一タグがある場合は最初に現れたタグの情報を返す。
     * {@link #getValue(String, String, int)}の第三引数に0を指定したものと等価。
     *
     * @param tagName  タグ名
     * @param attrName 属性名、タグの値を取得するときはnullを指定する。
     * @return 指定された値。見つからない場合はnull
     * @see #getValue(String, String, int)
     */
    @Nullable
    public String getValue(
            @Nullable final String tagName,
            @Nullable final String attrName) {
        return mTagMap.getValue(tagName, attrName);
    }

    /**
     * タグ名と属性名を指定して値を取り出す。
     *
     * <p>複数同一のタグがあった場合は現れた順にインデックスが付けられる。
     * 属性値はタグと同じインデックスに格納される。
     *
     * @param tagName  タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param attrName 属性名、タグの値を取得するときはnullを指定する。
     * @param index    インデックス値
     * @return 指定された値。見つからない場合はnull
     */
    @Nullable
    public String getValue(
            @Nullable final String tagName,
            @Nullable final String attrName,
            final int index) {
        return mTagMap.getValue(tagName, attrName, index);
    }

    /**
     * 指定したタグ名のTagインスタンスを返す。
     *
     * <p>複数同一タグが存在した場合は最初に現れたタグ。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンス、見つからない場合はnull
     */
    @Nullable
    public Tag getTag(@Nullable final String tagName) {
        return mTagMap.getTag(tagName);
    }

    /**
     * 指定したタグ名、インデックスのTagインスタンスを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param index   インデックス値
     * @return Tagインスタンス、見つからない場合はnull
     */
    @Nullable
    public Tag getTag(
            @Nullable final String tagName,
            final int index) {
        return mTagMap.getTag(tagName, index);
    }

    /**
     * ルートタグ情報を返す。
     *
     * <p>CdsObjectXmlFormatterから利用するため。
     *
     * @return ルートタグ情報
     */
    @NonNull
    public Tag getRootTag() {
        return mRootTag;
    }

    /**
     * Tagを格納したマップそのものを返す。
     *
     * <p>CdsObjectXmlFormatterから利用するため。
     *
     * @return TagMap
     */
    @NonNull
    TagMap getTagMap() {
        return mTagMap;
    }

    /**
     * 指定したタグ名のTagインスタンスリストを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンスリスト
     */
    @Nullable
    public List<Tag> getTagList(@Nullable final String tagName) {
        return mTagMap.getTagList(tagName);
    }

    /**
     * XPATH風の指定で示された値をInt値として返す。
     *
     * <p>{@link #getValue(String)} の結果を {@link #parseIntSafely(String, int)} に渡すことと等価
     *
     * @param xpath        パラメータの位置を表現するXPATH風の指定
     * @param defaultValue 値が見つからない場合、Int値にパースできない値だった場合のデフォルト値
     * @return 指定された値
     * @see #getValue(String)
     */
    public int getIntValue(
            @NonNull final String xpath,
            final int defaultValue) {
        return parseIntSafely(getValue(xpath), defaultValue);
    }

    /**
     * XPATH風の指定で示された値をInt値として返す。
     *
     * <p>{@link #getValue(String, int)} の結果を {@link #parseIntSafely(String, int)} に渡すことと等価
     *
     * @param xpath        パラメータの位置を表現するXPATH風の指定
     * @param index        インデックス値
     * @param defaultValue 値が見つからない場合、Int値にパースできない値だった場合のデフォルト値
     * @return 指定された値
     * @see #getValue(String, int)
     */
    public int getIntValue(
            @NonNull final String xpath,
            final int index,
            final int defaultValue) {
        return parseIntSafely(getValue(xpath, index), defaultValue);
    }

    /**
     * XPATH風の指定で示された値をDateとして返す。
     *
     * <p>{@link #getValue(String)} の結果を {@link #parseDate(String)} に渡すことと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @return 指定された値。値が見つからない場合、パースできない値の場合null
     * @see #getValue(String, int)
     */
    @Nullable
    public Date getDateValue(@NonNull final String xpath) {
        return parseDate(getValue(xpath));
    }

    /**
     * XPATH風の指定で示された値をDateとして返す。
     *
     * <p>{@link #getValue(String, int)} の結果を {@link #parseDate(String)} に渡すことと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @param index インデックス値
     * @return 指定された値。値が見つからない場合、パースできない値の場合null
     * @see #getValue(String, int)
     */
    @Nullable
    public Date getDateValue(
            @NonNull final String xpath,
            final int index) {
        return parseDate(getValue(xpath, index));
    }

    /**
     * 与えられた文字列を10進数としてパースする。
     *
     * @param value        パースする文字列
     * @param defaultValue パースできない場合のデフォルト値
     * @return パース結果
     */
    public static int parseIntSafely(
            @Nullable final String value,
            final int defaultValue) {
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
    public static int parseIntSafely(
            @Nullable final String value,
            final int radix,
            final int defaultValue) {
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

    private static Date parseD(@NonNull final String value) throws ParseException {
        synchronized (FORMAT_D) {
            return FORMAT_D.parse(value);
        }
    }

    private static Date parseT(@NonNull final String value) throws ParseException {
        synchronized (FORMAT_T) {
            return FORMAT_T.parse(value);
        }
    }

    private static Date parseZ(@NonNull final String value) throws ParseException {
        synchronized (FORMAT_Z) {
            return FORMAT_Z.parse(value);
        }
    }


    /**
     * 与えられた文字列をパースしてDateとして戻す。
     *
     * <p>CDSで使用される日付フォーマットにはいくつかバリエーションがあるが、
     * 該当するフォーマットでパースを行う。
     *
     * @param value パースする文字列
     * @return パース結果、パースできない場合null
     */
    @Nullable
    public static Date parseDate(@Nullable final String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            if (value.length() <= 10) {
                return parseD(value);
            }
            if (value.length() <= 19) {
                return parseT(value);
            }
            if (value.lastIndexOf(':') == 22) {
                return parseZ(value.substring(0, 22) + value.substring(23));
            }
            return parseZ(value);
        } catch (final ParseException e) {
            return null;
        }
    }

    /**
     * リソースの数を返す。
     *
     * @return リソースの数
     */
    public int getResourceCount() {
        final List<Tag> list = getTagList(CdsObject.RES);
        return list == null ? 0 : list.size();
    }

    /**
     * リソースを持っているか否かを返す。
     *
     * @return リソースを持っている場合true
     */
    public boolean hasResource() {
        final List<Tag> tagList = getTagList(CdsObject.RES);
        return !(tagList == null || tagList.isEmpty());
    }

    /**
     * 著作権保護されたリソースを持っているか否かを返す。
     *
     * @return 著作権保護されたリソースを持っている場合true
     */
    public boolean hasProtectedResource() {
        final List<Tag> tagList = getTagList(CdsObject.RES);
        if (tagList == null) {
            return false;
        }
        for (final Tag tag : tagList) {
            final String protocolInfo = tag.getAttribute(CdsObject.PROTOCOL_INFO);
            final String mimeType = extractMimeTypeFromProtocolInfo(protocolInfo);
            if (!TextUtils.isEmpty(mimeType) && mimeType.equals("application/x-dtcp1")) {
                return true;
            }
        }
        return false;
    }

    /**
     * protocolInfoの文字列からMimeTypeの文字列を抽出する。
     *
     * @param protocolInfo protocolInfo
     * @return MimeTypeの文字列。抽出に失敗した場合null
     */
    @Nullable
    public static String extractMimeTypeFromProtocolInfo(@Nullable final String protocolInfo) {
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
    public static String extractProtocolFromProtocolInfo(@Nullable final String protocolInfo) {
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
        return mTagMap.toString();
    }

    @Override
    public int hashCode() {
        return mTagMap.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CdsObject)) {
            return false;
        }
        final CdsObject obj = (CdsObject) o;
        return mObjectId.equals(obj.mObjectId) && mUdn.equals(obj.mUdn);
    }

    /**
     * Parcelable用のコンストラクタ。
     *
     * @param in Parcel
     */
    private CdsObject(@NonNull final Parcel in) {
        mUdn = in.readString();
        mItem = in.readByte() != 0;
        mRootTag = in.readParcelable(Tag.class.getClassLoader());
        mTagMap = in.readParcelable(TagMap.class.getClassLoader());
        final Param param = new Param(mTagMap);
        mObjectId = param.mObjectId;
        mParentId = param.mParentId;
        mTitle = param.mTitle;
        mUpnpClass = param.mUpnpClass;
        mType = getType(mItem, mUpnpClass);
    }

    @Override
    public void writeToParcel(
            @NonNull final Parcel dest,
            int flags) {
        dest.writeString(mUdn);
        dest.writeByte((byte) (mItem ? 1 : 0));
        dest.writeParcelable(mRootTag, flags);
        dest.writeParcelable(mTagMap, flags);
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
        public CdsObject createFromParcel(@NonNull final Parcel in) {
            return new CdsObject(in);
        }

        @Override
        public CdsObject[] newArray(final int size) {
            return new CdsObject[size];
        }
    };
}
