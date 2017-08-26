/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.adapter.ServerListAdapter;
import net.mm2d.dmsexplorer.view.animator.CustomItemAnimator;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerListActivityModel extends BaseObservable {
    public interface ServerSelectListener {
        void onSelect(@NonNull View v);

        void onLostSelection();

        void onExecute(@NonNull View v);
    }

    @NonNull
    public final int[] refreshColors = new int[]{
            R.color.progress1,
            R.color.progress2,
            R.color.progress3,
            R.color.progress4,
    };
    @NonNull
    public final OnRefreshListener onRefreshListener;
    @NonNull
    public final ItemAnimator itemAnimator;
    @NonNull
    public final LayoutManager serverListLayoutManager;

    @NonNull
    private final ServerListAdapter mServerListAdapter;
    private boolean mRefreshing;

    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final ControlPointModel mControlPointModel;
    @NonNull
    private final ServerSelectListener mServerSelectListener;
    private final boolean mTwoPane;
    @NonNull
    private final Settings mSettings;

    public ServerListActivityModel(
            @NonNull final Context context,
            @NonNull final Repository repository,
            @NonNull final ServerSelectListener listener,
            final boolean twoPane) {
        mSettings = new Settings();
        mControlPointModel = repository.getControlPointModel();
        mServerListAdapter = new ServerListAdapter(context, mControlPointModel.getMediaServerList());
        mServerListAdapter.setOnItemClickListener(this::onItemClick);
        mServerListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        mRefreshing = mServerListAdapter.getItemCount() == 0;
        mServerSelectListener = listener;
        mControlPointModel.setMsDiscoveryListener(new MsDiscoveryListener() {
            @Override
            public void onDiscover(@NonNull final MediaServer server) {
                mHandler.post(() -> onDiscoverServer(server));
            }

            @Override
            public void onLost(@NonNull final MediaServer server) {
                mHandler.post(() -> onLostServer(server));
            }
        });

        serverListLayoutManager = new LinearLayoutManager(context);
        itemAnimator = new CustomItemAnimator(context);
        onRefreshListener = () -> mControlPointModel.restart(() -> {
            mServerListAdapter.clear();
            mServerListAdapter.notifyDataSetChanged();
        });
        mTwoPane = twoPane;
    }

    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(final boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    @NonNull
    public Adapter getServerListAdapter() {
        return mServerListAdapter;
    }

    public void updateListAdapter() {
        mServerListAdapter.clear();
        mServerListAdapter.addAll(mControlPointModel.getMediaServerList());
        mServerListAdapter.notifyDataSetChanged();
        mServerListAdapter.setSelectedServer(mControlPointModel.getSelectedMediaServer());
    }

    @Nullable
    public View findSharedView() {
        final MediaServer server = mControlPointModel.getSelectedMediaServer();
        final int position = mServerListAdapter.indexOf(server);
        if (position < 0) {
            return null;
        }
        final View listItem = serverListLayoutManager.findViewByPosition(position);
        final ServerListItemBinding binding = DataBindingUtil.findBinding(listItem);
        if (binding != null) {
            return binding.accent;
        }
        return null;
    }

    public boolean hasSelectedMediaServer() {
        return mControlPointModel.getSelectedMediaServer() != null;
    }

    private void onItemClick(
            @NonNull final View v,
            @NonNull final MediaServer server) {
        final boolean alreadySelected = mControlPointModel.isSelectedMediaServer(server);
        mServerListAdapter.setSelectedServer(server);
        mControlPointModel.setSelectedMediaServer(server);
        if (mSettings.shouldShowDeviceDetailOnTap()) {
            if (mTwoPane && alreadySelected) {
                mServerSelectListener.onExecute(v);
            } else {
                mServerSelectListener.onSelect(v);
            }
        } else {
            mServerSelectListener.onExecute(v);
        }
    }

    private void onItemLongClick(
            @NonNull final View v,
            @NonNull final MediaServer server) {
        mServerListAdapter.setSelectedServer(server);
        mControlPointModel.setSelectedMediaServer(server);

        if (mSettings.shouldShowDeviceDetailOnTap()) {
            mServerSelectListener.onExecute(v);
        } else {
            mServerSelectListener.onSelect(v);
        }
    }

    private void onDiscoverServer(@NonNull MediaServer server) {
        setRefreshing(false);
        if (mControlPointModel.getNumberOfMediaServer() == mServerListAdapter.getItemCount() + 1) {
            final int position = mServerListAdapter.add(server);
            mServerListAdapter.notifyItemInserted(position);
        } else {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mControlPointModel.getMediaServerList());
            mServerListAdapter.notifyDataSetChanged();
        }
    }

    private void onLostServer(@NonNull MediaServer server) {
        final int position = mServerListAdapter.remove(server);
        if (position < 0) {
            return;
        }
        if (mControlPointModel.getNumberOfMediaServer() == mServerListAdapter.getItemCount()) {
            mServerListAdapter.notifyItemRemoved(position);
        } else {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mControlPointModel.getMediaServerList());
            mServerListAdapter.notifyDataSetChanged();
        }
        if (server.equals(mControlPointModel.getSelectedMediaServer())) {
            mServerSelectListener.onLostSelection();
            mServerListAdapter.clearSelectedServer();
            mControlPointModel.clearSelectedServer();
        }
    }
}
