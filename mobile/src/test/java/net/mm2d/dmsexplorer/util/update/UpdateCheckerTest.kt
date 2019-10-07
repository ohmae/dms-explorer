/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.util.OkHttpClientHolder
import net.mm2d.dmsexplorer.util.update.model.UpdateInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class UpdateCheckerTest {
    @Test
    fun isUpdateAvailable_異常な入力ならfalse_必須フィールドがない() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_異常な入力ならfalse_フォーマットエラー() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": []
  }""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_targetが1つで対象_バージョンが低ければtrue() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Config(sdk = [21])
    @Test
    fun isUpdateAvailable_minSdkVersion以上() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": [],
    "minSdkVersion": 21
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Config(sdk = [21])
    @Test
    fun isUpdateAvailable_minSdkVersion未満() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": [],
    "minSdkVersion": 22
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_余計なフィールドがあってもtrueとなる条件がそろっていればtrue() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": [],
    "someField": "none"
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_targetが1つで対象_バージョンが等しければfalse() {
        val version = 716
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_targetが1つで対象_バージョンが高ければfalse() {
        val version = 717
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_targetに記載がなければfalse() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_1つで対象ならtrue() {
        val version = 710
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_1つで対象外ならfalse() {
        val version = 710
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      711
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_2つで対象ならtrue1() {
        val version = 710
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710, 714
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_2つで対象ならtrue2() {
        val version = 714
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710, 714
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_2つで対象ならtrue3() {
        val version = 712
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710, 714
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_2つで対象外ならfalse1() {
        val version = 709
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710, 714
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_2つで対象外ならfalse2() {
        val version = 715
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      710, 714
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_3つで対象ならtrue1() {
        val version = 700
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_3つで対象ならtrue2() {
        val version = 710
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_3つで対象ならtrue3() {
        val version = 712
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_3つで対象ならtrue4() {
        val version = 705
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_3つで対象ならtrue5() {
        val version = 714
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_3つで対象外ならfalse1() {
        val version = 600
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_3つで対象外ならfalse2() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,710,712
    ],
    "targetExclude": []
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_対象で除外リストに該当しないならtrue1() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      710
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }


    @Test
    fun isUpdateAvailable_対象で除外リストに該当しないならtrue2() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      710
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun isUpdateAvailable_対象だが除外ならfalse1() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      711
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_対象だが除外ならfalse2() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      700, 711
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_対象だが除外ならfalse3() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      711,712
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isFalse()
    }

    @Test
    fun isUpdateAvailable_対象であり除外外ならtrue() {
        val version = 711
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      0, 714
    ],
    "targetExclude": [
      710,712
    ]
  }
}""".trimIndent()
        assertThat(UpdateChecker(version).isUpdateAvailable(json)).isTrue()
    }

    @Test
    fun moshi_fromJson() {
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,
      714
    ],
    "targetExclude": [
      711,
      712
    ]
  }
}""".trimIndent()
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(UpdateInfo::class.java)
        val update = jsonAdapter.fromJson(json) ?: throw IllegalStateException()
        val mobile = update.mobile
        assertThat(mobile.versionCode).isEqualTo(716)
        assertThat(mobile.versionName).isEqualTo("0.7.16")
        assertThat(mobile.targetInclude).isEqualTo(listOf(700, 714))
        assertThat(mobile.targetExclude).isEqualTo(listOf(711, 712))
    }

    @Test
    fun moshi_toJson() {
        val json = """
{
  "mobile": {
    "versionName": "0.7.16",
    "versionCode": 716,
    "targetInclude": [
      700,
      714
    ],
    "targetExclude": [
      711,
      712
    ]
  }
}""".trimIndent()
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(UpdateInfo::class.java)
        val update = jsonAdapter.fromJson(json)
        val result = jsonAdapter.toJson(update)
        val update2 = jsonAdapter.fromJson(result)

        assertThat(update).isEqualTo(update2)
    }

    @Test
    fun retrofit() {
        val moshi = Moshi.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.URL_UPDATE_BASE)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClientHolder.get())
            .build()
        val service = retrofit.create(UpdateService::class.java)
        val info = service.get().blockingGet()
        assertThat(info.isValid).isTrue()
    }
}
