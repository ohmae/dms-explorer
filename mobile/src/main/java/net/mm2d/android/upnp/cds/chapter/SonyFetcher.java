/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.chapter.ChapterList.Callback;
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
class SonyFetcher implements Fetcher {
    private static final String CHAPTER_INFO = "av:chapterInfo";
    private static final String ROOT_NODE = "contentInfo";
    private static final String LIST_NODE = "content_chapter_info";
    private static final String ITEM_NODE = "chapter";
    private static final String TIME_NODE = "chapter_point";

    @Override
    public boolean get(@NonNull final CdsObject object, @NonNull final Callback callback) {
        final String url = object.getValue(CHAPTER_INFO);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        new Thread(() -> getInner(url, callback)).start();
        return true;
    }

    private void getInner(@NonNull final String url, @NonNull final Callback callback) {
        try {
            final String xml = new HttpClient(false).downloadString(new URL(url));
            callback.onResult(parseChapterInfo(xml));
        } catch (IOException | ParserConfigurationException | SAXException ignored) {
            callback.onResult(Collections.emptyList());
        }
    }

    @NonNull
    private List<Integer> parseChapterInfo(@NonNull final String xml)
            throws ParserConfigurationException, SAXException, IOException {
        if (TextUtils.isEmpty(xml)) {
            return Collections.emptyList();
        }
        final Element root = XmlUtils.newDocument(false, xml).getDocumentElement();
        if (root == null || !root.getNodeName().equals(ROOT_NODE)) {
            return Collections.emptyList();
        }
        final Element content = findChildElementByNodeName(root, LIST_NODE);
        if (content == null) {
            return Collections.emptyList();
        }
        final List<Integer> result = new ArrayList<>();
        for (Node node = content.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE
                    || !node.getNodeName().equals(ITEM_NODE)) {
                continue;
            }
            final Element point = findChildElementByNodeName(node, TIME_NODE);
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
    private Element findChildElementByNodeName(
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
