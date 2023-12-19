/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import net.mm2d.log.Logger
import net.mm2d.upnp.util.XmlUtils
import net.mm2d.upnp.util.siblingElements
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import javax.xml.parsers.ParserConfigurationException

/**
 * CdsObjectのファクトリークラス。
 *
 * BrowseDirectChildrenの結果及びBrowseMetadataの結果をCdsObjectに変換して返す。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object CdsObjectFactory {
    /**
     * BrowseDirectChildrenの結果をパースしてCdsObjectのリストとして返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果
     */
    fun parseDirectChildren(
        udn: String,
        xml: String?,
    ): List<CdsObject> {
        if (xml.isNullOrEmpty()) {
            return emptyList()
        }
        try {
            val document = XmlUtils.newDocument(false, xml)
            val rootTag = createRootTag(document)
            val firstChild = document.documentElement.firstChild ?: return emptyList()
            return firstChild.siblingElements()
                .mapNotNull { createCdsObject(udn, it, rootTag) }
        } catch (e: ParserConfigurationException) {
            Logger.w(e)
        } catch (e: SAXException) {
            Logger.w(e)
        } catch (e: IOException) {
            Logger.w(e)
        }
        return emptyList()
    }

    /**
     * BrowseMetadataの結果をパースしてCdsObjectのインスタンスを返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果、パースに失敗した場合null
     */
    fun parseMetadata(
        udn: String,
        xml: String?,
    ): CdsObject? {
        if (xml.isNullOrEmpty()) {
            return null
        }
        try {
            val document = XmlUtils.newDocument(false, xml)
            val rootTag = createRootTag(document)
            val firstChild = document.documentElement.firstChild ?: return null
            return firstChild.siblingElements().firstOrNull()
                ?.let { createCdsObject(udn, it, rootTag) }
        } catch (e: ParserConfigurationException) {
            Logger.w(e)
        } catch (e: SAXException) {
            Logger.w(e)
        } catch (e: IOException) {
            Logger.w(e)
        }
        return null
    }

    private fun createRootTag(doc: Document): Tag =
        Tag.create(doc.documentElement, true)

    /**
     * CdsObjectのインスタンスを作成する。
     *
     * @param udn     MediaServerのUDN
     * @param element CdsObjectを指すElement
     * @param rootTag DIDL-Liteノードに記載されたNamespace情報
     * @return CdsObjectのインスタンス、パースに失敗した場合null
     */
    private fun createCdsObject(
        udn: String,
        element: Element,
        rootTag: Tag,
    ): CdsObject? = try {
        CdsObjectImpl.create(udn, element, rootTag)
    } catch (e: IllegalArgumentException) {
        Logger.w(e)
        null
    }
}
