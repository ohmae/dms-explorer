/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter

import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.upnp.HttpClient
import net.mm2d.upnp.util.XmlUtils
import net.mm2d.upnp.util.forEachElement
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.ParserConfigurationException

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class PanasonicFetcherFactory : FetcherFactory {
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
        val content = root.findChildElementByNodeName(LIST_NODE) ?: return emptyList()
        val result = ArrayList<Int>()
        content.firstChild.forEachElement { element ->
            if (element.nodeName == ITEM_NODE) {
                element.findChildElementByNodeName(TIME_NODE)
                    ?.textContent
                    ?.parseTimeNode()
                    ?.let { result.add(it) }
            }
        }
        return result
    }

    private fun Node.findChildElementByNodeName(localName: String): Element? {
        firstChild?.forEachElement {
            if (localName == it.nodeName) {
                return it
            }
        }
        return null
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
        private const val CHAPTER_INFO = "res@pxn:ChapterList"
        private const val ROOT_NODE = "result"
        private const val LIST_NODE = "chapterList"
        private const val ITEM_NODE = "item"
        private const val TIME_NODE = "timeCode"
        private val ONE_SECOND = TimeUnit.SECONDS.toMillis(1)
        private val ONE_MINUTE = TimeUnit.MINUTES.toMillis(1)
        private val ONE_HOUR = TimeUnit.HOURS.toMillis(1)
    }
}
