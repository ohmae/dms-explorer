/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.PropertyListItemBinding;
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter.ViewHolder;
import net.mm2d.dmsexplorer.viewmodel.PropertyItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 詳細情報の各項目をRecyclerViewを使用して表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PropertyAdapter extends Adapter<ViewHolder> {
    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w/:%#$&?()~.=+\\-]+");

    public enum Type {
        TITLE,
        TEXT,
        LINK,
        DESCRIPTION
    }

    public static class Entry {
        private final String mName;
        private final String mValue;
        private final Type mType;

        private Entry(
                @NonNull final String name,
                @NonNull final String value,
                @NonNull final Type type) {
            mName = name;
            mValue = value;
            mType = type;
        }

        @NonNull
        public String getName() {
            return mName;
        }

        @NonNull
        public String getValue() {
            return mValue;
        }

        @NonNull
        public Type getType() {
            return mType;
        }
    }

    private final Context mContext;
    private final List<Entry> mList;
    private final LayoutInflater mInflater;

    public PropertyAdapter(@NonNull final Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
    }

    protected Context getContext() {
        return mContext;
    }

    public void addEntry(
            @NonNull final String name,
            @Nullable final String value) {
        addEntry(name, value, Type.TEXT);
    }

    public void addEntry(
            @NonNull final String name,
            @Nullable final String value,
            @NonNull final Type type) {
        if (value == null || value.isEmpty()) {
            return;
        }
        mList.add(new Entry(name, value, type));
    }

    public void addTitleEntry(@NonNull final String name) {
        mList.add(new Entry(name, "", Type.TITLE));
    }

    @Override
    public int getItemViewType(final int position) {
        return mList.get(position).getType() != Type.DESCRIPTION ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            int viewType) {
        return new ViewHolder(getContext(), mInflater,
                DataBindingUtil.inflate(mInflater, R.layout.property_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(
            @NonNull final ViewHolder holder,
            int position) {
        holder.applyItem(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final PropertyListItemBinding mBinding;

        ViewHolder(
                @NonNull final Context context,
                @NonNull final LayoutInflater inflater,
                @NonNull final PropertyListItemBinding binding) {
            super(binding.getRoot());
            mContext = context;
            mInflater = inflater;
            mBinding = binding;
        }

        void applyItem(@NonNull final Entry entry) {
            String value = entry.getValue();
            if (entry.getType() == Type.DESCRIPTION) {
                value = setUpDescription(entry);
            }
            mBinding.setModel(new PropertyItemModel(entry.getName(), entry.getType(), value, this));
            mBinding.executePendingBindings();
        }

        private String setUpDescription(@NonNull final Entry entry) {
            final LinearLayout layout = mBinding.container;
            final int count = layout.getChildCount();
            for (int i = count - 1; i >= 2; i--) {
                layout.removeViewAt(i);
            }
            return makeDescription(layout, entry.getValue());
        }

        private String makeDescription(
                @NonNull final ViewGroup parent,
                @NonNull final String text) {
            String firstNormalText = "";
            final Matcher matcher = URL_PATTERN.matcher(text);
            int lastEnd = 0;
            while (matcher.find()) {
                final int start = matcher.start();
                final int end = matcher.end();
                if (start != lastEnd) {
                    final String normalText = trim(text.substring(lastEnd, start));
                    if (!normalText.isEmpty()) {
                        if (lastEnd == 0) {
                            firstNormalText = normalText;
                        } else {
                            addNormalText(parent, normalText);
                        }
                    }
                }
                addLinkText(parent, trim(text.substring(start, end)));
                lastEnd = end;
            }
            if (lastEnd == 0) {
                return trim(text);
            } else if (lastEnd != text.length()) {
                final String normalText = trim(text.substring(lastEnd, text.length()));
                if (!normalText.isEmpty()) {
                    addNormalText(parent, normalText);
                }
            }
            return firstNormalText;
        }

        private void addNormalText(
                @NonNull final ViewGroup parent,
                @NonNull final String text) {
            final TextView normal = (TextView) mInflater.inflate(R.layout.normal_text_view, parent, false);
            normal.setText(text);
            parent.addView(normal);
        }

        private void addLinkText(
                @NonNull final ViewGroup parent,
                @NonNull final String text) {
            final TextView link = (TextView) mInflater.inflate(R.layout.link_text_view, parent, false);
            link.setText(text);
            link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            link.setOnClickListener(this);
            parent.addView(link);
        }

        @Override
        public void onClick(final View v) {
            if (!(v instanceof TextView)) {
                return;
            }
            final CharSequence text = ((TextView) v).getText();
            if (TextUtils.isEmpty(text)) {
                return;
            }
            Repository.get().getOpenUriModel().openUri(mContext, text.toString());
        }
    }

    private static String trim(String str) {
        int len = str.length();
        int st = 0;
        while ((st < len) && isSpace(str.charAt(st))) {
            st++;
        }
        while ((st < len) && isSpace(str.charAt(len - 1))) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }

    private static boolean isSpace(final char c) {
        return c <= '\u0020' || c == '\u00A0' || c == '\u3000';
    }
}
