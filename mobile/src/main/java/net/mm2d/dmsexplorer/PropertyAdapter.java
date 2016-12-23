/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 詳細情報の各項目をRecyclerViewを使用して表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PropertyAdapter
        extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    public interface OnItemLinkClickListener {
        void onItemLinkClick(String link);
    }

    private static class Entry {
        private final String mName;
        private final String mValue;
        private final boolean mLink;
        private final boolean mAutoLink;

        Entry(String name, String value, boolean link, boolean auto) {
            mName = name;
            mValue = value;
            mLink = link;
            mAutoLink = auto;
        }

        String getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }

        boolean isLink() {
            return mLink;
        }

        boolean isAutoLink() {
            return mAutoLink;
        }
    }

    private final List<Entry> mList;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private OnItemLinkClickListener mListener;

    public PropertyAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mList = new ArrayList<>();
    }

    public void addEntry(String name, String value) {
        addEntry(name, value, false);
    }

    public void addEntry(String name, String value, boolean link) {
        if (value == null || value.isEmpty()) {
            return;
        }
        mList.add(new Entry(name, value, link, false));
    }

    public void addEntryAutoLink(String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        mList.add(new Entry(name, value, true, true));
    }

    public void setOnItemLinkClickListener(OnItemLinkClickListener l) {
        mListener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.li_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.applyItem(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class LinkSpan extends ClickableSpan {
        private final String mLink;

        LinkSpan(String link) {
            mLink = link;
        }

        @Override
        public void onClick(View widget) {
            if (mListener != null) {
                mListener.onItemLinkClick(mLink);
            }
        }
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mText1;
        private final TextView mText2;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mText1 = (TextView) mView.findViewById(R.id.text1);
            mText2 = (TextView) mView.findViewById(R.id.text2);
            mText2.setMovementMethod(LinkMovementMethod.getInstance());
        }

        void applyItem(Entry entry) {
            mText1.setText(entry.getName());
            final String value = entry.getValue();
            if (entry.isAutoLink()) {
                mText2.setText(makeLinkString(value));
            } else if (entry.isLink()) {
                final SpannableString ss = new SpannableString(value);
                ss.setSpan(new LinkSpan(value), 0, value.length(), Spanned.SPAN_MARK_POINT);
                mText2.setText(ss);
            } else {
                mText2.setText(value);
            }
        }

        private SpannableString makeLinkString(String string) {
            final SpannableString ss = new SpannableString(string);
            final Matcher matcher = URL_PATTERN.matcher(string);
            while (matcher.find()) {
                final int start = matcher.start();
                final int end = matcher.end();
                ss.setSpan(new LinkSpan(string.substring(start, end)),
                        start, end, Spanned.SPAN_MARK_POINT);
            }
            return ss;
        }
    }
}
