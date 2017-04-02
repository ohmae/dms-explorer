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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.CdsListItemBinding;
import net.mm2d.dmsexplorer.model.CdsItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CDSのコンテンツリストをRecyclerViewへ表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsListAdapter
        extends RecyclerView.Adapter<CdsListAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull View v, @NonNull CdsObject object);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(@NonNull View v, @NonNull CdsObject object);
    }

    private static final OnItemClickListener ON_ITEM_CLICK_LISTENER = (v, object) -> {
    };
    private static final OnItemLongClickListener ON_ITEM_LONG_CLICK_LISTENER = (v, object) -> {
    };
    @NonNull
    private final LayoutInflater mInflater;
    @NonNull
    private final List<CdsObject> mList = new ArrayList<>();
    @NonNull
    private OnItemClickListener mClickListener = ON_ITEM_CLICK_LISTENER;
    @NonNull
    private OnItemLongClickListener mLongClickListener = ON_ITEM_LONG_CLICK_LISTENER;
    private CdsObject mSelectedObject;

    public CdsListAdapter(@NonNull final Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(DataBindingUtil
                .inflate(mInflater, R.layout.cds_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.applyItem(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(@Nullable final OnItemClickListener listener) {
        mClickListener = listener != null ? listener : ON_ITEM_CLICK_LISTENER;
    }

    public void setOnItemLongClickListener(@Nullable final OnItemLongClickListener listener) {
        mLongClickListener = listener != null ? listener : ON_ITEM_LONG_CLICK_LISTENER;
    }

    public void clear() {
        mList.clear();
    }

    public void addAll(@NonNull final Collection<? extends CdsObject> objects) {
        mList.addAll(objects);
    }

    public int add(@NonNull final CdsObject obj) {
        mList.add(obj);
        return mList.size() - 1;
    }

    public int remove(@NonNull final CdsObject obj) {
        final int position = mList.indexOf(obj);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public void setSelectedObject(@Nullable final CdsObject object) {
        if (mSelectedObject != null && mSelectedObject.equals(object)) {
            return;
        }
        final CdsObject previous = mSelectedObject;
        mSelectedObject = object;
        notifyItemChangedIfPossible(previous);
        notifyItemChangedIfPossible(object);
    }

    private void notifyItemChangedIfPossible(@Nullable final CdsObject object) {
        if (object == null) {
            return;
        }
        final int position = mList.indexOf(object);
        if (position < 0) {
            return;
        }
        notifyItemChanged(position);
    }

    public void clearSelectedObject() {
        setSelectedObject(null);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CdsListItemBinding mBinding;
        private CdsObject mObject;

        ViewHolder(@NonNull final CdsListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this::onClick);
            itemView.setOnLongClickListener(this::onLongClick);
            mBinding = binding;
        }

        void applyItem(@NonNull final CdsObject object) {
            mObject = object;
            final boolean selected = object.equals(mSelectedObject);
            itemView.setSelected(selected);
            mBinding.setModel(new CdsItemModel(itemView.getContext(), object, selected));
            mBinding.executePendingBindings();
        }

        private void onClick(@NonNull View v) {
            mClickListener.onItemClick(v, mObject);
        }

        public boolean onLongClick(@NonNull View v) {
            mLongClickListener.onItemLongClick(v, mObject);
            return true;
        }
    }
}
