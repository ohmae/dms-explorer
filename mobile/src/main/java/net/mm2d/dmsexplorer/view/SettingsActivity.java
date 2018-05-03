/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.format.DateFormat;

import net.mm2d.android.util.LaunchUtils;
import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.CustomTabsHelper;
import net.mm2d.dmsexplorer.domain.model.OpenUriCustomTabsModel;
import net.mm2d.dmsexplorer.domain.model.OpenUriModel;
import net.mm2d.dmsexplorer.settings.Key;
import net.mm2d.dmsexplorer.settings.Orientation;
import net.mm2d.dmsexplorer.util.ViewSettingsNotifier;
import net.mm2d.dmsexplorer.view.base.AppCompatPreferenceActivity;

import java.util.List;

/**
 * アプリ設定を行うActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     * 現時点ではExtraは設定していない。
     *
     * @param context コンテキスト
     * @return このActivityを起動するためのIntent
     */
    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    public static void start(@NonNull Context context) {
        context.startActivity(makeIntent(context));
    }

    private static boolean isXLargeTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Repository.get().getThemeModel().setThemeColor(this,
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.defaultStatusBar));
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || PlaybackPreferenceFragment.class.getName().equals(fragmentName)
                || FunctionPreferenceFragment.class.getName().equals(fragmentName)
                || ViewPreferenceFragment.class.getName().equals(fragmentName)
                || InformationPreferenceFragment.class.getName().equals(fragmentName);
    }

    private static boolean canUseChromeCustomTabs() {
        return !TextUtils.isEmpty(CustomTabsHelper.getPackageNameToBind());
    }

    private static void openUrl(
            @NonNull final Context context,
            @NonNull final String url) {
        Repository.get().getOpenUriModel().openUri(context, url);
    }

    public static class PlaybackPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_playback);
        }
    }

    public static class FunctionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_function);
            setUpCustomTabs();
        }

        private void setUpCustomTabs() {
            final SwitchPreference customTabs = (SwitchPreference) findPreference(Key.USE_CUSTOM_TABS.name());
            customTabs.setOnPreferenceChangeListener((preference, newValue) -> {
                final OpenUriModel model = Repository.get().getOpenUriModel();
                if ((newValue instanceof Boolean) && (model instanceof OpenUriCustomTabsModel)) {
                    ((OpenUriCustomTabsModel) model).setUseCustomTabs((Boolean) newValue);
                }
                return true;
            });
            if (canUseChromeCustomTabs()) {
                return;
            }
            if (customTabs.isChecked()) {
                customTabs.setChecked(false);
            }
            customTabs.setEnabled(false);
        }
    }

    public static class ViewPreferenceFragment extends PreferenceFragment {
        private SharedPreferences mSharedPreferences;
        private ViewSettingsNotifier mViewSettingsNotifier;

        private final OnPreferenceChangeListener mBindSummaryListener = (preference, value) -> {
            final Orientation orientation = Orientation.of(value.toString());
            preference.setSummary(orientation.getName(preference.getContext()));
            mViewSettingsNotifier.update();
            return true;
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mViewSettingsNotifier = new ViewSettingsNotifier(getActivity());
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            addPreferencesFromResource(R.xml.pref_view);
            bindOrientationSummary(findPreference(Key.ORIENTATION_BROWSE.name()));
            bindOrientationSummary(findPreference(Key.ORIENTATION_MOVIE.name()));
            bindOrientationSummary(findPreference(Key.ORIENTATION_MUSIC.name()));
            bindOrientationSummary(findPreference(Key.ORIENTATION_PHOTO.name()));
            bindOrientationSummary(findPreference(Key.ORIENTATION_DMC.name()));
        }

        private void bindOrientationSummary(@NonNull final Preference preference) {
            preference.setOnPreferenceChangeListener(mBindSummaryListener);
            mBindSummaryListener.onPreferenceChange(preference,
                    mSharedPreferences.getString(preference.getKey(), ""));
        }
    }

    public static class InformationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_information);
            findPreference(Key.PLAY_STORE.name()).setOnPreferenceClickListener(preference -> {
                final Context context = preference.getContext();
                LaunchUtils.openGooglePlay(context, Const.PACKAGE_NAME);
                return true;
            });
            findPreference(Key.VERSION_NUMBER.name()).setSummary(makeVersionInfo());
            findPreference(Key.SOURCE_CODE.name()).setOnPreferenceClickListener(preference -> {
                openUrl(getActivity(), Const.URL_GITHUB_PROJECT);
                return true;
            });
            findPreference(Key.LICENSE.name()).setOnPreferenceClickListener(preference -> {
                WebViewActivity.start(getActivity(),
                        getString(R.string.pref_title_license),
                        Const.URL_OPEN_SOURCE_LICENSE);
                return true;
            });
        }

        private String makeVersionInfo() {
            if (BuildConfig.DEBUG) {
                return BuildConfig.VERSION_NAME + " # "
                        + DateFormat.format("yyyy/M/d kk:mm:ss", BuildConfig.BUILD_TIME);
            }
            return BuildConfig.VERSION_NAME;
        }
    }
}
