/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.os.Parcelable
import androidx.annotation.IntDef
import java.util.*

/**
 * ContentDirectoryServiceのObjectを表現するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface CdsObject : Parcelable {
    /**
     * MediaServerのUDN
     */
    val udn: String

    /**
     * アイテムであるか否か
     */
    val isItem: Boolean

    /**
     * コンテナであるか否か
     */
    val isContainer: Boolean
        get() = !isItem

    /**
     * Typeの値
     */
    @get:ContentType
    val type: Int

    /**
     * \@idの値
     */
    val objectId: String

    /**
     * \@parentIDの値
     */
    val parentId: String

    /**
     * upnp:classの値
     */
    val upnpClass: String

    /**
     * dc:titleの値
     */
    val title: String

    /**
     * ルートタグ情報
     *
     * CdsObjectXmlFormatter/CdsFormatterから利用
     */
    val rootTag: Tag

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
    fun getValue(xpath: String, index: Int = 0): String?

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
    fun getValue(tagName: String?, attrName: String?, index: Int = 0): String?

    /**
     * 指定したタグ名、インデックスのTagインスタンスを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param index   インデックス値
     * @return Tagインスタンス、見つからない場合はnull
     */
    fun getTag(tagName: String?, index: Int = 0): Tag?

    /**
     * 指定したタグ名のTagインスタンスリストを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンスリスト
     */
    fun getTagList(tagName: String?): List<Tag>?

    /**
     * XPATH風の指定で示された値をInt値として返す。
     *
     * [.getValue] の結果を [.parseIntSafely] に渡すことと等価
     *
     * @param xpath        パラメータの位置を表現するXPATH風の指定
     * @param defaultValue 値が見つからない場合、Int値にパースできない値だった場合のデフォルト値
     * @param index        インデックス値
     * @return 指定された値
     * @see .getValue
     */
    fun getIntValue(xpath: String, defaultValue: Int, index: Int = 0): Int

    /**
     * XPATH風の指定で示された値をDateとして返す。
     *
     * [.getValue] の結果を [.parseDate] に渡すことと等価
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @param index インデックス値
     * @return 指定された値。値が見つからない場合、パースできない値の場合null
     * @see .getValue
     */
    fun getDateValue(xpath: String, index: Int = 0): Date?

    /**
     * リソースの数
     *
     * @return リソースの数
     */
    fun getResourceCount(): Int

    /**
     * リソースを持っているか否かを返す。
     *
     * @return リソースを持っている場合true
     */
    fun hasResource(): Boolean

    /**
     * 著作権保護されたリソースを持っているか否かを返す。
     *
     * @return 著作権保護されたリソースを持っている場合true
     */
    fun hasProtectedResource(): Boolean

    /**
     * 全情報をダンプした文字列を返す。
     *
     * @return ダンプ文字列
     */
    fun toDumpString(): String

    companion object {
        // オブジェクト種別の定義
        @IntDef(TYPE_UNKNOWN, TYPE_VIDEO, TYPE_AUDIO, TYPE_IMAGE, TYPE_CONTAINER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ContentType

        // XML関係の定義
        const val DIDL_LITE = "DIDL-Lite"

        /**
         * "item".
         *
         * item is a first-level class derived directly from object.
         * An item most often represents a single piece of AV data, such as a CD track, a movie or an audio file.
         * Items may be playable, meaning they have information that can be played on a rendering device.
         * Any object which derives from the item class is expressed via the DIDL-Lite item structure.
         */
        const val ITEM = "item"

        /**
         * "container".
         *
         * container is a first-level class derived directly from object.
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
        const val CONTAINER = "container"

        /**
         * "@id".
         *
         * An identifier for the object.
         * The value of each object id property must be unique with respect to the Content Directory.
         *
         * Namespace: DIDL-Lite / Type: string
         */
        const val ID = "@id"

        /**
         * "@parentID".
         *
         * id property of object's parent.
         * The parentID of the Content Directory 'root' container must be set to the reserved value of "-1".
         * No other parentID attribute of any other Content Directory object may take this value.
         *
         * Namespace: DIDL-Lite / Type: string
         */
        const val PARENT_ID = "@parentID"

        /**
         * "@restricted".
         *
         * When true, ability to modify a given object is confined to the Content Directory Service.
         * Control point metadata write access is disabled.
         *
         * Namespace: DIDL-Lite / Type: boolean
         */
        const val RESTRICTED = "@restricted"

        /**
         * "@childCount".
         *
         * Child count for the object.
         * Applies to containers only.
         *
         * Namespace: DIDL-Lite / Type: integer
         */
        const val CHILD_COUNT = "@childCount"

        /**
         * "@searchable".
         *
         * When true, the ability to perform a Search() action under a container is enabled,
         * otherwise a Search() under that container will return no results.
         * The default value of this attribute when it is absent on a container is false
         *
         * Namespace: DIDL-Lite / Type: boolean
         */
        const val SEARCHABLE = "@searchable"

        /**
         * "dc:title".
         *
         * Name of the object.
         *
         * **ARIB:**
         * イベント名を表す。原則として、EIT のイベントループに含まれる短形式イベント記述子の、第1 ループに含まれる
         * event_name_char の情報を挿入する。その場合、挿入する情報はEIT の情報の変更に合わせて更新することが望ましい。
         * event_name_char が不明な場合、DLNA ガイドラインの規定に従い、upnp:channelName と同一の文字列を挿入すること。
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_TITLE = "dc:title"

        /**
         * "dc:date".
         *
         * ISO 8601, of the form "YYYY-MM-DD".
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_DATE = "dc:date"

        /**
         * "dc:creator".
         *
         * Primary content creator or owner of the object.
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_CREATOR = "dc:creator"

        /**
         * "dc:description".
         *
         * Description may include but is not limited to: an abstract, a table of contents,
         * a graphical representation, or a free-text account of the resource.
         *
         * **ARIB:**
         * 番組記述を表す。
         * 原則として、EITのイベントループに含まれる短形式イベント記述子の、第2ループに含まれる text_char を挿入する。
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_DESCRIPTION = "dc:description"

        /**
         * "upnp:longDescription".
         *
         * The upnp:longDescription property contains a few lines of description of the content item
         * (longer than the dc:description property).
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_LONG_DESCRIPTION = "upnp:longDescription"

        /**
         * "object.item.imageItem".
         *
         * upnp:class value.
         * represents a piece of content that, when rendered, generates some still image.
         */
        const val IMAGE_ITEM = "object.item.imageItem"

        /**
         * "object.item.audioItem".
         *
         * upnp:class value.
         * represents a piece of content that, when rendered, generates some audio.
         */
        const val AUDIO_ITEM = "object.item.audioItem"

        /**
         * "object.item.videoItem".
         *
         * upnp:class value.
         * represents a piece of content that, when rendered, generates some video.
         */
        const val VIDEO_ITEM = "object.item.videoItem"

        /**
         * "upnp:class".
         *
         * Class of the object.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_CLASS = "upnp:class"

        /**
         * "upnp:genre".
         *
         * Name of the genre to which an object belongs.
         *
         * **ARIB:**
         * dc:title で表されるイベントが属するジャンルを表す。
         * 原則として、EIT のイベントループに含まれるコンテント記述子に含まれる
         * content_nibble_level_1 （大分類）の値から
         * 「第四編第4 部 付録\[付録A]」のジャンル大分類の表の「記述内容」に記載されている記述（文字列）に変換して挿入する。
         * upnp:genre は複数持つことができる。但し content_nibble_level_1 が 0xC, 0xD, 0xE の場合は、
         * "未定義"という文字列を挿入すること。また content_nibble_level_1 が不明な場合、"不明"という文字列を挿入すること。
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_GENRE = "upnp:genre"

        /**
         * "upnp:actor".
         *
         * Name of an actor appearing in a video item.
         *
         * Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
         */
        const val UPNP_ACTOR = "upnp:actor"

        /**
         * "upnp:actor@role".
         *
         * Role of the actor in the work.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_ACTOR_ROLE = "upnp:actor@role"

        /**
         * "upnp:artist".
         *
         * Name of an artist.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_ARTIST = "upnp:artist"

        /**
         * "upnp:artist@role".
         *
         * Role of the artist in the work.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_ARTIST_ROLE = "upnp:artist@role"

        /**
         * "upnp:author".
         *
         * Name of an author of a text item.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_AUTHOR = "upnp:author"

        /**
         * "upnp:author@role".
         *
         * Role of the author in the work.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_AUTHOR_ROLE = "upnp:author@role"

        /**
         * "upnp:producer".
         *
         * Name of producer of e.g., a movie or CD
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_PRODUCER = "upnp:producer"

        /**
         * "upnp:director".
         *
         * Name of the director of the video content item (e.g., the movie).
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_DIRECTOR = "upnp:director"

        /**
         * "dc:publisher".
         *
         * An entity responsible for making the resource available.
         * Examples of a Publisher include a person, an organization, or a service.
         * Typically, the name of a Publisher should be used to indicate the entity.
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_PUBLISHER = "dc:publisher"

        /**
         * "dc:contributor".
         *
         * An entity responsible for making contributions to the resource.
         * Examples of a Contributor include a person, an organization, or a service.
         * Typically, the name of a Contributor should be used to indicate the entity.
         *
         * Namespace: Dublin Core / Type: string
         */
        const val DC_CONTRIBUTOR = "dc:contributor"

        /**
         * "upnp:album".
         *
         * Title of the album to which the item belongs.
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_ALBUM = "upnp:album"

        /**
         * "upnp:albumArtURI".
         *
         * Reference to album art.
         * Values must be properly escaped URIs as described in [RFC 2396].
         *
         * Namespace: UPnP / Type: URI
         */
        const val UPNP_ALBUM_ART_URI = "upnp:albumArtURI"

        /**
         * "upnp:channelName".
         *
         * Used for identification of channels themselves,
         * or information associated with a piece of recorded content.
         *
         * **ARIB:**
         * サービス（編成チャンネル）名を表す。
         * 原則として、SDT のサービスループに含まれるサービス記述子の、第2 ループに含まれる char の情報を挿入する。
         * char が不明な場合、以前設定されていた編成チャンネル名を挿入することが望ましい。
         *
         * Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
         */
        const val UPNP_CHANNEL_NAME = "upnp:channelName"

        /**
         * "upnp:channelNr".
         *
         * Used for identification of tuner channels themselves,
         * or information associated with a piece of recorded content.
         *
         * **ARIB:**
         * 以下の式で示されるチャンネル番号を表す。<br></br>
         * upnp:channelNr = ワンタッチ選局番号×10000＋3桁番号×10＋枝番\[ワンタッチ選局番号]<br></br>
         * 受信機でサービス（編成チャンネル）に対して設定されている値を利用する。
         * サービスにワンタッチ選局番号が割り振られていない場合には0とする。
         * [3桁番号]原則としてNITの第2ループ（TSループ）に含まれるサービスリスト記述子の
         * service_id と、同じTS 情報記述子の remote_control_key_id から、
         * 「第7編9.1.3(d)」で規定される計算式によって生成される値を利用する。
         * NIT が取得できない場合、以前設定されていた３桁番号を挿入することが望ましい。
         *
         * Namespace: UPnP / Type: integer
         */
        const val UPNP_CHANNEL_NR = "upnp:channelNr"

        /**
         * "upnp:scheduledStartTime".
         *
         * ISO 8601, of the form "yyyy-mm-ddThh:mm:ss".
         * Used to indicate the start time of a schedule program, indented for use by tuners.
         *
         * **ARIB:**
         * 対象イベントの開始時刻を表す。
         * 原則として、EITのイベントループ内にある対象イベントの開始時刻を表す
         * start_time を MJD+BCD 表記からDLNAガイドラインで規定されている dc:date と同じ以下の書式に変換して挿入する。
         * TOTが無い場合は以下のように記載する。<br></br>
         * CCYY-MM-DDTHH:MM:SS<br></br>
         * サマータイムの導入によりTOTにサマータイム実施時の時間オフセット値が設定されている場合には、
         * 以下のようにTimeOffset(±HH:MM)を付加する記載とする。<br></br>
         * CCYY-MM-DDTHH:MM:SS+09:00 → サマータイムなし<br></br>
         * CCYY-MM-DDTHH:MM:SS+10:00 → サマータイムあり（１時間）<br></br>
         * start_timeが不定な値の場合にはこのpropertyを配置しない。
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_SCHEDULED_START_TIME = "upnp:scheduledStartTime"

        /**
         * "upnp:scheduledEndTime".
         *
         * ISO 8601, of the form "yyyy-mm-ddThh:mm:ss".
         * Used to indicate the end time of a scheduled program, indented for use by tuners.
         *
         * **ARIB:**
         * 対象イベントの開始時刻を表す。
         * 対象イベントの終了時刻を表す。原則として、EIT のイベントループ内の
         * start_time 及び対象イベントの時間の長さを表す duration から終了時刻を生成し、
         * BCD表記からDLNAガイドラインで規定されている dc:date と同じ以下の書式に変換して挿入する。
         * TOTが無い場合は以下のように記載する。<br></br>
         * CCYY-MM-DDTHH:MM:SS<br></br>
         * サマータイムの導入によりTOTにサマータイム実施時の時間オフセット値が設定されている場合には、
         * 以下のようにTimeOffset(±HH:MM)を付加する記載とする。<br></br>
         * CCYY-MM-DDTHH:MM:SS+09:00 → サマータイムなし<br></br>
         * CCYY-MM-DDTHH:MM:SS+10:00 → サマータイムあり（１時間）<br></br>
         * start_time もしくは duration が不定なために
         * upnp:scheduledEndTime が生成できない場合にはこの property を配置しない。
         *
         * Namespace: UPnP / Type: string
         */
        const val UPNP_SCHEDULED_END_TIME = "upnp:scheduledEndTime"

        /**
         * "upnp:icon".
         *
         * Some icon that a control point can use in its UI to display the content,
         * e.g. a CNN logo for a Tuner channel.
         * Recommend same format as the icon element in the UPnP device description document schema. (PNG).
         * Values must be properly escaped URIs as described in [RFC 2396].
         *
         * **ARIB**
         * 当該サービス（編成チャンネル）のロゴのURLを表す。
         *
         * Namespace: UPnP / Type: URI
         */
        const val UPNP_ICON = "upnp:icon"

        /**
         * "upnp:icon@arib:resolution".
         *
         * **ARIB**
         * 当該サービス（編成チャンネル）のロゴのサイズを表す。
         * 書式はres@resolution に従う。
         * また、挿入する値は「第一編 4 (3)ロゴデータの更新」の表4-1送出するロゴマークのサイズパターン
         * に記載されている中の該当するロゴの横ドット数と縦ドット数を用いる。
         * (横ドット数) x(縦ドット数)
         * 例）64x36
         *
         * Namespace: ARIB / Type: pattern string
         */
        const val UPNP_ICON_ARIB_RESOLUTION = "upnp:icon@arib:resolution"

        /**
         * "upnp:rating".
         *
         * Rating of the object's resource, for 'parental control' filtering purposes,
         * such as "R", "PG-13", "X", etc.,.
         *
         * **ARIB**
         * 視聴年齢の制限を表す。
         * 原則として、パレンタルレート記述子のratingの値を0xXXという16進数文字列に変換して挿入する。
         * 例えばratingの値が"10"の時には"0x10"として挿入する。
         * このpropertyはあくまでも表示のためであり、それ以外での使用は保証されない。
         *
         * Namespace: UPnP / Type: string, not standardized by the Content Directory Service.
         */
        const val UPNP_RATING = "upnp:rating"

        /**
         * "res".
         *
         * Resource, typically a media file, associated with the object.
         * Values must be properly escaped URIs as described in [RFC 2396].
         *
         * Namespace: DIDL-Lite / Type: URI
         */
        const val RES = "res"

        /**
         * "res@size".
         *
         * Size in bytes of the resource.
         *
         * Namespace: DIDL-Lite / Type: unsigned long
         */
        const val RES_SIZE = "res@size"

        /**
         * "size".
         *
         * @see .RES_SIZE
         */
        const val SIZE = "size"

        /**
         * "res@duration".
         *
         * Time duration of the playback of the resource, at normal speed.
         *
         * Namespace: DIDL-Lite / Type: duration string
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
         </pre></pre> */
        const val RES_DURATION = "res@duration"

        /**
         * "duration".
         *
         * @see .RES_DURATION
         */
        const val DURATION = "duration"

        /**
         * "res@resolution".
         *
         * XxY resolution of the resource in pixels (typically image item or video item).
         * String pattern is of the form: [0-9]+x[0-9]+ (one or more digits,'x', followed by one or more digits).
         *
         * **ARIB**
         * 出力するコンテンツの解像度を表す。
         * 解像度は、水平画素数と垂直画素数を用いて、半角文字列で以下のように表す。<br></br>
         * (水平画素数) x(垂直画素数)<br></br>
         * 例）1920x1080
         *
         * Namespace: DIDL-Lite / Type: pattern string
         */
        const val RES_RESOLUTION = "res@resolution"

        /**
         * "resolution".
         *
         * @see .RES_RESOLUTION
         */
        const val RESOLUTION = "resolution"

        /**
         * "res@protocolInfo".
         *
         * A string that identifies the recommended HTTP protocol for transmitting the resource
         * (see also UPnP A/V Connection Manager Service template, section 2.5.2).
         * If not present, then the content has not yet been fully imported by CDS and is not yet accessible for playback purposes.
         *
         * Namespace: DIDL-Lite / Type: string
         */
        const val RES_PROTOCOL_INFO = "res@protocolInfo"

        /**
         * "protocolInfo".
         *
         * @see .RES_PROTOCOL_INFO
         */
        const val PROTOCOL_INFO = "protocolInfo"

        /**
         * "res@bitrate".
         *
         * Bitrate in bytes/seconds of the encoding of the resource.
         *
         * Namespace: DIDL-Lite / Type: unsigned integer
         */
        const val RES_BITRATE = "res@bitrate"

        /**
         * "bitrate".
         *
         * @see .RES_BITRATE
         */
        const val BITRATE = "bitrate"

        /**
         * "arib:objectType".
         *
         * **ARIB**
         * 「ARIB STD-B21 9.2 IP インタフェース仕様」および｢8.3 IP インタフェース運用仕様｣に準拠していることを表す。
         * 地上デジタルは"ARIB_TB"を、BSデジタルは"ARIB_BS"を、CSデジタルは"ARIB_CS"を、半角文字列で記述する。
         *
         * Namespace: ARIB / Type: string
         */
        const val ARIB_OBJECT_TYPE = "arib:objectType"

        /**
         * "arib:audioComponentType".
         *
         * **ARIB**
         * 音声コンポーネントの種別を表す。
         * 原則として、EITの音声コンポーネント記述子のcomponent_typeの値を10進数に変えて挿入する。
         * 一つのservice_idが複数の音声ES を含む時にはこのpropertyを複数持つことができ、
         * この場合デフォルトESを最初に記述する。
         *
         * Namespace: ARIB / Type: unsigned integer
         */
        const val ARIB_AUDIO_COMPONENT_TYPE = "arib:audioComponentType"

        /**
         * "arib:audioComponentType@qualityIndicator".
         *
         * **ARIB**
         * 音声コンポーネントの音質モードを表す。
         * 原則として、EITの音声コンポーネント記述子のquality_indicatorの値を10進数に変えて挿入する。
         *
         * Namespace: ARIB / Type: unsigned integer
         */
        const val ARIB_AUDIO_COMPONENT_TYPE_QI = "arib:audioComponentType@qualityIndicator"

        /**
         * "arib:caProgramInfo".
         *
         * **ARIB**
         * 番組が有料か無料かを表す。
         * 原則として、EITのfree_CA_mode="1"の時には有料番組であるとして"1"を挿入し、
         * free_CA_mode="0"の時には無料番組であるとして"0"を挿入する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_CA_PROGRAM_INFO = "arib:caProgramInfo"

        /**
         * "arib:caProgramInfo@price".
         *
         * **ARIB**
         * 有料番組の購入金額(視聴のみ、録画購入)を表す。
         * arib:caProgramInfo="1"(有料番組)の場合、購入金額を表す。
         * arib:caProgramInfo="0"(無料番組)の場合はこのattributeは持たない。
         * 購入金額を円単位の半角数字で、「視聴のみ」、「録画視聴」の順で挿入する。<br></br>
         * 例）「視聴のみ」のみの場合<br></br>
         * \@price="500"<br></br>
         * 「録画購入」ありの場合<br></br>
         * \@price="500,700"<br></br>
         *
         * Namespace: ARIB / Type: CSV string
         */
        const val ARIB_CA_PROGRAM_INFO_PRICE = "arib:caProgramInfo@price"

        /**
         * "arib:caProgramInfo@available".
         *
         * **ARIB**
         * 有料番組の契約（購入）を表す。
         * arib:caProgramInfo="1"(有料番組)の場合、デフォルトES群が契約(購入)済かどうかを表す。
         * arib:caProgramInfo="0"(無料番組)の場合はこのattributeは持たない。
         * デフォルトES 群が契約(購入)済の時には"1"を挿入する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_CA_PROGRAM_INFO_AVAILABLE = "arib:caProgramInfo@available"

        /**
         * "arib:captionInfo".
         *
         * **ARIB**
         * 字幕・文字スーパーの運用を表す。
         * 原則として、データコンテンツ記述子のdata_component_id="0008"、
         * または当該字幕ESがある場合に"1"を挿入する。
         * そうでない場合には"0"を挿入する。
         * 一つのservice_idが複数の字幕ESを含む時にはこのpropertyを複数持つことができ、
         * この場合デフォルトESを最初に記述する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_CAPTION_INFO = "arib:captionInfo"

        /**
         * "arib:copyControlInfo".
         *
         * **ARIB**
         * 番組の記録や出力の制御に関する情報を表す。
         * 原則として、コンテント利用記述子のencryption_modeとデジタルコピー制御記述子の
         * digital_recording_control_data、APS_control_dataと
         * 出力可/不可を表すビットの値を文字列にしてカンマで区切って挿入する。
         * 出力可/不可を表すビットは、"1"の時出力不可を表し、"0"の時出力可を表す。
         * このpropertyはあくまでも表示のためであり、それ以外での使用は保証されない。
         *
         * Namespace: ARIB / Type: CSV string
         */
        const val ARIB_COPY_CONTROL_INFO = "arib:copyControlInfo"

        /**
         * "arib:dataProgramInfo".
         *
         * **ARIB**
         * データ放送の存在を表す。
         * 原則として、データコンテンツ記述子のdata_component_id="000C"かつ
         * entry_component="当該字幕ESのcomponent_tag値(0x40～0x7F)"である時、
         * または他の方法でデータ放送が有ることを判別できる時に"1"を挿入し、
         * そうではない時に"0"を挿入する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_DATE_PROGRAM_INFO = "arib:dataProgramInfo"

        /**
         * "arib:dataProgramInfo@sync".
         *
         * **ARIB**
         * データ放送の番組連動を表す。
         * 原則として、arib:dataProgramInfo="1"に相当し、かつデータコンテンツ記述子のセレクタ領域で
         * associated_contents_flag="1"の時に"1"を挿入し、そうではない時に"0"を挿入する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_DATE_PROGRAM_INFO_SYNC = "arib:dataProgramInfo@sync"

        /**
         * "arib:longDescription".
         *
         * **ARIB**
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
         * Namespace: ARIB / Type: string
         */
        const val ARIB_LONG_DESCRIPTION = "arib:longDescription"

        /**
         * "arib:multiESInfo".
         *
         * **ARIB**
         * 映像ESまたは音声ESが複数であることを表す。
         * 原則として、EITでのコンポーネント/音声コンポーネント記述子が
         * 複数配置されている時に"1"を挿入し、複数ではない時に"0"を挿入する。
         *
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_MULTI_ES_INFO = "arib:multiESInfo"

        /**
         * "arib:multiViewInfo".
         *
         * **ARIB**
         * マルチビューテレビ(MVTV)の運用を表す。
         * 原則として、component_group_type="000"であるコンポーネントグループ記述子が
         * EITにある時に"1"を挿入し、無い場合には"0"を挿入する。
         *
         * Namespace: ARIB / Type: boolean
         */
        const val ARIB_MULTI_VIEW_INFO = "arib:multiViewInfo"

        /**
         * "arib:videoComponentType".
         *
         * **ARIB**
         * 映像コンポーネントの種別を表す。
         * 原則として、EIT のコンポーネント記述子のcomponent_typeの値を10進数に変えて挿入する。
         * 一つのservice_idが複数の映像ESを含む時にはこのpropertyを複数持つことができ、
         * この場合デフォルトESを最初に記述する。
         *
         * Namespace: ARIB / Type: unsigned integer
         */
        const val ARIB_VIDEO_COMPONENT_TYPE = "arib:videoComponentType"

        /**
         * 未定義
         */
        const val TYPE_UNKNOWN = 0

        /**
         * 動画
         */
        const val TYPE_VIDEO = 1

        /**
         * 音楽
         */
        const val TYPE_AUDIO = 2

        /**
         * 画像
         */
        const val TYPE_IMAGE = 3

        /**
         * コンテナ
         */
        const val TYPE_CONTAINER = 4
    }
}
