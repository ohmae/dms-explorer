/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                final String link = (String) v.getTag();
                mListener.onItemLinkClick(link);
            }
        }
    };

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
        }

        void applyItem(Entry entry) {
            final int defaultColor = ContextCompat.getColor(mContext, R.color.textMain);
            final int linkColor = ContextCompat.getColor(mContext, R.color.textLink);
            mText1.setText(entry.getName());
            final String value = entry.getValue();
            if (entry.isAutoLink()) {
                final SpannableString ss = new SpannableString(value);
                final Matcher matcher = URL_PATTERN.matcher(value);
                while (matcher.find()) {
                    final int start = matcher.start();
                    final int end = matcher.end();
                    final String link = value.substring(start, end);
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(linkColor);
                            ds.setUnderlineText(true);
                        }

                        @Override
                        public void onClick(View widget) {
                            if (mListener != null) {
                                mListener.onItemLinkClick(link);
                            }
                        }
                    }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                mText2.setTextColor(defaultColor);
                mText2.setText(ss);
                mText2.setMovementMethod(LinkMovementMethod.getInstance());
                mView.setTag(null);
                mView.setOnClickListener(null);
            } else if (entry.isLink()) {
                final SpannableString ss = new SpannableString(value);
                final UnderlineSpan us = new UnderlineSpan();
                ss.setSpan(us, 0, value.length(), ss.getSpanFlags(us));
                mText2.setTextColor(linkColor);
                mText2.setText(ss);
                mView.setTag(value);
                mView.setOnClickListener(mOnClickListener);
            } else {
                mText2.setTextColor(defaultColor);
                mText2.setText(value);
                mView.setTag(null);
                mView.setOnClickListener(null);
            }
        }
    }
}
