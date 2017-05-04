/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.PropertyListItemBinding;
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter.ViewHolder;
import net.mm2d.dmsexplorer.view.adapter.property.DescriptionFormatter;
import net.mm2d.dmsexplorer.view.adapter.property.LinkFormatter;
import net.mm2d.dmsexplorer.view.adapter.property.PropertyFormatter;
import net.mm2d.dmsexplorer.view.adapter.property.TextFormatter;
import net.mm2d.dmsexplorer.viewmodel.PropertyItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 詳細情報の各項目をRecyclerViewを使用して表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PropertyAdapter extends Adapter<ViewHolder> {
    public static final String TITLE_PREFIX = "##";

    enum Type {
        TEXT(new TextFormatter()),
        LINK(new LinkFormatter()),
        DESCRIPTION(new DescriptionFormatter());

        @NonNull
        private final PropertyFormatter mPropertyFormatter;

        Type(@NonNull final PropertyFormatter formatter) {
            mPropertyFormatter = formatter;
        }

        @NonNull
        private CharSequence format(@NonNull final Context context, @NonNull final String string) {
            return mPropertyFormatter.format(context, string);
        }
    }

    private static class Entry {
        private final String mName;
        private final String mValue;
        private final Type mType;

        private Entry(@NonNull final String name, @NonNull final String value, @NonNull final Type type) {
            mName = name;
            mValue = value;
            mType = type;
        }

        private String getName() {
            return mName;
        }

        private String getValue() {
            return mValue;
        }

        @NonNull
        private CharSequence getFormatValue(Context context) {
            return mType.format(context, mValue);
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

    public void addEntry(@NonNull final String name, @Nullable final String value) {
        addEntry(name, value, Type.TEXT);
    }

    public void addEntry(@NonNull final String name, @Nullable final String value, @NonNull final Type type) {
        if (value == null || value.isEmpty()) {
            return;
        }
        mList.add(new Entry(name, value, type));
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil
                .inflate(mInflater, R.layout.property_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.applyItem(getContext(), mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final PropertyListItemBinding mBinding;

        ViewHolder(@NonNull final PropertyListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        void applyItem(@NonNull final Context context, @NonNull final Entry entry) {
            mBinding.setModel(new PropertyItemModel(entry.getName(), entry.getFormatValue(context)));
            mBinding.executePendingBindings();
        }
    }
}
