package net.mm2d.android.upnp.cds.chapter

import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.upnp.HttpClient
import net.mm2d.upnp.util.XmlUtils
import net.mm2d.upnp.util.siblingElements
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import javax.xml.parsers.ParserConfigurationException

class SharpFetcherFactory : FetcherFactory {
    override fun create(cdsObject: CdsObject): Single<List<Int>>? {
        val url = cdsObject.getValue(CHAPTER_INFO)
        if (url.isNullOrEmpty()) {
            return null
        }
        return Single.create { emitter: SingleEmitter<List<Int>> ->
            try {
                val xml = HttpClient(false).downloadString(URL(url))
                emitter.onSuccess(parseChapterInfo(xml))
            } catch (ignored: Exception) {
                emitter.onSuccess(emptyList())
            }
        }.subscribeOn(Schedulers.io())
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    private fun parseChapterInfo(xml: String): List<Int> {
        if (xml.isEmpty()) {
            return emptyList()
        }
        val root = XmlUtils.newDocument(false, xml).documentElement
        if (root == null || root.nodeName != ROOT_NODE) {
            return emptyList()
        }
        return root.firstChild.siblingElements()
            .filter { it.nodeName == ITEM_NODE }
            .mapNotNull { it.textContent.parseTimeNode() }
    }

    /**
     * 時間を表現する文字列をミリ秒に変換する。
     *
     * フォーマット：00:00:00.000
     *
     * @receiver 時間文字列
     * @return ミリ秒
     */
    private fun String?.parseTimeNode(): Int? {
        if (this.isNullOrEmpty()) return null
        val times = this.split(':')
        if (times.size != 3) return null
        val hours = times[0].toIntOrNull() ?: return null
        val minutes = times[1].toIntOrNull() ?: return null
        val seconds = times[2].toFloatOrNull() ?: return null
        return (hours * ONE_HOUR + minutes * ONE_MINUTE + seconds * ONE_SECOND).toInt()
    }

    companion object {
        private const val CHAPTER_INFO = "shp:chapterList"
        private const val ROOT_NODE = "chapterList"
        private const val ITEM_NODE = "chapter"
        private const val ONE_SECOND = 1000
        private const val ONE_MINUTE = 60_000
        private const val ONE_HOUR = 3600_000
    }
}
