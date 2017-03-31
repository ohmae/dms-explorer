/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.adapter.property.DescriptionFormatter;
import net.mm2d.dmsexplorer.adapter.property.LinkFormatter;
import net.mm2d.dmsexplorer.adapter.property.PropertyFormatter;
import net.mm2d.dmsexplorer.adapter.property.TextFormatter;
import net.mm2d.dmsexplorer.databinding.PropertyListItemBinding;
import net.mm2d.dmsexplorer.model.PropertyItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 詳細情報の各項目をRecyclerViewを使用して表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PropertyAdapter
        extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    public static final String TITLE_PREFIX = "##";
    enum Type {
        TEXT(new TextFormatter()),
        LINK(new LinkFormatter()),
        DESCRIPTION(new DescriptionFormatter());

        private final PropertyFormatter mPropertyFormatter;
        Type(PropertyFormatter formatter) {
            mPropertyFormatter = formatter;
        }

        @NonNull
        CharSequence format(@NonNull Context context, @NonNull String string) {
            return mPropertyFormatter.format(context, string);
        }
    }

    private static class Entry {
        private final String mName;
        private final String mValue;
        private final Type mType;

        Entry(String name, String value, Type type) {
            mName = name;
            mValue = value;
            mType = type;
        }

        String getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }

        CharSequence getFormatValue(Context context) {
            return mType.format(context, mValue);
        }
    }

    private final Context mContext;
    private final List<Entry> mList;
    private final LayoutInflater mInflater;

    public PropertyAdapter(@NonNull Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
    }

    protected Context getContext() {
        return mContext;
    }

    public void addEntry(String name, String value) {
        addEntry(name, value, Type.TEXT);
    }

    public void addEntry(String name, String value, Type type) {
        if (value == null || value.isEmpty()) {
            return;
        }
        mList.add(new Entry(name, value, type));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil
                .inflate(mInflater, R.layout.property_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.applyItem(getContext(), mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final PropertyListItemBinding mBinding;

        ViewHolder(PropertyListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        void applyItem(Context context, Entry entry) {
            mBinding.setModel(new PropertyItemModel(entry.getName(), entry.getFormatValue(context)));
            mBinding.executePendingBindings();
        }
    }
}
