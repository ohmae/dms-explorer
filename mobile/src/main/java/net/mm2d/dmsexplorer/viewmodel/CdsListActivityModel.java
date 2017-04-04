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
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel.ExploreListener;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentListAdapter;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsListActivityModel extends BaseObservable implements ExploreListener {
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
            mMediaServerModel.reload();
        }
    };
    public final ItemDecoration itemDecoration;
    public final LayoutManager cdsListLayoutManager;
    public final String title;
    public final int toolbarBackground;

    private final ContentListAdapter mContentListAdapter;
    private String mSubtitle;
    private boolean mRefreshing;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final MediaServerModel mMediaServerModel;
    private final CdsSelectListener mCdsSelectListener;

    public CdsListActivityModel(@NonNull Context context, @NonNull CdsSelectListener listener) {
        itemDecoration = new DividerItemDecoration(context);
        mContentListAdapter = new ContentListAdapter(context);
        mContentListAdapter.setOnItemClickListener(this::onItemClick);
        mContentListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        cdsListLayoutManager = new LinearLayoutManager(context);

        mCdsSelectListener = listener;
        mMediaServerModel = Repository.getInstance().getMediaServerModel();
        mMediaServerModel.setExploreListener(this);
        title = mMediaServerModel.getTitle();

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

    public Adapter getContentListAdapter() {
        return mContentListAdapter;
    }

    private void onItemClick(@NonNull final View v, @NonNull final CdsObject object) {
        if (mMediaServerModel.enterChild(object)) {
            return;
        }
        final boolean alreadySelected = object.equals(mMediaServerModel.getSelectedObject());
        mMediaServerModel.setSelectedObject(object);
        mContentListAdapter.setSelectedObject(object);
        mCdsSelectListener.onSelect(v, object, alreadySelected);
    }

    private void onItemLongClick(@NonNull final View v, @NonNull final CdsObject object) {
        if (mMediaServerModel.enterChild(object)) {
            return;
        }
        final boolean alreadySelected = object.equals(mMediaServerModel.getSelectedObject());
        mMediaServerModel.setSelectedObject(object);
        mContentListAdapter.setSelectedObject(object);
        mCdsSelectListener.onDetermine(v, object, alreadySelected);
    }

    public boolean onBackPressed() {
        mCdsSelectListener.onUnselect();
        return mMediaServerModel.exitToParent();
    }

    @Override
    public void onUpdate(@NonNull final List<CdsObject> list, final boolean inProgress) {
        setRefreshing(inProgress);
        setSubtitle("[" + list.size() + "] " + mMediaServerModel.getPath());
        mHandler.post(() -> updateList(list));
    }

    private void updateList(@NonNull final List<CdsObject> list) {
        final int beforeSize = mContentListAdapter.getItemCount();
        final int afterSize = list.size();
        mContentListAdapter.clear();
        mContentListAdapter.addAll(list);
        mContentListAdapter.setSelectedObject(mMediaServerModel.getSelectedObject());
        if (beforeSize < afterSize) {
            mContentListAdapter.notifyItemRangeInserted(beforeSize, afterSize - beforeSize);
        } else {
            mContentListAdapter.notifyDataSetChanged();
        }
    }

    public boolean isItemSelected() {
        final CdsObject object = mMediaServerModel.getSelectedObject();
        return object != null && object.isItem();
    }

    public void terminate() {
        mMediaServerModel.terminate();
        mMediaServerModel.initialize();
    }
}
