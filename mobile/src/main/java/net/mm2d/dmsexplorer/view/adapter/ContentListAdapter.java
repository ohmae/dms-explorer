/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ContentListItemBinding;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.util.FeatureUtils;
import net.mm2d.dmsexplorer.viewmodel.ContentItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CDSのコンテンツリストをRecyclerViewへ表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ContentListAdapter
        extends RecyclerView.Adapter<ContentListAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(
                @NonNull View v,
                @NonNull ContentEntity entity);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(
                @NonNull View v,
                @NonNull ContentEntity entity);
    }

    private static final float FOCUS_SCALE = 1.1f;
    private static final OnItemClickListener ON_ITEM_CLICK_LISTENER = (v, entity) -> {
    };
    private static final OnItemLongClickListener ON_ITEM_LONG_CLICK_LISTENER = (v, entity) -> {
    };
    @NonNull
    private final LayoutInflater mInflater;
    @NonNull
    private final List<ContentEntity> mList = new ArrayList<>();
    @NonNull
    private OnItemClickListener mClickListener = ON_ITEM_CLICK_LISTENER;
    @NonNull
    private OnItemLongClickListener mLongClickListener = ON_ITEM_LONG_CLICK_LISTENER;
    private ContentEntity mSelectedEntity;
    private final boolean mHasTouchScreen;
    private final float mTranslationZ;

    public ContentListAdapter(@NonNull final Context context) {
        mInflater = LayoutInflater.from(context);
        mHasTouchScreen = FeatureUtils.hasTouchScreen(context);
        mTranslationZ = context.getResources().getDimension(R.dimen.list_item_focus_elevation);
    }

    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            final int viewType) {
        return new ViewHolder(DataBindingUtil
                .inflate(mInflater, R.layout.content_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(
            @NonNull final ViewHolder holder,
            final int position) {
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

    public void addAll(@NonNull final Collection<? extends ContentEntity> entities) {
        mList.addAll(entities);
    }

    public int add(@NonNull final ContentEntity entity) {
        mList.add(entity);
        return mList.size() - 1;
    }

    public int remove(@NonNull final ContentEntity entity) {
        final int position = mList.indexOf(entity);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public int indexOf(@NonNull final ContentEntity entity) {
        return mList.indexOf(entity);
    }

    public ContentEntity getSelectedEntity() {
        return mSelectedEntity;
    }

    public boolean setSelectedEntity(@Nullable final ContentEntity entity) {
        if (mSelectedEntity != null && mSelectedEntity.equals(entity)) {
            return false;
        }
        final ContentEntity previous = mSelectedEntity;
        mSelectedEntity = entity;
        notifyItemChangedIfPossible(previous);
        notifyItemChangedIfPossible(entity);
        return true;
    }

    private void notifyItemChangedIfPossible(@Nullable final ContentEntity entity) {
        if (entity == null) {
            return;
        }
        final int position = mList.indexOf(entity);
        if (position < 0) {
            return;
        }
        notifyItemChanged(position);
    }

    public void clearSelectedEntity() {
        setSelectedEntity(null);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ContentListItemBinding mBinding;
        private ContentEntity mEntity;

        ViewHolder(@NonNull final ContentListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this::onClick);
            itemView.setOnLongClickListener(this::onLongClick);
            if (!mHasTouchScreen) {
                itemView.setOnFocusChangeListener(this::onFocusChange);
            }
            mBinding = binding;
        }

        void applyItem(@NonNull final ContentEntity entity) {
            mEntity = entity;
            final boolean selected = entity.equals(mSelectedEntity);
            itemView.setSelected(selected);
            mBinding.setModel(new ContentItemModel(itemView.getContext(), entity, selected));
            mBinding.executePendingBindings();
        }

        private void onClick(@NonNull final View v) {
            mClickListener.onItemClick(v, mEntity);
        }

        private boolean onLongClick(@NonNull final View v) {
            mLongClickListener.onItemLongClick(v, mEntity);
            return true;
        }

        private void onFocusChange(
                @NonNull final View v,
                final boolean focus) {
            if (focus) {
                v.setScaleX(FOCUS_SCALE);
                v.setScaleY(FOCUS_SCALE);
            } else {
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                v.setTranslationZ(focus ? mTranslationZ : 0.0f);
            }
        }
    }
}
