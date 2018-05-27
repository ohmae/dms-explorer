/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme;

import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import net.mm2d.preference.Header;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ThemeParams {
    public interface PreferenceHeaderConverter {
        void convert(@NonNull List<Header> headers);
    }

    public static class Builder {
        private String mHtmlQuery;
        @StyleRes
        private int mThemeId;
        @StyleRes
        private int mNoActionBarThemeId;
        @StyleRes
        private int mListThemeId;
        @StyleRes
        private int mFullscreenThemeId;
        @StyleRes
        private int mSettingsThemeId;
        @StyleRes
        private int mPopupThemeId;
        private PreferenceHeaderConverter mPreferenceHeaderConverter;
        private ThemeColorGenerator mThemeColorGenerator;
        private ServerColorExtractor mServerColorExtractor;

        public Builder setHtmlQuery(@NonNull final String query) {
            mHtmlQuery = query;
            return this;
        }

        public Builder setThemeId(@StyleRes final int id) {
            mThemeId = id;
            return this;
        }

        public Builder setNoActionBarThemeId(@StyleRes final int id) {
            mNoActionBarThemeId = id;
            return this;
        }

        public Builder setListThemeId(@StyleRes final int id) {
            mListThemeId = id;
            return this;
        }

        public Builder setFullscreenThemeId(@StyleRes final int id) {
            mFullscreenThemeId = id;
            return this;
        }

        public Builder setSettingsThemeId(@StyleRes final int id) {
            mSettingsThemeId = id;
            return this;
        }

        public Builder setPopupThemeId(@StyleRes final int id) {
            mPopupThemeId = id;
            return this;
        }

        public Builder setPreferenceHeaderConverter(final PreferenceHeaderConverter converter) {
            mPreferenceHeaderConverter = converter;
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

        public ThemeParams build() {
            if (mHtmlQuery == null ||
                    mPreferenceHeaderConverter == null ||
                    mThemeColorGenerator == null ||
                    mServerColorExtractor == null) {
                throw new IllegalStateException();
            }
            return new ThemeParams(this);
        }
    }

    @NonNull
    private final String mHtmlQuery;
    @StyleRes
    private final int mThemeId;
    @StyleRes
    private final int mNoActionBarThemeId;
    @StyleRes
    private final int mListThemeId;
    @StyleRes
    private final int mFullscreenThemeId;
    @StyleRes
    private final int mSettingsThemeId;
    @StyleRes
    private final int mPopupThemeId;
    @NonNull
    private final PreferenceHeaderConverter mPreferenceHeaderConverter;
    @NonNull
    private final ThemeColorGenerator mThemeColorGenerator;
    @NonNull
    private final ServerColorExtractor mServerColorExtractor;

    private ThemeParams(@NonNull final Builder builder) {
        mHtmlQuery = builder.mHtmlQuery;
        mThemeId = builder.mThemeId;
        mNoActionBarThemeId = builder.mNoActionBarThemeId;
        mListThemeId = builder.mListThemeId;
        mFullscreenThemeId = builder.mFullscreenThemeId;
        mSettingsThemeId = builder.mSettingsThemeId;
        mPopupThemeId = builder.mPopupThemeId;
        mPreferenceHeaderConverter = builder.mPreferenceHeaderConverter;
        mThemeColorGenerator = builder.mThemeColorGenerator;
        mServerColorExtractor = builder.mServerColorExtractor;
    }

    @NonNull
    public String getHtmlQuery() {
        return mHtmlQuery;
    }

    @StyleRes
    public int getThemeId() {
        return mThemeId;
    }

    @StyleRes
    public int getNoActionBarThemeId() {
        return mNoActionBarThemeId;
    }

    @StyleRes
    public int getListThemeId() {
        return mListThemeId;
    }

    @StyleRes
    public int getFullscreenThemeId() {
        return mFullscreenThemeId;
    }

    @StyleRes
    public int getSettingsThemeId() {
        return mSettingsThemeId;
    }

    @StyleRes
    public int getPopupThemeId() {
        return mPopupThemeId;
    }

    @NonNull
    public PreferenceHeaderConverter getPreferenceHeaderConverter() {
        return mPreferenceHeaderConverter;
    }

    @NonNull
    public ThemeColorGenerator getThemeColorGenerator() {
        return mThemeColorGenerator;
    }

    @NonNull
    public ServerColorExtractor getServerColorExtractor() {
        return mServerColorExtractor;
    }
}
