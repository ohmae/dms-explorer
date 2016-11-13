/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * CdsObjectのファクトリークラス。
 *
 * BrowseDirectChildrenの結果及び
 * BrowseMetadataの結果を
 * CdsObjectに変換して返す。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
final class CdsObjectFactory {
    private static final String TAG = "CdsObjectFactory";

    /**
     * BrowseDirectChildrenの結果をパースしてCdsObjectのリストとして返す。
     *
     * @param xml パースするXML
     * @return パース結果
     */
    @NonNull
    static List<CdsObject> parseDirectChildren(@Nullable String xml) {
        final List<CdsObject> list = new ArrayList<>();
        if (TextUtils.isEmpty(xml)) {
            return list;
        }
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(new InputSource(new StringReader(xml)));
            Node node = doc.getDocumentElement().getFirstChild();
            for (; node != null; node = node.getNextSibling()) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                final CdsObject object = createCdsObject((Element) node);
                if (object != null) {
                    list.add(object);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    /**
     * BrowseMetadataの結果をパースしてCdsObjectのインスタンスを返す。
     *
     * @param xml パースするXML
     * @return パース結果、パースに失敗した場合null
     */
    @Nullable
    static CdsObject parseMetadata(@Nullable String xml) {
        if (TextUtils.isEmpty(xml)) {
            return null;
        }
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(new InputSource(new StringReader(xml)));
            Node node = doc.getDocumentElement().getFirstChild();
            for (; node != null; node = node.getNextSibling()) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                return createCdsObject((Element) node);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(TAG, e);
        }
        return null;
    }

    /**
     * CdsObjectのインスタンスを作成する。
     *
     * @param element CdsObjectを指すElement
     * @return CdsObjectのインスタンス、パースに失敗した場合null
     */
    @Nullable
    private static CdsObject createCdsObject(@NonNull Element element) {
        final String name = element.getNodeName();
        try {
            if (name.equals(CdsObject.ITEM)) {
                return new CdsObject(element);
            } else if (name.equals(CdsObject.CONTAINER)) {
                return new CdsObject(element);
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e);
        }
        return null;
    }
}
