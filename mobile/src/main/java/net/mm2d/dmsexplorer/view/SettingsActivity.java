/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.app.FragmentBreadCrumbs;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.AttrUtils;
import net.mm2d.dmsexplorer.view.base.AppCompatPreferenceActivity;
import net.mm2d.dmsexplorer.view.eventrouter.EventNotifier;
import net.mm2d.dmsexplorer.view.eventrouter.EventObserver;
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter;

import java.util.ArrayList;
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

    private EventObserver mFinishObserver;
    private FragmentBreadCrumbs mFragmentBreadCrumbs;

    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        Settings.get()
                .getThemeParams()
                .getPreferenceHeaderConverter()
                .convert(target);
        final View title = findViewById(android.R.id.title);
        if (title instanceof FragmentBreadCrumbs) {
            mFragmentBreadCrumbs = (FragmentBreadCrumbs) title;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.get().getThemeParams().getThemeId());
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Repository.get().getThemeModel().setThemeColor(this,
                AttrUtils.resolveColor(this, R.attr.colorPrimary, Color.BLACK),
                ContextCompat.getColor(this, R.color.defaultStatusBar));
        mFinishObserver = EventRouter.createFinishObserver();
        mFinishObserver.register(this::finish);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFinishObserver.unregister();
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void showBreadCrumbs(
            final CharSequence title,
            final CharSequence shortTitle) {
        super.showBreadCrumbs(title, shortTitle);
        setBreadCrumbsTextColor();
    }

    private void setBreadCrumbsTextColor() {
        final int color = AttrUtils.resolveColor(this, R.attr.themeTextColor, Color.BLACK);
        setTextColorRecursively(mFragmentBreadCrumbs, color);
    }

    private void setTextColorRecursively(
            @Nullable final View view,
            @ColorInt final int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        } else if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setTextColorRecursively(group.getChildAt(i), color);
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || PlaybackPreferenceFragment.class.getName().equals(fragmentName)
                || FunctionPreferenceFragment.class.getName().equals(fragmentName)
                || ViewPreferenceFragment.class.getName().equals(fragmentName)
                || ExpertPreferenceFragment.class.getName().equals(fragmentName)
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
        private boolean mSetFromCode;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final Context context = getActivity();
            final EventNotifier finishNotifier = EventRouter.createFinishNotifier();
            addPreferencesFromResource(R.xml.pref_view);
            findPreference(Key.DARK_THEME.name()).setOnPreferenceChangeListener((preference, newValue) -> {
                if (mSetFromCode) {
                    mSetFromCode = false;
                    return true;
                }
                final SwitchPreference switchPreference = (SwitchPreference) preference;
                final boolean checked = switchPreference.isChecked();
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_title_change_theme)
                        .setMessage(R.string.dialog_message_change_theme)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            mSetFromCode = true;
                            switchPreference.setChecked(!checked);
                            finishNotifier.send();
                            new Handler().postDelayed(() -> ServerListActivity.start(context), 500);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return false;
            });
        }
    }

    public static class ExpertPreferenceFragment extends PreferenceFragment {
        private static final String[] ORIENTATION_KEYS = new String[]{
                Key.ORIENTATION_BROWSE.name(),
                Key.ORIENTATION_MOVIE.name(),
                Key.ORIENTATION_MUSIC.name(),
                Key.ORIENTATION_PHOTO.name(),
                Key.ORIENTATION_DMC.name(),
        };
        private EventNotifier mOrientationSettingsNotifier;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mOrientationSettingsNotifier = EventRouter.createOrientationSettingsNotifier();
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            addPreferencesFromResource(R.xml.pref_expert);
            final List<ListPreference> preferences = new ArrayList<>();
            for (final String key : ORIENTATION_KEYS) {
                preferences.add((ListPreference) findPreference(key));
            }
            final OnPreferenceChangeListener listener = createBindSummaryListener();
            for (final Preference p : preferences) {
                p.setOnPreferenceChangeListener(listener);
                final String value = sharedPreferences.getString(p.getKey(), "");
                listener.onPreferenceChange(p, value);
            }
            findPreference(Key.ORIENTATION_COLLECTIVE.name())
                    .setOnPreferenceChangeListener(createCollectiveSettingListener(preferences));
        }

        @NonNull
        private OnPreferenceChangeListener createBindSummaryListener() {
            return (preference, value) -> {
                final Orientation orientation = Orientation.of(value.toString());
                preference.setSummary(orientation.getName(preference.getContext()));
                mOrientationSettingsNotifier.send();
                return true;
            };
        }

        @NonNull
        private OnPreferenceChangeListener createCollectiveSettingListener(@NonNull final List<ListPreference> preferences) {
            return (preference, value) -> {
                final String stringValue = value.toString();
                final String summary = Orientation.of(stringValue).getName(preference.getContext());
                for (final ListPreference p : preferences) {
                    p.setValue(stringValue);
                    p.setSummary(summary);
                }
                mOrientationSettingsNotifier.send();
                return false; // Do not write the value of collective setting
            };
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
            final Settings settings = Settings.get();
            findPreference(Key.LICENSE.name()).setOnPreferenceClickListener(preference -> {
                final String query = settings.getThemeParams().getHtmlQuery();
                WebViewActivity.start(getActivity(),
                        getString(R.string.pref_title_license),
                        Const.URL_OPEN_SOURCE_LICENSE + "?" + query);
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
