/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.CdsObject;
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
    private static final String TAG = "CdsListAdapter";

    public interface OnItemClickListener {
        void onItemClick(View v, int position, CdsObject object);
    }

    private static final int NOT_SELECTED = -1;
    private final LayoutInflater mInflater;
    private final List<CdsObject> mList;
    private OnItemClickListener mListener;
    private int mSelection = NOT_SELECTED;

    public CdsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil
                .inflate(mInflater, R.layout.cds_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.applyItem(position, mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public void clear() {
        mList.clear();
    }

    public void addAll(Collection<? extends CdsObject> objects) {
        mList.addAll(objects);
    }

    public int add(CdsObject obj) {
        mList.add(obj);
        return mList.size() - 1;
    }

    public int remove(CdsObject obj) {
        final int position = mList.indexOf(obj);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public void setSelection(int position) {
        final int previous = mSelection;
        mSelection = position;
        if (previous != position) {
            notifyItemChangedIfPossible(previous);
        }
        notifyItemChangedIfPossible(position);
    }

    private void notifyItemChangedIfPossible(int position) {
        if (position == NOT_SELECTED || position >= getItemCount()) {
            return;
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        setSelection(NOT_SELECTED);
    }

    public int getSelection() {
        return mSelection;
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            final CdsObject obj = holder.getItem();
            final int position = holder.getListPosition();
            if (mListener != null) {
                mListener.onItemClick(v, position, obj);
            }
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CdsListItemBinding mBinding;
        private int mPosition;
        private CdsObject mObject;

        ViewHolder(CdsListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(mItemClickListener);
            itemView.setTag(this);
            mBinding = binding;
        }

        void applyItem(int position, CdsObject object) {
            mPosition = position;
            mObject = object;
            final boolean selected = mSelection == position;
            itemView.setSelected(selected);
            mBinding.setModel(new CdsItemModel(itemView.getContext(), object, selected));
            mBinding.executePendingBindings();
        }

        CdsObject getItem() {
            return mObject;
        }

        int getListPosition() {
            return mPosition;
        }
    }
}
