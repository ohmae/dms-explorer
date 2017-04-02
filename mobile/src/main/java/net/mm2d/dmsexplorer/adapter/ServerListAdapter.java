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

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding;
import net.mm2d.dmsexplorer.viewmodel.ServerItemModel;

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
        void onItemClick(@NonNull View v, @NonNull MediaServer server);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(@NonNull View v, @NonNull MediaServer server);
    }

    private static final OnItemClickListener ON_ITEM_CLICK_LISTENER = (v, server) -> {
    };
    private static final OnItemLongClickListener ON_ITEM_LONG_CLICK_LISTENER = (v, server) -> {
    };
    private final LayoutInflater mInflater;
    private final List<MediaServer> mList;
    private final Context mContext;
    private OnItemClickListener mClickListener = ON_ITEM_CLICK_LISTENER;
    private OnItemLongClickListener mLongClickListener = ON_ITEM_LONG_CLICK_LISTENER;
    private MediaServer mSelectedServer;

    public ServerListAdapter(@NonNull Context context, @Nullable Collection<? extends MediaServer> servers) {
        mContext = context;
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
        holder.applyItem(mList.get(position));
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

    public void setSelectedServer(@Nullable MediaServer server) {
        if (mSelectedServer != null && mSelectedServer.equals(server)) {
            return;
        }
        final MediaServer previous = mSelectedServer;
        mSelectedServer = server;
        notifyItemChangedIfPossible(previous);
        notifyItemChangedIfPossible(server);
    }

    private void notifyItemChangedIfPossible(@Nullable MediaServer server) {
        if (server == null) {
            return;
        }
        final int position = mList.indexOf(server);
        if (position < 0) {
            return;
        }
        notifyItemChanged(position);
    }

    public void clearSelectedServer() {
        setSelectedServer(null);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ServerListItemBinding mBinding;
        private MediaServer mServer;

        ViewHolder(ServerListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this::onClick);
            itemView.setOnLongClickListener(this::onLongClick);
            mBinding = binding;
        }

        void applyItem(final @NonNull MediaServer server) {
            mServer = server;
            final boolean selected = server.equals(mSelectedServer);
            itemView.setSelected(selected);
            mBinding.setModel(new ServerItemModel(mContext, server, selected));
            mBinding.executePendingBindings();
        }

        private void onClick(View v) {
            mClickListener.onItemClick(v, mServer);
        }

        private boolean onLongClick(View v) {
            mLongClickListener.onItemLongClick(v, mServer);
            return true;
        }
    }
}
