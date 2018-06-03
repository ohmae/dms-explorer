/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.util.OkHttpClientHolder
import net.mm2d.dmsexplorer.util.update.model.UpdateInfo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("TestFunctionName")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UpdateCheckerTest {
    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_異常な入力ならfalse() {
        val version = 715
        val json1 = "{\"mobile\":{\"versionCode\":716,\"targetInclude\":[0],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json1), `is`(false))
        val json2 = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0],\"targetExclude\":[]}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json2), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_targetが1つで対象_バージョンが低ければtrue() {
        val version = 715
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_targetが1つで対象_バージョンが等しければfalse() {
        val version = 716
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_targetが1つで対象_バージョンが高ければfalse() {
        val version = 717
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_targetに記載がなければfalse() {
        val version = 715
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_1つで対象ならtrue() {
        val version = 710
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_1つで対象外ならfalse() {
        val version = 710
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[711],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_2つで対象ならtrue1() {
        val version = 710
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710,714],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_2つで対象ならtrue2() {
        val version = 714
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710,714],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_2つで対象ならtrue3() {
        val version = 712
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710,714],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_2つで対象外ならfalse1() {
        val version = 709
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710,714],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_2つで対象外ならfalse2() {
        val version = 715
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[710,714],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象ならtrue1() {
        val version = 700
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象ならtrue2() {
        val version = 710
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象ならtrue3() {
        val version = 712
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象ならtrue4() {
        val version = 705
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象ならtrue5() {
        val version = 714
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象外ならfalse1() {
        val version = 600
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_3つで対象外ならfalse2() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,710,712],\"targetExclude\":[]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_対象で除外リストに該当しないならtrue1() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0,714],\"targetExclude\":[710]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }


    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_対象で除外リストに該当しないならtrue2() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[0,714],\"targetExclude\":[710,712]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_対象だが除外ならfalse1() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,714],\"targetExclude\":[711]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_対象だが除外ならfalse2() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,714],\"targetExclude\":[700,711]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun isUpdateAvailable_対象だが除外ならfalse3() {
        val version = 711
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,714],\"targetExclude\":[711,712]}}"
        assertThat(UpdateChecker(version).isUpdateAvailable(json), `is`(false))
    }

    @Test
    @Throws(Exception::class)
    fun moshi_fromJson() {
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,714],\"targetExclude\":[711,712]}}"
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(UpdateInfo::class.java)
        val update = jsonAdapter.fromJson(json)
        assertThat(update!!.versionCode, `is`(716))
        assertThat(update.versionName, `is`("0.7.16"))
        assertThat(update.targetInclude, `is`(Arrays.asList(700, 714)))
        assertThat(update.targetExclude, `is`(Arrays.asList(711, 712)))
    }

    @Test
    @Throws(Exception::class)
    fun moshi_toJson() {
        val json = "{\"mobile\":{\"versionName\":\"0.7.16\",\"versionCode\":716,\"targetInclude\":[700,714],\"targetExclude\":[711,712]}}"
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(UpdateInfo::class.java)
        val update = jsonAdapter.fromJson(json)
        val result = jsonAdapter.toJson(update)
        assertThat(result, `is`(json))
    }

    @Test
    @Throws(Exception::class)
    fun retrofit() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
                .baseUrl(Const.URL_UPDATE_BASE)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpClientHolder.get())
                .build()
        val service = retrofit.create(UpdateService::class.java)
        val info = service.get().blockingGet()
        assertThat(info.versionCode, `is`(737))
    }
}
