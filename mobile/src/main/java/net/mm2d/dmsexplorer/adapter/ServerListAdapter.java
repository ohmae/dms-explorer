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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding;
import net.mm2d.dmsexplorer.model.ServerItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * MediaServerをRecyclerViewへ表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListAdapter
        extends RecyclerView.Adapter<ServerListAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull View v, int position, @NonNull MediaServer server);
    }
    private static final OnItemClickListener ON_ITEM_CLICK_LISTENER
            = (v, position, server) -> {};
    public interface OnItemLongClickListener {
        void onItemLongClick(@NonNull View v, int position, @NonNull MediaServer server);
    }
    private static final OnItemLongClickListener ON_ITEM_LONG_CLICK_LISTENER
            = (v, position, server) -> {};

    private static final int NOT_SELECTED = -1;
    private final LayoutInflater mInflater;
    private final List<MediaServer> mList;
    private OnItemClickListener mClickListener = ON_ITEM_CLICK_LISTENER;
    private OnItemLongClickListener mLongClickListener = ON_ITEM_LONG_CLICK_LISTENER;
    private int mSelection = NOT_SELECTED;

    public ServerListAdapter(Context context, Collection<? extends MediaServer> servers) {
        mInflater = LayoutInflater.from(context);
        if (servers == null) {
            mList = new ArrayList<>();
        } else {
            mList = new ArrayList<>(servers);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(mInflater, R.layout.server_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.applyItem(position, mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int indexOf(@NonNull MediaServer server) {
        return mList.indexOf(server);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener != null ? listener : ON_ITEM_CLICK_LISTENER;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener != null ? listener : ON_ITEM_LONG_CLICK_LISTENER;
    }

    public void clear() {
        mList.clear();
    }

    public void addAll(Collection<? extends MediaServer> servers) {
        mList.addAll(servers);
    }

    public int add(MediaServer server) {
        mList.add(server);
        return mList.size() - 1;
    }

    public int remove(MediaServer server) {
        final int position = mList.indexOf(server);
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

    private final View.OnClickListener mItemClickListener = v -> {
        final ViewHolder holder = (ViewHolder) v.getTag();
        final MediaServer server = holder.getItem();
        final int position = holder.getListPosition();
        mClickListener.onItemClick(v, position, server);
    };
    private final View.OnLongClickListener mItemLongClickListener = v -> {
        final ViewHolder holder = (ViewHolder) v.getTag();
        final MediaServer server = holder.getItem();
        final int position = holder.getListPosition();
        mLongClickListener.onItemLongClick(v, position, server);
        return true;
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ServerListItemBinding mBinding;
        private int mPosition;
        private MediaServer mServer;

        ViewHolder(ServerListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(mItemClickListener);
            itemView.setOnLongClickListener(mItemLongClickListener);
            itemView.setTag(this);
            mBinding = binding;
        }

        void applyItem(final int position, final @NonNull MediaServer server) {
            mPosition = position;
            mServer = server;
            final boolean selected = mSelection == position;
            itemView.setSelected(selected);
            mBinding.setModel(new ServerItemModel(itemView.getContext(), server, selected));
            mBinding.executePendingBindings();
        }

        MediaServer getItem() {
            return mServer;
        }

        int getListPosition() {
            return mPosition;
        }
    }
}
