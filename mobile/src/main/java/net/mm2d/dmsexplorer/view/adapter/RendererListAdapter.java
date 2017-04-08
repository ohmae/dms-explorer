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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.RendererListItemBinding;
import net.mm2d.dmsexplorer.viewmodel.RendererItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class RendererListAdapter extends RecyclerView.Adapter<RendererListAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull View v, @NonNull MediaRenderer renderer);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(@NonNull View v, @NonNull MediaRenderer renderer);
    }

    private static final OnItemClickListener ON_ITEM_CLICK_LISTENER = (v, renderer) -> {
    };
    private static final OnItemLongClickListener ON_ITEM_LONG_CLICK_LISTENER = (v, renderer) -> {
    };
    private final LayoutInflater mInflater;
    private final List<MediaRenderer> mList;
    private final Context mContext;
    private OnItemClickListener mClickListener = ON_ITEM_CLICK_LISTENER;
    private OnItemLongClickListener mLongClickListener = ON_ITEM_LONG_CLICK_LISTENER;

    public RendererListAdapter(@NonNull final Context context,
                               @Nullable final Collection<? extends MediaRenderer> renderers) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (renderers == null) {
            mList = new ArrayList<>();
        } else {
            mList = new ArrayList<>(renderers);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(mInflater, R.layout.renderer_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.applyItem(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int indexOf(@NonNull final MediaRenderer renderer) {
        return mList.indexOf(renderer);
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

    public void addAll(@NonNull final Collection<? extends MediaRenderer> renderers) {
        mList.addAll(renderers);
    }

    public int add(@NonNull final MediaRenderer renderer) {
        mList.add(renderer);
        return mList.size() - 1;
    }

    public int remove(@NonNull final MediaRenderer renderer) {
        final int position = mList.indexOf(renderer);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final RendererListItemBinding mBinding;
        private MediaRenderer mRenderer;

        ViewHolder(@NonNull final RendererListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this::onClick);
            itemView.setOnLongClickListener(this::onLongClick);
            mBinding = binding;
        }

        void applyItem(@NonNull final MediaRenderer renderer) {
            mRenderer = renderer;
            mBinding.setModel(new RendererItemModel(mContext, renderer));
            mBinding.executePendingBindings();
        }

        private void onClick(@NonNull final View v) {
            mClickListener.onItemClick(v, mRenderer);
        }

        private boolean onLongClick(@NonNull final View v) {
            mLongClickListener.onItemLongClick(v, mRenderer);
            return true;
        }
    }
}
