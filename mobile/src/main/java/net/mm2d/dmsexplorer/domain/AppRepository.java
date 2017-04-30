/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriModelImpl;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.domain.model.ThemeModel;
import net.mm2d.dmsexplorer.domain.model.ThemeModelImpl;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class AppRepository extends Repository {
    private final Context mContext;
    private final ControlPointModel mControlPointModel;
    private final ThemeModel mThemeModel;
    private final OpenUriModel mOpenUriModel;
    private MediaServerModel mMediaServerModel;
    private PlayerModel mMediaRendererModel;
    private PlaybackTargetModel mPlaybackTargetModel;

    public AppRepository(@NonNull final Application application) {
        mContext = application;
        final CustomTabsSessionHelper helper = new CustomTabsSessionHelper(mContext);
        application.registerActivityLifecycleCallbacks(new LifecycleCallbacks(helper));
        mControlPointModel = new ControlPointModel(mContext, this::updateMediaServer, this::updateMediaRenderer);
        final ThemeModelImpl themeModel = new ThemeModelImpl();
        application.registerActivityLifecycleCallbacks(themeModel);
        mThemeModel = themeModel;
        mOpenUriModel = new OpenUriModelImpl(helper, themeModel);
    }

    private void updateMediaServer(@Nullable final MediaServer server) {
        if (mMediaServerModel != null) {
            mMediaServerModel.terminate();
            mMediaServerModel = null;
        }
        if (server != null) {
            mMediaServerModel = createMediaServerModel(server);
            mMediaServerModel.initialize();
        }
    }

    private void updateMediaRenderer(@Nullable final MediaRenderer renderer) {
        if (mMediaRendererModel != null) {
            mMediaRendererModel.terminate();
            mMediaRendererModel = null;
        }
        if (renderer != null) {
            mMediaRendererModel = createMediaRendererModel(renderer);
        }
    }

    private MediaServerModel createMediaServerModel(@NonNull final MediaServer server) {
        return new MediaServerModel(mContext, server, this::updatePlaybackTarget);
    }

    private PlayerModel createMediaRendererModel(@NonNull final MediaRenderer renderer) {
        return new MediaRendererModel(mContext, renderer);
    }

    private void updatePlaybackTarget(@Nullable final CdsObject object) {
        mPlaybackTargetModel = object != null ? new PlaybackTargetModel(object) : null;
    }

    @Override
    public ThemeModel getThemeModel() {
        return mThemeModel;
    }

    @Override
    @NonNull
    public OpenUriModel getOpenUriModel() {
        return mOpenUriModel;
    }

    @Override
    @NonNull
    public ControlPointModel getControlPointModel() {
        return mControlPointModel;
    }

    @Override
    @Nullable
    public MediaServerModel getMediaServerModel() {
        return mMediaServerModel;
    }

    @Override
    @Nullable
    public PlayerModel getMediaRendererModel() {
        return mMediaRendererModel;
    }

    @Override
    @Nullable
    public PlaybackTargetModel getPlaybackTargetModel() {
        return mPlaybackTargetModel;
    }
}
