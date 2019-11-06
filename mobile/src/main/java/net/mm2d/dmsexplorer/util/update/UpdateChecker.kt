/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.Moshi
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.OkHttpClientHolder
import net.mm2d.dmsexplorer.util.update.model.UpdateInfo
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import net.mm2d.log.Logger
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
        Moshi.Builder().build()
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

    @SuppressLint("CheckResult")
    fun check() {
        Single.fromCallable { Settings.get() }
            .subscribeOn(Schedulers.io())
            .flatMap { checkIfNeed(it) }
            .subscribe(Consumer { checkAndNotify(it) })
    }

    private fun checkIfNeed(settings: Settings): Single<UpdateInfo> {
        makeConsistent(settings)
        if (!hasEnoughInterval(settings)) {
            return Single.just(UpdateInfo.EMPTY_UPDATE_INFO)
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

    private fun hasEnoughInterval(settings: Settings): Boolean =
        System.currentTimeMillis() - settings.updateFetchTime > FETCH_INTERVAL

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
        EventRouter.notifyUpdateAvailabilityNotifier()
    }

    private fun checkAndSave(
        settings: Settings,
        info: UpdateInfo
    ) {
        settings.setUpdateFetchTime()
        val normalizedJson = jsonAdapter.toJson(info)
        if (normalizedJson == settings.updateJson) {
            return
        }
        settings.isUpdateAvailable = isUpdateAvailable(info.mobile)
        settings.updateJson = normalizedJson
    }

    @VisibleForTesting
    internal fun isUpdateAvailable(json: String): Boolean {
        if (json.isEmpty()) {
            return false
        }
        try {
            return jsonAdapter.fromJson(json)?.let {
                it.isValid && isUpdateAvailable(it.mobile)
            } ?: false
        } catch (e: Exception) {
            Logger.w(e)
        }
        return false
    }

    private fun isUpdateAvailable(info: UpdateInfo.Mobile): Boolean {
        return Build.VERSION.SDK_INT >= info.minSdkVersion
            && currentVersion < info.versionCode
            && isInclude(info.targetInclude)
            && !isExclude(info.targetExclude)
    }

    private fun isInclude(ranges: List<Int>): Boolean {
        ranges.chunked(2).forEach {
            if (it.size == 2) {
                if (it[0] <= currentVersion && it[1] >= currentVersion) {
                    return true
                }
            } else {
                if (it[0] <= currentVersion) {
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
