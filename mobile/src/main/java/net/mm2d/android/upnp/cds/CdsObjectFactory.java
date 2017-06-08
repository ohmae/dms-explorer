/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.util.Log;
import net.mm2d.util.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * CdsObjectのファクトリークラス。
 *
 * <p>BrowseDirectChildrenの結果及びBrowseMetadataの結果をCdsObjectに変換して返す。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
final class CdsObjectFactory {
    /**
     * BrowseDirectChildrenの結果をパースしてCdsObjectのリストとして返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果
     */
    @NonNull
    static List<CdsObject> parseDirectChildren(@NonNull final String udn, @Nullable final String xml) {
        final List<CdsObject> list = new ArrayList<>();
        if (TextUtils.isEmpty(xml)) {
            return list;
        }
        try {
            final Document doc = XmlUtils.newDocument(false, xml);
            Node node = doc.getDocumentElement().getFirstChild();
            for (; node != null; node = node.getNextSibling()) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                final CdsObject object = createCdsObject(udn, (Element) node);
                if (object != null) {
                    list.add(object);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(e);
        }
        return list;
    }

    /**
     * BrowseMetadataの結果をパースしてCdsObjectのインスタンスを返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果、パースに失敗した場合null
     */
    @Nullable
    static CdsObject parseMetadata(@NonNull final String udn, @Nullable final String xml) {
        if (TextUtils.isEmpty(xml)) {
            return null;
        }
        try {
            final Document doc = XmlUtils.newDocument(false, xml);
            Node node = doc.getDocumentElement().getFirstChild();
            for (; node != null; node = node.getNextSibling()) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                return createCdsObject(udn, (Element) node);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(e);
        }
        return null;
    }

    /**
     * CdsObjectのインスタンスを作成する。
     *
     * @param udn     MediaServerのUDN
     * @param element CdsObjectを指すElement
     * @return CdsObjectのインスタンス、パースに失敗した場合null
     */
    @Nullable
    private static CdsObject createCdsObject(@NonNull final String udn, @NonNull final Element element) {
        try {
            return new CdsObject(udn, element);
        } catch (final IllegalArgumentException e) {
            Log.w(e);
        }
        return null;
    }
}
