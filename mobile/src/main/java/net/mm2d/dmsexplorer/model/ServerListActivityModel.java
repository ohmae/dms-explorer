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
    public final ItemDecoration itemDecoration;
    private boolean mRefreshing;
    private final ServerListAdapter mServerListAdapter;
    private final LayoutManager mServerListLayoutManager;

    private final ControlPointModel mControlPointModel = DataHolder.getInstance().getControlPointModel();
    private final MsControlPoint mMsControlPoint = mControlPointModel.getMsControlPoint();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ServerSelectListener mServerSelectListener;

    public ServerListActivityModel(@NonNull Context context, ServerSelectListener listener) {
        itemDecoration = new DividerItemDecoration(context);
        mServerListAdapter = new ServerListAdapter(context, mMsControlPoint.getDeviceList());
        mServerListAdapter.setOnItemClickListener(this::onItemClick);
        mServerListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        mServerListLayoutManager = new LinearLayoutManager(context);
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

    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(final boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    public Adapter getServerListAdapter() {
        return mServerListAdapter;
    }

    public LayoutManager getServerListLayoutManager() {
        return mServerListLayoutManager;
    }

    public void updateListAdapter() {
        final List<MediaServer> list = mMsControlPoint.getDeviceList();
        final int position = list.indexOf(mControlPointModel.getSelectedMediaServer());
        mServerListAdapter.clear();
        mServerListAdapter.addAll(list);
        mServerListAdapter.notifyDataSetChanged();
        mServerListAdapter.setSelection(position);
    }

    public View findSharedView() {
        final MediaServer server = mControlPointModel.getSelectedMediaServer();
        final int p = mServerListAdapter.indexOf(server);
        final View shared = mServerListLayoutManager.findViewByPosition(p);
        if (shared != null) {
            return shared.findViewById(R.id.accent);
        }
        return null;
    }

    private void onItemClick(final @NonNull View v, final int position, final @NonNull MediaServer server) {
        final boolean alreadySelected = mControlPointModel.isSelectedMediaServer(server);
        mServerListAdapter.setSelection(position);
        mControlPointModel.selectMediaServer(server);
        mServerSelectListener.onSelect(v, server, alreadySelected);
    }

    private void onItemLongClick(final @NonNull View v, final int position, final @NonNull MediaServer server) {
        mServerListAdapter.setSelection(position);
        mControlPointModel.selectMediaServer(server);
        mServerSelectListener.onDetermine(v, server);
    }

    private void onDiscoverServer(MediaServer server) {
        setRefreshing(false);
        if (mMsControlPoint.getDeviceListSize()
                != mServerListAdapter.getItemCount() + 1) {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mMsControlPoint.getDeviceList());
            mServerListAdapter.notifyDataSetChanged();
        } else {
            final int position = mServerListAdapter.add(server);
            mServerListAdapter.notifyItemInserted(position);
        }
    }

    private void onLostServer(MediaServer server) {
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
            mServerListAdapter.clearSelection();
            mControlPointModel.unselectMediaServer();
        }
    }
}
