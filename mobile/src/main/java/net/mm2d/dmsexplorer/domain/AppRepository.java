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
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.domain.formatter.CdsFormatter;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.domain.model.CustomTabsBinder;
import net.mm2d.dmsexplorer.domain.model.CustomTabsHelper;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriCustomTabsModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.ThemeModel;
import net.mm2d.dmsexplorer.domain.model.ThemeModelImpl;
import net.mm2d.dmsexplorer.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class AppRepository extends Repository {
    @NonNull
    private final Context mContext;
    @NonNull
    private final ControlPointModel mControlPointModel;
    @NonNull
    private final ThemeModel mThemeModel;
    @NonNull
    private final OpenUriCustomTabsModel mOpenUriModel;
    @Nullable
    private MediaServerModel mMediaServerModel;
    @Nullable
    private MediaRendererModel mMediaRendererModel;
    @Nullable
    private PlaybackTargetModel mPlaybackTargetModel;

    public AppRepository(@NonNull final Application application) {
        mContext = application;
        mControlPointModel = new ControlPointModel(mContext, this::updateMediaServer, this::updateMediaRenderer);
        CdsFormatter.initialize(application);
        final ThemeModelImpl themeModel = new ThemeModelImpl();
        final CustomTabsHelper helper = new CustomTabsHelper(mContext);
        mOpenUriModel = new OpenUriCustomTabsModel(helper, themeModel);
        mOpenUriModel.setUseCustomTabs(Settings.get().useCustomTabs());
        mThemeModel = themeModel;

        application.registerActivityLifecycleCallbacks(new CustomTabsBinder(helper));
        application.registerActivityLifecycleCallbacks(themeModel);
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

    private MediaRendererModel createMediaRendererModel(@NonNull final MediaRenderer renderer) {
        return new MediaRendererModel(mContext, renderer);
    }

    private void updatePlaybackTarget(@Nullable final ContentEntity object) {
        mPlaybackTargetModel = object != null ? new PlaybackTargetModel(object) : null;
    }

    @Override
    @NonNull
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
    public MediaRendererModel getMediaRendererModel() {
        return mMediaRendererModel;
    }

    @Override
    @Nullable
    public PlaybackTargetModel getPlaybackTargetModel() {
        return mPlaybackTargetModel;
    }
}
