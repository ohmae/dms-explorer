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

import net.mm2d.upnp.HttpClient;
import net.mm2d.util.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ChapterInfo {
    private static final String SONY_CHAPTER_INFO = "av:chapterInfo";
    private static final String SONY_ROOT_NODE = "contentInfo";
    private static final String SONY_LIST_NODE = "content_chapter_info";
    private static final String SONY_CHAPTER_NODE = "chapter";
    private static final String SONY_POINT_NODE = "chapter_point";

    public interface Callback {
        void onResult(@NonNull List<Integer> result);
    }

    public static void get(@NonNull final CdsObject object, @NonNull final Callback callback) {
        getSony(object, callback);
    }

    private static boolean getSony(@NonNull final CdsObject object, @NonNull final Callback callback) {
        final String url = object.getValue(SONY_CHAPTER_INFO);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        new Thread(() -> getSonyInner(url, callback)).start();
        return true;
    }

    private static void getSonyInner(@NonNull final String url, @NonNull final Callback callback) {
        try {
            final String xml = new HttpClient(false).downloadString(new URL(url));
            callback.onResult(parseSonyChapterInfo(xml));
        } catch (IOException | ParserConfigurationException | SAXException ignored) {
            callback.onResult(Collections.emptyList());
        }
    }

    @NonNull
    private static List<Integer> parseSonyChapterInfo(@NonNull final String xml)
            throws ParserConfigurationException, SAXException, IOException {
        if (TextUtils.isEmpty(xml)) {
            return Collections.emptyList();
        }
        final Element root = XmlUtils.newDocument(false, xml).getDocumentElement();
        if (root == null || !root.getNodeName().equals(SONY_ROOT_NODE)) {
            return Collections.emptyList();
        }
        final Element content = findChildElementByNodeName(root, SONY_LIST_NODE);
        if (content == null) {
            return Collections.emptyList();
        }
        final List<Integer> result = new ArrayList<>();
        for (Node node = content.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE
                    || !node.getNodeName().equals(SONY_CHAPTER_NODE)) {
                continue;
            }
            final Element point = findChildElementByNodeName(node, SONY_POINT_NODE);
            if (point == null || TextUtils.isEmpty(point.getTextContent())) {
                continue;
            }
            try {
                final float value = Float.parseFloat(point.getTextContent());
                result.add((int) (value * 1000));
            } catch (final NumberFormatException ignored) {
            }
        }
        return result;
    }

    @Nullable
    private static Element findChildElementByNodeName(
            @NonNull final Node parent, @NonNull final String nodeName) {
        Node child = parent.getFirstChild();
        for (; child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (nodeName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
}
