/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

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
 * BrowseDirectChildrenの結果及び
 * BrowseMetadataの結果を
 * CdsObjectに変換して返す。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
final class CdsObjectFactory {
    private static final String TAG = "CdsObjectFactory";

    static List<CdsObject> parseDirectChildren(String xml) {
        final List<CdsObject> list = new ArrayList<>();
        if (xml == null || xml.isEmpty()) {
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
                final CdsObject object = newCdsObject((Element) node);
                if (object != null) {
                    list.add(object);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    static CdsObject parseMetadata(String xml) {
        if (xml == null || xml.isEmpty()) {
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
                return newCdsObject((Element) node);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.w(TAG, e);
        }
        return null;
    }

    private static CdsObject newCdsObject(Element element) {
        final String name = element.getNodeName();
        if (name.equals(CdsObject.ITEM)) {
            return new CdsObject(element);
        } else if (name.equals(CdsObject.CONTAINER)) {
            return new CdsObject(element);
        }
        return null;
    }
}
