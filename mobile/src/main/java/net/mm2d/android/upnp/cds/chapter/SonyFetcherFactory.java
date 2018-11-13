/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.text.TextUtils;

import net.mm2d.android.upnp.cds.CdsObject;
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
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class SonyFetcherFactory implements FetcherFactory {
    private static final String CHAPTER_INFO = "av:chapterInfo";
    private static final String ROOT_NODE = "contentInfo";
    private static final String LIST_NODE = "content_chapter_info";
    private static final String ITEM_NODE = "chapter";
    private static final String TIME_NODE = "chapter_point";

    @Nullable
    @Override
    public Single<List<Integer>> create(@NonNull final CdsObject object) {
        final String url = object.getValue(CHAPTER_INFO);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return Single.create((SingleOnSubscribe<List<Integer>>) emitter -> {
            try {
                final String xml = new HttpClient(false).downloadString(new URL(url));
                emitter.onSuccess(parseChapterInfo(xml));
            } catch (IOException | ParserConfigurationException | SAXException ignored) {
                emitter.onSuccess(Collections.emptyList());
            }
        }).subscribeOn(Schedulers.io());
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
            final Element timeNode = findChildElementByNodeName(node, TIME_NODE);
            if (timeNode == null || TextUtils.isEmpty(timeNode.getTextContent())) {
                continue;
            }
            try {
                result.add(parseTimeNode(timeNode.getTextContent()));
            } catch (final IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    @Nullable
    private Element findChildElementByNodeName(
            @NonNull final Node parent,
            @NonNull final String nodeName) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (nodeName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    /**
     * 時間を表現する文字列をミリ秒に変換する。
     *
     * <p>フォーマット：0.000
     *
     * @param timeNode 時間文字列
     * @return ミリ秒
     */
    private int parseTimeNode(@NonNull final String timeNode) {
        try {
            return (int) (Float.parseFloat(timeNode) * TimeUnit.SECONDS.toMillis(1));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
