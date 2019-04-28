/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.text.TextUtils
import net.mm2d.upnp.util.XmlUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * AVTransport#SetAVTransportURIのCurrentURIMetaDataとして送信するXMLを作成するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object CdsObjectXmlConverter {
    /**
     * CdsObjectの情報からXMLを作成する。
     *
     * @param cdsObject CdsObject
     * @return XML
     */
    fun convert(cdsObject: CdsObject): String? {
        if (!cdsObject.isItem) {
            return null
        }
        try {
            val document = XmlUtils.newDocument(false)
            val didl = makeRootElement(document, cdsObject)
            document.appendChild(didl)
            val item = makeItemElement(document, cdsObject)
            didl.appendChild(item)
            for ((key, value) in cdsObject.tagMap.rawMap) {
                if (TextUtils.isEmpty(key)) {
                    continue
                }
                for (tag in value) {
                    item.appendChild(makeElement(document, key, tag))
                }
            }
            return formatXmlString(document)
        } catch (ignored: ParserConfigurationException) {
        } catch (ignored: TransformerException) {
        } catch (ignored: IllegalArgumentException) {
        }

        return null
    }

    private fun makeElement(
        document: Document,
        tagName: String,
        tag: Tag
    ): Element {
        val element = document.createElement(tagName)
        val value = tag.value
        if (!TextUtils.isEmpty(value)) {
            element.textContent = value
        }
        for ((key, value1) in tag.attributes) {
            element.setAttribute(key, value1)
        }
        return element
    }

    private fun makeRootElement(
        document: Document,
        `object`: CdsObject
    ): Element {
        val element = document.createElement(CdsObject.DIDL_LITE)
        for ((key, value) in `object`.rootTag.attributes) {
            element.setAttribute(key, value)
        }
        return element
    }

    private fun makeItemElement(
        document: Document,
        cdsObject: CdsObject
    ): Element {
        val tag = cdsObject.getTag("") ?: throw IllegalArgumentException()
        return makeElement(document, CdsObject.ITEM, tag)
    }

    /**
     * XML Documentを文字列に変換する
     *
     * @param document 変換するXML Document
     * @return 変換された文字列
     * @throws TransformerException 変換処理に問題が発生した場合
     */
    @Throws(TransformerException::class)
    private fun formatXmlString(document: Document): String {
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        val sw = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(sw))
        return sw.toString()
    }
}
