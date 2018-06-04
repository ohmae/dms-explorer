/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update

import android.support.annotation.VisibleForTesting
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.OkHttpClientHolder
import net.mm2d.dmsexplorer.util.update.model.UpdateInfo
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import net.mm2d.log.Log
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class UpdateChecker(
        private val currentVersion: Int = BuildConfig.VERSION_CODE
) {
    private val moshi by lazy {
        Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }
    private val jsonAdapter by lazy {
        moshi.adapter(UpdateInfo::class.java)
    }
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(Const.URL_UPDATE_BASE)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpClientHolder.get())
                .build()
    }

    fun check() {
        Single.fromCallable { Settings.get() }
                .subscribeOn(Schedulers.io())
                .flatMap { checkIfNeed(it) }
                .subscribe { it -> checkAndNotify(it) }
    }

    private fun checkIfNeed(settings: Settings): Single<UpdateInfo> {
        makeConsistent(settings)
        if (!hasEnoughInterval(settings)) {
            return Single.just(UpdateInfo.EMPTY)
        }
        return retrofit
                .create(UpdateService::class.java)
                .get()
    }

    private fun makeConsistent(settings: Settings) {
        if (settings.isUpdateAvailable && !isUpdateAvailable(settings.updateJson)) {
            settings.isUpdateAvailable = false
        }
    }

    private fun hasEnoughInterval(settings: Settings): Boolean {
        return System.currentTimeMillis() - settings.updateFetchTime > FETCH_INTERVAL
    }

    private fun checkAndNotify(info: UpdateInfo) {
        if (!info.isValid) {
            return
        }
        val settings = Settings.get()
        val before = settings.isUpdateAvailable
        checkAndSave(settings, info)
        if (settings.isUpdateAvailable == before) {
            return
        }
        EventRouter.createUpdateAvailabilityNotifier().send()
    }

    private fun checkAndSave(
            settings: Settings,
            info: UpdateInfo) {
        settings.setUpdateFetchTime()
        val normalizedJson = jsonAdapter.toJson(info)
        if (normalizedJson == settings.updateJson) {
            return
        }
        settings.isUpdateAvailable = isUpdateAvailable(info)
        settings.setUpdateJson(normalizedJson)
    }

    @VisibleForTesting
    internal fun isUpdateAvailable(json: String): Boolean {
        if (json.isEmpty()) {
            return false
        }
        try {
            val info = jsonAdapter.fromJson(json)
            return info != null && isUpdateAvailable(info)
        } catch (e: Exception) {
            Log.w(e)
        }

        return false
    }

    private fun isUpdateAvailable(info: UpdateInfo): Boolean {
        return currentVersion < info.versionCode
                && isInclude(info.targetInclude)
                && !isExclude(info.targetExclude)
    }

    private fun isInclude(ranges: List<Int>): Boolean {
        for (i in 0 until ranges.size step 2) {
            if (i + 1 < ranges.size) {
                if (ranges[i] <= currentVersion && ranges[i + 1] >= currentVersion) {
                    return true
                }
            } else {
                if (ranges[i] <= currentVersion) {
                    return true
                }
            }
        }
        return false
    }

    private fun isExclude(versions: List<Int>): Boolean {
        for (version in versions) {
            if (currentVersion == version) {
                return true
            }
        }
        return false
    }

    companion object {
        private val FETCH_INTERVAL = TimeUnit.HOURS.toMillis(23)
    }
}
