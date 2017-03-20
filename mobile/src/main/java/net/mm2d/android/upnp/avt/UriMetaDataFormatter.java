/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt;

import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.util.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * AVTransport#SetAVTransportURIのCurrentURIMetaDataとして送信するXMLを作成するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class UriMetaDataFormatter {
    /**
     * CdsObjectの情報からXMLを作成する。
     *
     * <p>CdsObjectのXMLをそのまま利用すべきだが
     * 無駄な情報が多いため必要最小限の情報に絞ったデータを作成する。
     * 拡張情報は削除される。
     *
     * @param object CdsObject
     * @return XML
     */
    public static String createUriMetaData(
            final @NonNull CdsObject object, final @NonNull String uri) {
        if (!object.isItem()) {
            return null;
        }
        final int index = findResIndex(object, uri);
        if (index < 0) {
            return null;
        }
        try {
            final Document document = XmlUtils.newDocument(false);
            final Element didl = makeRootElement(document);
            document.appendChild(didl);
            final Element item = makeItemElement(document, object);
            didl.appendChild(item);
            item.appendChild(makeTitleElement(document, object));
            item.appendChild(makeUpnpClassElement(document, object));
            item.appendChild(makeResElement(document, object, index));
            return formatXmlString(document);
        } catch (ParserConfigurationException | TransformerException ignored) {
        }
        return null;
    }

    @NonNull
    private static Element makeRootElement(final @NonNull Document document) {
        final Element element = document.createElement("DIDL-Lite");
        element.setAttribute("xmlns", "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/");
        element.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        element.setAttribute("xmlns:upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/");
        return element;
    }

    @NonNull
    private static Element makeItemElement(
            final @NonNull Document document, final @NonNull CdsObject object) {
        final Element element = document.createElement("item");
        element.setAttribute("id", object.getObjectId());
        element.setAttribute("parentID", object.getParentId());
        element.setAttribute("restricted", object.getValue(CdsObject.RESTRICTED));
        return element;
    }

    @NonNull
    private static Element makeTitleElement(
            final @NonNull Document document, final @NonNull CdsObject object) {
        final Element element = document.createElement("dc:title");
        element.setTextContent(object.getTitle());
        return element;
    }

    @NonNull
    private static Element makeUpnpClassElement(
            final @NonNull Document document, final @NonNull CdsObject object) {
        final Element element = document.createElement("upnp:class");
        element.setTextContent(object.getUpnpClass());
        return element;
    }

    @NonNull
    private static Element makeResElement(
            final @NonNull Document document, final @NonNull CdsObject object, final int index) {
        final Element element = document.createElement("res");
        element.setAttribute(CdsObject.PROTOCOL_INFO, object.getValue(CdsObject.RES_PROTOCOL_INFO, index));
        setElement(element, object, index, CdsObject.RES_DURATION, CdsObject.DURATION);
        setElement(element, object, index, CdsObject.RES_BITRATE, CdsObject.BITRATE);
        setElement(element, object, index, CdsObject.RES_SIZE, CdsObject.SIZE);
        setElement(element, object, index, CdsObject.RES_RESOLUTION, CdsObject.RESOLUTION);
        element.setTextContent(object.getValue(CdsObject.RES, index));
        return element;
    }

    private static void setElement(
            final @NonNull Element element, final @NonNull CdsObject object, final int index,
            final @NonNull String xpath, final @NonNull String name) {
        final String value = object.getValue(xpath, index);
        if (value != null) {
            element.setAttribute(name, value);
        }
    }

    private static int findResIndex(final @NonNull CdsObject object, final @NonNull String uri) {
        final List<Tag> list = object.getTagList(CdsObject.RES);
        if (list == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            if (uri.equals(list.get(i).getValue())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * XML Documentを文字列に変換する
     *
     * @param document 変換するXML Document
     * @return 変換された文字列
     * @throws TransformerException 変換処理に問題が発生した場合
     */
    @Nonnull
    private static String formatXmlString(final @Nonnull Document document)
            throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        final StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }
}
