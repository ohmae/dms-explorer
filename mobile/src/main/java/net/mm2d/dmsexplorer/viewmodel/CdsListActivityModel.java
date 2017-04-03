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
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.view.DividerItemDecoration;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel.CdsListListener;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.CdsListAdapter;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsListActivityModel extends BaseObservable implements CdsListListener {
    public interface CdsSelectListener {
        void onSelect(@NonNull View v, @NonNull CdsObject object, boolean alreadySelected);

        void onUnselect();

        void onDetermine(@NonNull View v, @NonNull CdsObject object, boolean alreadySelected);
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
            mCdsTreeModel.reload();
        }
    };
    public final ItemDecoration itemDecoration;
    public final LayoutManager cdsListLayoutManager;
    public final String title;
    public final int toolbarBackground;

    private final CdsListAdapter mCdsListAdapter;
    private String mSubtitle;
    private boolean mRefreshing;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final CdsTreeModel mCdsTreeModel;
    private final CdsSelectListener mCdsSelectListener;

    public CdsListActivityModel(@NonNull Context context, @NonNull CdsSelectListener listener) {
        itemDecoration = new DividerItemDecoration(context);
        mCdsListAdapter = new CdsListAdapter(context);
        mCdsListAdapter.setOnItemClickListener(this::onItemClick);
        mCdsListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        cdsListLayoutManager = new LinearLayoutManager(context);

        mCdsSelectListener = listener;
        mCdsTreeModel = Repository.getInstance().getCdsTreeModel();
        mCdsTreeModel.setCdsListListener(this);
        title = mCdsTreeModel.getTitle();

        final MediaServer server = Repository.getInstance()
                .getControlPointModel().getSelectedMediaServer();
        ToolbarThemeUtils.setServerThemeColor(server, null);
        toolbarBackground = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
    }

    @Bindable
    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(final String subtitle) {
        mSubtitle = subtitle;
        notifyPropertyChanged(BR.subtitle);
    }

    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(final boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    public Adapter getCdsListAdapter() {
        return mCdsListAdapter;
    }

    private void onItemClick(@NonNull final View v, @NonNull final CdsObject object) {
        if (mCdsTreeModel.enterChild(object)) {
            return;
        }
        final boolean alreadySelected = object.equals(mCdsTreeModel.getSelectedObject());
        mCdsTreeModel.setSelectedObject(object);
        mCdsListAdapter.setSelectedObject(object);
        mCdsSelectListener.onSelect(v, object, alreadySelected);
    }

    private void onItemLongClick(@NonNull final View v, @NonNull final CdsObject object) {
        if (mCdsTreeModel.enterChild(object)) {
            return;
        }
        final boolean alreadySelected = object.equals(mCdsTreeModel.getSelectedObject());
        mCdsTreeModel.setSelectedObject(object);
        mCdsListAdapter.setSelectedObject(object);
        mCdsSelectListener.onDetermine(v, object, alreadySelected);
    }

    public boolean onBackPressed() {
        mCdsSelectListener.onUnselect();
        return mCdsTreeModel.exitToParent();
    }

    @Override
    public void onUpdateList(@NonNull final List<CdsObject> list, final boolean inProgress) {
        setRefreshing(inProgress);
        setSubtitle("[" + list.size() + "] " + mCdsTreeModel.getPath());
        mHandler.post(() -> updateList(list));
    }

    private void updateList(@NonNull final List<CdsObject> list) {
        final int beforeSize = mCdsListAdapter.getItemCount();
        final int afterSize = list.size();
        mCdsListAdapter.clear();
        mCdsListAdapter.addAll(list);
        mCdsListAdapter.setSelectedObject(mCdsTreeModel.getSelectedObject());
        if (beforeSize < afterSize) {
            mCdsListAdapter.notifyItemRangeInserted(beforeSize, afterSize - beforeSize);
        } else {
            mCdsListAdapter.notifyDataSetChanged();
        }
    }
}
