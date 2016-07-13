/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.os.Parcel;
import android.os.Parcelable;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class Tag implements Parcelable {
    private final String mValue;
    private final Map<String, String> mAttribute;

    Tag(Element element) {
        this(element, false);
    }

    Tag(Element element, boolean root) {
        this(element, root ? "" : element.getTextContent());
    }

    private Tag(Element element, String value) {
        mValue = value;
        final NamedNodeMap attributes = element.getAttributes();
        final int size = attributes.getLength();
        if (size == 0) {
            mAttribute = Collections.emptyMap();
        } else {
            mAttribute = new LinkedHashMap<>(size);
        }
        for (int i = 0; i < size; i++) {
            final Node attr = attributes.item(i);
            mAttribute.put(attr.getNodeName(), attr.getNodeValue());
        }
    }

    public String getValue() {
        return mValue;
    }

    public String getAttribute(String name) {
        return mAttribute.get(name);
    }

    public Map<String, String> getAttributes() {
        if (mAttribute.size() == 0) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(mAttribute);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(mValue);
        for (final Entry<String, String> entry : mAttribute.entrySet()) {
            sb.append("\n");
            sb.append("@");
            sb.append(entry.getKey());
            sb.append(" => ");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    protected Tag(Parcel in) {
        mValue = in.readString();
        final int size = in.readInt();
        if (size == 0) {
            mAttribute = Collections.emptyMap();
        } else {
            mAttribute = new LinkedHashMap<>(size);
            for (int i = 0; i < size; i++) {
                final String name = in.readString();
                final String value = in.readString();
                mAttribute.put(name, value);
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mValue);
        dest.writeInt(mAttribute.size());
        for (final Entry<String, String> entry : mAttribute.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
