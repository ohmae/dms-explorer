/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.settings.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class UpdateChecker {
    private static final long FETCH_INTERVAL = TimeUnit.HOURS.toMillis(23);

    private final Context mContext;
    private final Settings mSettings;
    private final int mCurrentVersion;

    public UpdateChecker(@NonNull final Context context) {
        this(context, new Settings(context), BuildConfig.VERSION_CODE);
    }

    @VisibleForTesting
    UpdateChecker(
            final Context context,
            final Settings settings,
            final int version) {
        mContext = context;
        mSettings = settings;
        mCurrentVersion = version;
    }

    public void check() {
        if (mSettings.isUpdateAvailable() && !isUpdateAvailable(mSettings.getUpdateJson())) {
            mSettings.setUpdateAvailable(false);
        }
        if (System.currentTimeMillis() - mSettings.getUpdateFetchTime() < FETCH_INTERVAL) {
            return;
        }
        fetchAndCheck();
    }

    private void fetchAndCheck() {
        createFetcher()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkAndNotify);
    }

    @NonNull
    private Single<String> createFetcher() {
        return Single.create(emitter -> {
            final Request request = new Builder()
                    .url(Const.URL_UPDATE_JSON)
                    .get()
                    .build();
            final OkHttpClient client = new OkHttpClient();
            final Response response = client.newCall(request).execute();
            final ResponseBody body = response.body();
            if (response.isSuccessful() && body != null) {
                emitter.onSuccess(body.string());
                return;
            }
            emitter.onError(new IOException());
        });
    }

    private void checkAndNotify(@NonNull final String json) {
        final boolean before = mSettings.isUpdateAvailable();
        try {
            checkAndSave(json);
        } catch (final JSONException ignored) {
            return;
        }
        if (mSettings.isUpdateAvailable() == before) {
            return;
        }
        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(new Intent(Const.ACTION_UPDATE));
    }

    private void checkAndSave(@NonNull final String json) throws JSONException {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        mSettings.setUpdateFetchTime();
        final JSONObject jsonObject = new JSONObject(json);
        final String normalizedJson = jsonObject.toString();
        if (normalizedJson.equals(mSettings.getUpdateJson())) {
            return;
        }
        final UpdateInfo info = new UpdateInfo(jsonObject);
        mSettings.setUpdateAvailable(isUpdateAvailable(info));
        mSettings.setUpdateJson(normalizedJson);
    }

    @VisibleForTesting
    boolean isUpdateAvailable(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            final JSONObject jsonObject = new JSONObject(json);
            final UpdateInfo info = new UpdateInfo(jsonObject);
            return isUpdateAvailable(info);
        } catch (final JSONException ignored) {
        }
        return false;
    }

    private boolean isUpdateAvailable(@NonNull final UpdateInfo info) {
        return mCurrentVersion < info.getVersionCode()
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
