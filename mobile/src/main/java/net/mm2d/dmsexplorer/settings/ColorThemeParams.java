/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.preference.PreferenceActivity.Header;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import net.mm2d.dmsexplorer.util.ServerColorExtractor;
import net.mm2d.dmsexplorer.util.ThemeColorGenerator;

import java.util.List;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class ColorThemeParams {
    public interface PreferenceHeaderConverter {
        void convert(@NonNull List<Header> headers);
    }

    public static class Builder {
        private String mHtmlQuery;
        @StyleRes
        private int mTheme;
        @StyleRes
        private int mThemeNoActionBar;
        @StyleRes
        private int mThemeList;
        @StyleRes
        private int mThemeFullscreen;
        @StyleRes
        private int mThemePopup;
        private PreferenceHeaderConverter mConverter;
        private ThemeColorGenerator mThemeColorGenerator;
        private ServerColorExtractor mServerColorExtractor;

        public Builder setHtmlQuery(@NonNull final String query) {
            mHtmlQuery = query;
            return this;
        }

        public Builder setTheme(@StyleRes final int id) {
            mTheme = id;
            return this;
        }

        public Builder setThemeNoActionBar(@StyleRes final int id) {
            mThemeNoActionBar = id;
            return this;
        }

        public Builder setThemeList(@StyleRes final int id) {
            mThemeList = id;
            return this;
        }

        public Builder setThemeFullscreen(@StyleRes final int id) {
            mThemeFullscreen = id;
            return this;
        }

        public Builder setThemePopup(@StyleRes final int id) {
            mThemePopup = id;
            return this;
        }

        public Builder setPreferenceHeaderConverter(final PreferenceHeaderConverter converter) {
            mConverter = converter;
            return this;
        }

        public Builder setThemeColorGenerator(final ThemeColorGenerator generator) {
            mThemeColorGenerator = generator;
            return this;
        }

        public Builder setServerColorExtractor(final ServerColorExtractor extractor) {
            mServerColorExtractor = extractor;
            return this;
        }

        public ColorThemeParams build() {
            return new ColorThemeParams(this);
        }
    }

    private final String mHtmlQuery;
    @StyleRes
    private final int mTheme;
    @StyleRes
    private final int mThemeNoActionBar;
    @StyleRes
    private final int mThemeList;
    @StyleRes
    private final int mThemeFullscreen;
    @StyleRes
    private final int mThemePopup;
    private final PreferenceHeaderConverter mConverter;
    private final ThemeColorGenerator mThemeColorGenerator;
    private ServerColorExtractor mServerColorExtractor;

    private ColorThemeParams(@NonNull final Builder builder) {
        mHtmlQuery = builder.mHtmlQuery;
        mTheme = builder.mTheme;
        mThemeNoActionBar = builder.mThemeNoActionBar;
        mThemeList = builder.mThemeList;
        mThemeFullscreen = builder.mThemeFullscreen;
        mThemePopup = builder.mThemePopup;
        mConverter = builder.mConverter;
        mThemeColorGenerator = builder.mThemeColorGenerator;
        mServerColorExtractor = builder.mServerColorExtractor;
    }

    public String getHtmlQuery() {
        return mHtmlQuery;
    }

    @StyleRes
    public int getTheme() {
        return mTheme;
    }

    @StyleRes
    public int getThemeNoActionBar() {
        return mThemeNoActionBar;
    }

    @StyleRes
    public int getThemeList() {
        return mThemeList;
    }

    @StyleRes
    public int getThemeFullscreen() {
        return mThemeFullscreen;
    }

    @StyleRes
    public int getThemePopup() {
        return mThemePopup;
    }

    public PreferenceHeaderConverter getPreferenceHeaderConverter() {
        return mConverter;
    }

    public ThemeColorGenerator getThemeColorGenerator() {
        return mThemeColorGenerator;
    }

    public ServerColorExtractor getServerColorExtractor() {
        return mServerColorExtractor;
    }
}
