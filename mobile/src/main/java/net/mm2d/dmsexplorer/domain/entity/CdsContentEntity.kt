package net.mm2d.dmsexplorer.domain.entity

import android.net.Uri
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.PropertyParser
import net.mm2d.android.upnp.cds.Tag
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter
import net.mm2d.dmsexplorer.util.StringJoiner

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CdsContentEntity(
    override val rawEntity: CdsObject
) : ContentEntity {
    override val id: String = rawEntity.objectId
    private var selectedRes: Tag? = null
    override val type: ContentType
    override var uri: Uri? = null
        private set
    override var mimeType: String? = null
        private set
    override val name: String
        get() = rawEntity.title
    override val date: Long
        get() = rawEntity.getDateValue(CdsObject.UPNP_SCHEDULED_START_TIME)?.time
            ?: rawEntity.getDateValue(CdsObject.DC_DATE)?.time
            ?: 0L

    override val description: String
        get() {
            val stringJoiner = StringJoiner()
            when (type) {
                ContentType.MOVIE -> {
                    stringJoiner.join(CdsFormatter.makeChannel(rawEntity))
                    stringJoiner.join(CdsFormatter.makeScheduleOrDate(rawEntity))
                }
                ContentType.MUSIC -> {
                    stringJoiner.join(CdsFormatter.makeArtistsSimple(rawEntity))
                    stringJoiner.join(CdsFormatter.makeAlbum(rawEntity), " / ")
                }
                ContentType.PHOTO -> stringJoiner.join(CdsFormatter.makeDate(rawEntity))
                ContentType.CONTAINER -> {
                    stringJoiner.join(CdsFormatter.makeChannel(rawEntity))
                    stringJoiner.join(CdsFormatter.makeDate(rawEntity), ' ')
                }
                else -> {
                }
            }
            return if (stringJoiner.isNotEmpty()) {
                stringJoiner.toString()
            } else ""
        }

    override val artUri: Uri
        get() {
            val uri = rawEntity.getValue(CdsObject.UPNP_ALBUM_ART_URI)
            return if (uri.isNullOrEmpty()) {
                Uri.EMPTY
            } else Uri.parse(uri)
        }

    override val iconUri: Uri
        get() = Uri.EMPTY

    override val resourceCount: Int
        get() = rawEntity.getResourceCount()

    override val isProtected: Boolean
        get() = rawEntity.hasProtectedResource()

    init {
        type = getType(rawEntity)
        selectedRes = rawEntity.getTag(CdsObject.RES)
        updateUri()
    }

    private fun getType(o: CdsObject): ContentType = when (o.type) {
        CdsObject.TYPE_VIDEO -> ContentType.MOVIE
        CdsObject.TYPE_AUDIO -> ContentType.MUSIC
        CdsObject.TYPE_IMAGE -> ContentType.PHOTO
        CdsObject.TYPE_CONTAINER -> ContentType.CONTAINER
        CdsObject.TYPE_UNKNOWN -> ContentType.UNKNOWN
        else -> ContentType.UNKNOWN
    }

    private fun updateUri() {
        val selectedRes = selectedRes
        if (selectedRes == null || selectedRes.value.isEmpty()) {
            uri = null
            mimeType = null
            return
        }
        uri = Uri.parse(selectedRes.value)
        val protocolInfo = selectedRes.getAttribute(CdsObject.PROTOCOL_INFO)
        mimeType = PropertyParser.extractMimeTypeFromProtocolInfo(protocolInfo)
    }

    override fun hasResource(): Boolean = resourceCount != 0

    override fun canDelete(): Boolean =
        rawEntity.getIntValue(CdsObject.RESTRICTED, -1) == 0

    override fun selectResource(index: Int) {
        if (index < 0 || index >= resourceCount) {
            throw IndexOutOfBoundsException()
        }
        selectedRes = rawEntity.getTag(CdsObject.RES, index)
        updateUri()
    }
}
