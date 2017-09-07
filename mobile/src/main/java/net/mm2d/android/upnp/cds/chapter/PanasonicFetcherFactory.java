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
import net.mm2d.upnp.HttpClient;
import net.mm2d.util.Log;
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

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class PanasonicFetcherFactory implements FetcherFactory {
    private static final String CHAPTER_INFO = "res@pxn:ChapterList";
    private static final String ROOT_NODE = "result";
    private static final String LIST_NODE = "chapterList";
    private static final String ITEM_NODE = "item";
    private static final String TIME_NODE = "timeCode";

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
            final Element point = findChildElementByNodeName(node, TIME_NODE);
            if (point == null || TextUtils.isEmpty(point.getTextContent())) {
                continue;
            }
            try {
                result.add(parseTimeCode(point.getTextContent()));
            } catch (final IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    @Nullable
    private Element findChildElementByNodeName(
            @NonNull final Node parent,
            @NonNull final String nodeName) {
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

    private int parseTimeCode(@NonNull final String timeCode) {
        final String[] times = timeCode.split(":");
        if (times.length != 3) {
            throw new IllegalArgumentException();
        }
        try {
            return (int) TimeUnit.HOURS.toMillis(Integer.parseInt(times[0]))
                    + (int) TimeUnit.MINUTES.toMillis(Integer.parseInt(times[1]))
                    + (int) (Float.parseFloat(times[2]) * 1000);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
