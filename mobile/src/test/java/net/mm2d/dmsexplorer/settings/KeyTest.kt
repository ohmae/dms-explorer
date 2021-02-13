/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Suppress("TestFunctionName", "NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class KeyTest {
    @Test
    fun getValueType_表示用Key() {
        assertThat(Key.VERSION_NUMBER.isReadWriteKey).isFalse()
    }

    @Test(expected = NullPointerException::class)
    fun getDefaultBoolean_表示用Key() {
        Key.VERSION_NUMBER.defaultBoolean
    }

    @Test(expected = NullPointerException::class)
    fun getDefaultInteger_表示用Key() {
        Key.VERSION_NUMBER.defaultInt
    }

    @Test(expected = NullPointerException::class)
    fun getDefaultLong_表示用Key() {
        Key.VERSION_NUMBER.defaultLong
    }

    @Test(expected = NullPointerException::class)
    fun getDefaultString_表示用Key() {
        Key.VERSION_NUMBER.defaultString
    }

    @Test
    fun getValueType_Boolean用Key() {
        assertThat(Key.PLAY_MOVIE_MYSELF.isBooleanKey).isTrue()
    }

    @Test
    fun getDefaultBoolean_Boolean用Key() {
        Key.PLAY_MOVIE_MYSELF.defaultBoolean
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultInteger_Boolean用Key() {
        Key.PLAY_MOVIE_MYSELF.defaultInt
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultLong_Boolean用Key() {
        Key.PLAY_MOVIE_MYSELF.defaultLong
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultString_Boolean用Key() {
        Key.PLAY_MOVIE_MYSELF.defaultString
    }

    @Test
    fun getValueType_Integer用Key() {
        assertThat(Key.SETTINGS_VERSION.isIntKey).isTrue()
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultBoolean_Integer用Key() {
        Key.SETTINGS_VERSION.defaultBoolean
    }

    @Test
    fun getDefaultInteger_Integer用Key() {
        Key.SETTINGS_VERSION.defaultInt
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultLong_Integer用Key() {
        Key.SETTINGS_VERSION.defaultLong
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultString_Integer用Key() {
        Key.SETTINGS_VERSION.defaultString
    }

    @Test
    fun getValueType_String用Key() {
        assertThat(Key.REPEAT_MODE_MOVIE.isStringKey).isTrue()
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultBoolean_String用Key() {
        Key.REPEAT_MODE_MOVIE.defaultBoolean
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultInteger_String用Key() {
        Key.REPEAT_MODE_MOVIE.defaultInt
    }

    @Test(expected = ClassCastException::class)
    fun getDefaultLong_String用Key() {
        Key.REPEAT_MODE_MOVIE.defaultLong
    }

    @Test
    fun getDefaultString_String用Key() {
        Key.REPEAT_MODE_MOVIE.defaultString
    }
}
