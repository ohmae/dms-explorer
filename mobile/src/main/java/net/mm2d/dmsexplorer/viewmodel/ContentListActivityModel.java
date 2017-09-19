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
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.model.ExploreListener;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentListAdapter;
import net.mm2d.dmsexplorer.view.animator.CustomItemAnimator;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentListActivityModel extends BaseObservable implements ExploreListener {
    private static final int INVALID_POSITION = -1;
    private static final int FIRST_COUNT = 20;
    private static final int SECOND_COUNT = 300;
    private static final long FIRST_INTERVAL = 50;
    private static final long SECOND_INTERVAL = 300;

    public interface CdsSelectListener {
        void onSelect(
                @NonNull View v,
                @NonNull ContentEntity entity);

        void onLostSelection();

        void onExecute(
                @NonNull View v,
                @NonNull ContentEntity entity,
                boolean selected);
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
    public final LayoutManager cdsListLayoutManager;
    @NonNull
    public final String title;
    public final int toolbarBackground;

    @NonNull
    private final ContentListAdapter mContentListAdapter;
    @NonNull
    private String mSubtitle = "";
    private boolean mRefreshing;
    private int mScrollPosition = INVALID_POSITION;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final MediaServerModel mMediaServerModel;
    @NonNull
    private final CdsSelectListener mCdsSelectListener;
    private final boolean mTwoPane;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private Runnable mUpdateList = () -> {
    };
    private long mUpdateTime;
    private boolean mTimerResetLatch;

    public ContentListActivityModel(
            @NonNull final Context context,
            @NonNull final Repository repository,
            @NonNull final CdsSelectListener listener,
            final boolean twoPane) {
        mSettings = new Settings(context);
        mTwoPane = twoPane;
        final MediaServerModel model = repository.getMediaServerModel();
        if (model == null) {
            throw new IllegalStateException();
        }
        mMediaServerModel = model;
        mMediaServerModel.setExploreListener(this);
        mContentListAdapter = new ContentListAdapter(context);
        mContentListAdapter.setOnItemClickListener(this::onItemClick);
        mContentListAdapter.setOnItemLongClickListener(this::onItemLongClick);
        mCdsSelectListener = listener;

        itemAnimator = new CustomItemAnimator(context);
        cdsListLayoutManager = new LinearLayoutManager(context);
        title = mMediaServerModel.getTitle();
        onRefreshListener = mMediaServerModel::reload;
        final MediaServer server = mMediaServerModel.getMediaServer();
        ToolbarThemeUtils.setServerThemeColor(server, null);
        toolbarBackground = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
    }

    @NonNull
    @Bindable
    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(@NonNull final String subtitle) {
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

    private void onItemClick(
            @NonNull final View v,
            @NonNull final ContentEntity entity) {
        if (mMediaServerModel.enterChild(entity)) {
            mCdsSelectListener.onLostSelection();
            return;
        }
        final boolean selected = entity.equals(mMediaServerModel.getSelectedEntity());
        mMediaServerModel.setSelectedEntity(entity);
        mContentListAdapter.setSelectedEntity(entity);
        if (mSettings.shouldShowContentDetailOnTap()) {
            if (mTwoPane && selected) {
                mCdsSelectListener.onExecute(v, entity, true);
            } else {
                mCdsSelectListener.onSelect(v, entity);
            }
        } else {
            mCdsSelectListener.onExecute(v, entity, selected);
        }
    }

    private void onItemLongClick(
            @NonNull final View v,
            @NonNull final ContentEntity entity) {
        if (mMediaServerModel.enterChild(entity)) {
            mCdsSelectListener.onLostSelection();
            return;
        }
        final boolean selected = entity.equals(mMediaServerModel.getSelectedEntity());
        mMediaServerModel.setSelectedEntity(entity);
        mContentListAdapter.setSelectedEntity(entity);

        if (mSettings.shouldShowContentDetailOnTap()) {
            mCdsSelectListener.onExecute(v, entity, selected);
        } else {
            mCdsSelectListener.onSelect(v, entity);
        }
    }

    public void syncSelectedEntity() {
        final ContentEntity entity = mMediaServerModel.getSelectedEntity();
        if (!mContentListAdapter.setSelectedEntity(entity) || entity == null) {
            return;
        }
        final int index = mContentListAdapter.indexOf(entity);
        if (index >= 0) {
            setScrollPosition(index);
        }
    }

    public boolean onBackPressed() {
        mCdsSelectListener.onLostSelection();
        return mMediaServerModel.exitToParent();
    }

    @Override
    public void onStart() {
        setSize(0);
        setRefreshing(true);
        mHandler.post(() -> updateList(Collections.emptyList()));
    }

    @Override
    public void onUpdate(@NonNull final List<ContentEntity> list) {
        setSize(list.size());
        mHandler.post(() -> updateList(list));
    }

    @Override
    public void onComplete() {
        setRefreshing(false);
    }

    private void setSize(final int size) {
        setSubtitle("[" + size + "] " + mMediaServerModel.getPath());
    }

    private long calculateDelay(
            final int before,
            final int after) {
        if (before > after) {
            return 0;
        }
        final long diff = System.currentTimeMillis() - mUpdateTime;
        if (before == 0) {
            if (after < FIRST_COUNT && diff < FIRST_INTERVAL) {
                return FIRST_INTERVAL - diff;
            }
            return 0;
        }
        if (after - before < SECOND_COUNT && diff < SECOND_INTERVAL) {
            return SECOND_INTERVAL - diff;
        }
        return 0;
    }

    private void updateList(@NonNull final List<ContentEntity> list) {
        final int beforeSize = mContentListAdapter.getItemCount();
        final int afterSize = list.size();
        if (beforeSize == afterSize) {
            return;
        }
        if (mTimerResetLatch && beforeSize == 0 && afterSize == 1) {
            mUpdateTime = System.currentTimeMillis();
        }
        mTimerResetLatch = afterSize == 0;
        mHandler.removeCallbacks(mUpdateList);
        final long delay = calculateDelay(beforeSize, afterSize);
        if (delay > 0) {
            mUpdateList = () -> updateList(list);
            mHandler.postDelayed(mUpdateList, delay);
            return;
        }
        mUpdateTime = System.currentTimeMillis();
        mContentListAdapter.clear();
        mContentListAdapter.addAll(list);
        final ContentEntity entity = mMediaServerModel.getSelectedEntity();
        mContentListAdapter.setSelectedEntity(entity);
        if (beforeSize < afterSize) {
            mContentListAdapter.notifyItemRangeInserted(beforeSize, afterSize - beforeSize);
        } else {
            mContentListAdapter.notifyDataSetChanged();
        }
        if (beforeSize == 0 && entity != null) {
            setScrollPosition(list.indexOf(entity));
        } else {
            mScrollPosition = INVALID_POSITION;
        }
    }

    public boolean isItemSelected() {
        final ContentEntity entity = mMediaServerModel.getSelectedEntity();
        return entity != null && entity.getType().isPlayable();
    }

    public void terminate() {
        mMediaServerModel.terminate();
        mMediaServerModel.initialize();
    }

    // 選択項目を中央に表示させる処理
    // FIXME: DataBindingを使ったことで返って複雑化してしまっている
    @Bindable
    public int getScrollPosition() {
        return mScrollPosition;
    }

    public void setScrollPosition(final int position) {
        mScrollPosition = position;
        notifyPropertyChanged(BR.scrollPosition);
    }
}
