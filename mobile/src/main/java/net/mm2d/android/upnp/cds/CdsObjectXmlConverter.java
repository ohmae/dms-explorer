/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.util.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

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
public class CdsObjectXmlConverter {
    /**
     * CdsObjectの情報からXMLを作成する。
     *
     * @param object CdsObject
     * @return XML
     */
    @Nullable
    public static String convert(@NonNull final CdsObject object) {
        if (!object.isItem()) {
            return null;
        }
        try {
            final Document document = XmlUtils.newDocument(false);
            final Element didl = makeRootElement(document, object);
            document.appendChild(didl);
            final Element item = makeItemElement(document, object);
            didl.appendChild(item);
            for (final Map.Entry<String, List<Tag>> tagListEntry : object.getTagMap().getRawMap().entrySet()) {
                final String key = tagListEntry.getKey();
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                for (final Tag tag : tagListEntry.getValue()) {
                    item.appendChild(makeElement(document, key, tag));
                }
            }
            return formatXmlString(document);
        } catch (ParserConfigurationException | TransformerException | IllegalArgumentException ignored) {
        }
        return null;
    }

    @NonNull
    private static Element makeElement(
            @NonNull final Document document,
            @NonNull final String tagName,
            @NonNull final Tag tag) {
        final Element element = document.createElement(tagName);
        final String value = tag.getValue();
        if (!TextUtils.isEmpty(value)) {
            element.setTextContent(value);
        }
        for (final Map.Entry<String, String> attribute : tag.getAttributes().entrySet()) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }
        return element;
    }

    @NonNull
    private static Element makeRootElement(
            @NonNull final Document document,
            @NonNull final CdsObject object) {
        final Element element = document.createElement(CdsObject.DIDL_LITE);
        for (final Map.Entry<String, String> attribute : object.getRootTag().getAttributes().entrySet()) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }
        return element;
    }

    @NonNull
    private static Element makeItemElement(
            @NonNull final Document document,
            @NonNull final CdsObject object) {
        final Tag tag = object.getTag("");
        if (tag == null) {
            throw new IllegalArgumentException();
        }
        return makeElement(document, CdsObject.ITEM, tag);
    }

    /**
     * XML Documentを文字列に変換する
     *
     * @param document 変換するXML Document
     * @return 変換された文字列
     * @throws TransformerException 変換処理に問題が発生した場合
     */
    @NonNull
    private static String formatXmlString(final @NonNull Document document)
            throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        final StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }
}
