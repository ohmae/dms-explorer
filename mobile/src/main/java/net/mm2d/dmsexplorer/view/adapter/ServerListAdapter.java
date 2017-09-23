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

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding;
import net.mm2d.dmsexplorer.util.FeatureUtils;
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
        void onItemClick(
                @NonNull View v,
                @NonNull MediaServer server);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(
                @NonNull View v,
                @NonNull MediaServer server);
    }

    private static final float FOCUS_SCALE = 1.1f;
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
    private final boolean mHasTouchScreen;
    private final float mTranslationZ;

    public ServerListAdapter(
            @NonNull final Context context,
            @Nullable final Collection<? extends MediaServer> servers) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mHasTouchScreen = FeatureUtils.hasTouchScreen(context);
        if (servers == null) {
            mList = new ArrayList<>();
        } else {
            mList = new ArrayList<>(servers);
        }
        mTranslationZ = context.getResources().getDimension(R.dimen.list_item_focus_elevation);
    }

    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(mInflater, R.layout.server_list_item, parent, false));
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

    public int indexOf(@NonNull final MediaServer server) {
        return mList.indexOf(server);
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

    public void addAll(@NonNull final Collection<? extends MediaServer> servers) {
        mList.addAll(servers);
    }

    public int add(@NonNull final MediaServer server) {
        mList.add(server);
        return mList.size() - 1;
    }

    public int remove(@NonNull final MediaServer server) {
        final int position = mList.indexOf(server);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public void setSelectedServer(@Nullable final MediaServer server) {
        if (mSelectedServer != null && mSelectedServer.equals(server)) {
            return;
        }
        final MediaServer previous = mSelectedServer;
        mSelectedServer = server;
        notifyItemChangedIfPossible(previous);
        notifyItemChangedIfPossible(server);
    }

    private void notifyItemChangedIfPossible(@Nullable final MediaServer server) {
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

        ViewHolder(@NonNull final ServerListItemBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this::onClick);
            itemView.setOnLongClickListener(this::onLongClick);
            if (!mHasTouchScreen) {
                itemView.setOnFocusChangeListener(this::onFocusChange);
            }
            mBinding = binding;
        }

        void applyItem(@NonNull final MediaServer server) {
            mServer = server;
            final boolean selected = server.equals(mSelectedServer);
            itemView.setSelected(selected);
            mBinding.setModel(new ServerItemModel(mContext, server, selected));
            mBinding.executePendingBindings();
        }

        private void onClick(@NonNull final View v) {
            mClickListener.onItemClick(v, mServer);
        }

        private boolean onLongClick(@NonNull final View v) {
            mLongClickListener.onItemLongClick(v, mServer);
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
