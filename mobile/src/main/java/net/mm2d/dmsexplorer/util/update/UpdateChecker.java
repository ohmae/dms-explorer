/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.OkHttpClientHolder;
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter;
import net.mm2d.log.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class UpdateChecker {
    private static final long FETCH_INTERVAL = TimeUnit.HOURS.toMillis(23);

    private final int mCurrentVersion;
    private final JsonAdapter<UpdateInfo> mAdapter = new Moshi.Builder().build().adapter(UpdateInfo.class);

    public UpdateChecker() {
        this(BuildConfig.VERSION_CODE);
    }

    @VisibleForTesting
    UpdateChecker(final int version) {
        mCurrentVersion = version;
    }

    @SuppressLint("CheckResult")
    public void check() {
        Single.fromCallable(Settings::get)
                .subscribeOn(Schedulers.io())
                .subscribe(this::execute);
    }

    @SuppressLint("CheckResult")
    private void execute(final Settings settings) {
        makeConsistent(settings);
        if (!hasEnoughInterval(settings)) {
            return;
        }
        createFetcher()
                .subscribeOn(Schedulers.io())
                .subscribe(this::checkAndNotify);
    }

    @NonNull
    private Single<UpdateInfo> createFetcher() {
        return new Retrofit.Builder()
                .baseUrl(Const.URL_UPDATE_BASE)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpClientHolder.get())
                .build()
                .create(UpdateService.class)
                .get();
    }

    private void makeConsistent(@NonNull final Settings settings) {
        if (settings.isUpdateAvailable() && !isUpdateAvailable(settings.getUpdateJson())) {
            settings.setUpdateAvailable(false);
        }
    }

    private boolean hasEnoughInterval(@NonNull final Settings settings) {
        return System.currentTimeMillis() - settings.getUpdateFetchTime() > FETCH_INTERVAL;
    }

    private void checkAndNotify(@NonNull final UpdateInfo info) {
        final Settings settings = Settings.get();
        final boolean before = settings.isUpdateAvailable();
        checkAndSave(settings, info);
        if (settings.isUpdateAvailable() == before) {
            return;
        }
        EventRouter.createUpdateAvailabilityNotifier().send();
    }

    private void checkAndSave(
            @NonNull final Settings settings,
            @NonNull final UpdateInfo info) {
        settings.setUpdateFetchTime();
        final String normalizedJson = mAdapter.toJson(info);
        if (normalizedJson.equals(settings.getUpdateJson())) {
            return;
        }
        settings.setUpdateAvailable(isUpdateAvailable(info));
        settings.setUpdateJson(normalizedJson);
    }

    @VisibleForTesting
    boolean isUpdateAvailable(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            final UpdateInfo info = mAdapter.fromJson(json);
            return info != null && isUpdateAvailable(info);
        } catch (IOException e) {
            Log.w(e);
        }
        return false;
    }

    private boolean isUpdateAvailable(@NonNull final UpdateInfo info) {
        return info.isValid()
                && mCurrentVersion < info.getVersionCode()
                && isInclude(info.getTargetInclude())
                && !isExclude(info.getTargetExclude());
    }

    private boolean isInclude(@NonNull final int[] ranges) {
        for (int i = 0; i < ranges.length; i += 2) {
            if (i + 1 < ranges.length) {
                if (ranges[i] <= mCurrentVersion && ranges[i + 1] >= mCurrentVersion) {
                    return true;
                }
            } else {
                if (ranges[i] <= mCurrentVersion) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExclude(@NonNull final int[] versions) {
        for (final int version : versions) {
            if (mCurrentVersion == version) {
                return true;
            }
        }
        return false;
    }
}
