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

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.view.DividerItemDecoration;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.DataHolder;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.adapter.CdsListAdapter;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel;
import net.mm2d.dmsexplorer.domain.model.CdsTreeModel.CdsListListener;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsListActivityModel extends BaseObservable implements CdsListListener {
    public interface CdsSelectListener {
        void onSelect(@NonNull View v, boolean alreadySelected);

        void onUnselect();

        void onDetermine(@NonNull View v, boolean alreadySelected);
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
    private boolean mRefreshing;
    private final CdsListAdapter mCdsListAdapter;
    private final LayoutManager mCdsListLayoutManager;
    private final String mTitle;
    private String mSubtitle;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final CdsTreeModel mCdsTreeModel;
    private final CdsSelectListener mCdsSelectListener;

    public CdsListActivityModel(@NonNull Context context, @NonNull CdsSelectListener listener) {
        itemDecoration = new DividerItemDecoration(context);
        mCdsListAdapter = new CdsListAdapter(context);
        mCdsListAdapter.setOnItemClickListener(this::onItemClick);
        mCdsListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        mCdsListLayoutManager = new LinearLayoutManager(context);

        mCdsSelectListener = listener;
        mCdsTreeModel = DataHolder.getInstance().getCdsTreeModel();
        mCdsTreeModel.setCdsListListener(this);
        mTitle = mCdsTreeModel.getTitle();
    }

    public String getTitle() {
        return mTitle;
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

    public LayoutManager getCdsListLayoutManager() {
        return mCdsListLayoutManager;
    }

    private void onItemClick(final View v, int position, CdsObject object) {
        if (mCdsTreeModel.enterChild(position)) {
            return;
        }
        final boolean alreadySelected = object.equals(mCdsTreeModel.getSelectedObject());
        mCdsTreeModel.select(position);
        mCdsListAdapter.setSelection(position);
        mCdsSelectListener.onSelect(v, alreadySelected);
    }

    private void onItemLongClick(final View v, int position, CdsObject object) {
        if (mCdsTreeModel.enterChild(position)) {
            return;
        }
        final boolean alreadySelected = object.equals(mCdsTreeModel.getSelectedObject());
        mCdsTreeModel.select(position);
        mCdsListAdapter.setSelection(position);
        mCdsSelectListener.onDetermine(v, alreadySelected);
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
        mCdsListAdapter.setSelection(mCdsTreeModel.getSelectedPosition());
        if (beforeSize < afterSize) {
            mCdsListAdapter.notifyItemRangeInserted(beforeSize, afterSize - beforeSize);
        } else {
            mCdsListAdapter.notifyDataSetChanged();
        }
    }
}
