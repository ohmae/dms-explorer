/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
@SuppressWarnings({"NonAsciiCharacters", "ResultOfMethodCallIgnored"})
@RunWith(RobolectricTestRunner.class)
public class KeyTest {
    @Test
    public void getValueType_表示用Key() throws Exception {
        assertThat(Key.VERSION_NUMBER.getValueType(), is(nullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void getDefaultBoolean_表示用Key() throws Exception {
        Key.VERSION_NUMBER.getDefaultBoolean();
    }

    @Test(expected = NullPointerException.class)
    public void getDefaultInteger_表示用Key() throws Exception {
        Key.VERSION_NUMBER.getDefaultInt();
    }

    @Test(expected = NullPointerException.class)
    public void getDefaultLong_表示用Key() throws Exception {
        Key.VERSION_NUMBER.getDefaultLong();
    }

    @Test(expected = NullPointerException.class)
    public void getDefaultString_表示用Key() throws Exception {
        Key.VERSION_NUMBER.getDefaultString();
    }

    @Test
    public void getValueType_Boolean用Key() throws Exception {
        assertThat((Key.PLAY_MOVIE_MYSELF.getValueType() == Boolean.class), is(true));
    }

    @Test
    public void getDefaultBoolean_Boolean用Key() throws Exception {
        Key.PLAY_MOVIE_MYSELF.getDefaultBoolean();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultInteger_Boolean用Key() throws Exception {
        Key.PLAY_MOVIE_MYSELF.getDefaultInt();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultLong_Boolean用Key() throws Exception {
        Key.PLAY_MOVIE_MYSELF.getDefaultLong();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultString_Boolean用Key() throws Exception {
        Key.PLAY_MOVIE_MYSELF.getDefaultString();
    }

    @Test
    public void getValueType_Integer用Key() throws Exception {
        assertThat((Key.SETTINGS_VERSION.getValueType() == Integer.class), is(true));
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultBoolean_Integer用Key() throws Exception {
        Key.SETTINGS_VERSION.getDefaultBoolean();
    }

    @Test
    public void getDefaultInteger_Integer用Key() throws Exception {
        Key.SETTINGS_VERSION.getDefaultInt();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultLong_Integer用Key() throws Exception {
        Key.SETTINGS_VERSION.getDefaultLong();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultString_Integer用Key() throws Exception {
        Key.SETTINGS_VERSION.getDefaultString();
    }

    @Test
    public void getValueType_Long用Key() throws Exception {
        assertThat((Key.UPDATE_FETCH_TIME.getValueType() == Long.class), is(true));
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultBoolean_Long用Key() throws Exception {
        Key.UPDATE_FETCH_TIME.getDefaultBoolean();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultInteger_Long用Key() throws Exception {
        Key.UPDATE_FETCH_TIME.getDefaultInt();
    }

    @Test
    public void getDefaultLong_Long用Key() throws Exception {
        Key.UPDATE_FETCH_TIME.getDefaultLong();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultString_Long用Key() throws Exception {
        Key.UPDATE_FETCH_TIME.getDefaultString();
    }

    @Test
    public void getValueType_String用Key() throws Exception {
        assertThat((Key.REPEAT_MODE_MOVIE.getValueType() == String.class), is(true));
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultBoolean_String用Key() throws Exception {
        Key.REPEAT_MODE_MOVIE.getDefaultBoolean();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultInteger_String用Key() throws Exception {
        Key.REPEAT_MODE_MOVIE.getDefaultInt();
    }

    @Test(expected = ClassCastException.class)
    public void getDefaultLong_String用Key() throws Exception {
        Key.REPEAT_MODE_MOVIE.getDefaultLong();
    }

    @Test
    public void getDefaultString_String用Key() throws Exception {
        Key.REPEAT_MODE_MOVIE.getDefaultString();
    }
}
