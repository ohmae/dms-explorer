/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.MsControlPoint;
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.android.view.DividerItemDecoration;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.DataHolder;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.adapter.ServerListAdapter;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;

import java.util.List;


/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerListActivityModel extends BaseObservable {
    public interface ServerSelectListener {
        void onSelect(@NonNull View v, @NonNull MediaServer server, boolean alreadySelected);

        void onUnselect();

        void onDetermine(@NonNull View v, @NonNull MediaServer server);
    }

    public final int[] refreshColors = new int[]{
            R.color.progress1,
            R.color.progress2,
            R.color.progress3,
            R.color.progress4,
    };
    public final OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            mControlPointModel.restart(() -> {
                mServerListAdapter.clear();
                mServerListAdapter.notifyDataSetChanged();
            });
        }
    };
    private final ItemDecoration mItemDecoration;
    private final ServerListAdapter mServerListAdapter;
    private final LayoutManager mServerListLayoutManager;
    private boolean mRefreshing;

    private final ControlPointModel mControlPointModel = DataHolder.getInstance().getControlPointModel();
    private final MsControlPoint mMsControlPoint = mControlPointModel.getMsControlPoint();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ServerSelectListener mServerSelectListener;

    public ServerListActivityModel(@NonNull Context context, ServerSelectListener listener) {
        mItemDecoration = new DividerItemDecoration(context);
        mServerListLayoutManager = new LinearLayoutManager(context);
        mServerListAdapter = new ServerListAdapter(context, mMsControlPoint.getDeviceList());
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
    }

    public ItemDecoration getItemDecoration() {
        return mItemDecoration;
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

    @NonNull
    public LayoutManager getServerListLayoutManager() {
        return mServerListLayoutManager;
    }

    public void updateListAdapter() {
        final List<MediaServer> list = mMsControlPoint.getDeviceList();
        mServerListAdapter.clear();
        mServerListAdapter.addAll(list);
        mServerListAdapter.notifyDataSetChanged();
        mServerListAdapter.setSelectedServer(mControlPointModel.getSelectedMediaServer());
    }

    @Nullable
    public View findSharedView() {
        final MediaServer server = mControlPointModel.getSelectedMediaServer();
        final int position = mServerListAdapter.indexOf(server);
        final View shared = mServerListLayoutManager.findViewByPosition(position);
        if (shared != null) {
            return shared.findViewById(R.id.accent);
        }
        return null;
    }

    private void onItemClick(@NonNull final View v, @NonNull final MediaServer server) {
        final boolean alreadySelected = mControlPointModel.isSelectedMediaServer(server);
        mServerListAdapter.setSelectedServer(server);
        mControlPointModel.setSelectedServer(server);
        mServerSelectListener.onSelect(v, server, alreadySelected);
    }

    private void onItemLongClick(@NonNull final View v, @NonNull final MediaServer server) {
        mServerListAdapter.setSelectedServer(server);
        mControlPointModel.setSelectedServer(server);
        mServerSelectListener.onDetermine(v, server);
    }

    private void onDiscoverServer(@NonNull MediaServer server) {
        setRefreshing(false);
        if (mMsControlPoint.getDeviceListSize() != mServerListAdapter.getItemCount() + 1) {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mMsControlPoint.getDeviceList());
            mServerListAdapter.notifyDataSetChanged();
        } else {
            final int position = mServerListAdapter.add(server);
            mServerListAdapter.notifyItemInserted(position);
        }
    }

    private void onLostServer(@NonNull MediaServer server) {
        final int position = mServerListAdapter.remove(server);
        if (position >= 0) {
            if (mMsControlPoint.getDeviceListSize()
                    == mServerListAdapter.getItemCount()) {
                mServerListAdapter.notifyItemRemoved(position);
            } else {
                mServerListAdapter.clear();
                mServerListAdapter.addAll(mMsControlPoint.getDeviceList());
                mServerListAdapter.notifyDataSetChanged();
            }
        }
        if (server.equals(mControlPointModel.getSelectedMediaServer())) {
            mServerSelectListener.onUnselect();
            mServerListAdapter.clearSelectedServer();
            mControlPointModel.clearSelectedServer();
        }
    }
}
