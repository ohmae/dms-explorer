/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceChangeListener
import net.mm2d.android.util.LaunchUtils
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.tabs.CustomTabsHelper
import net.mm2d.dmsexplorer.settings.Key
import net.mm2d.dmsexplorer.settings.Orientation
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.view.base.PreferenceFragmentBase
import net.mm2d.dmsexplorer.view.eventrouter.EventNotifier
import net.mm2d.dmsexplorer.view.eventrouter.EventObserver
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import net.mm2d.preference.Header
import net.mm2d.preference.PreferenceActivityCompat

/**
 * アプリ設定を行うActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SettingsActivity : PreferenceActivityCompat() {
    private lateinit var finishObserver: EventObserver

    override fun onBuildHeaders(target: MutableList<Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
        Settings.get()
            .themeParams
            .preferenceHeaderConverter
            .convert(target)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Settings.get().themeParams.settingsThemeId)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Repository.get().themeModel.setThemeColor(
            this,
            AttrUtils.resolveColor(this, R.attr.colorPrimary, Color.BLACK),
            ContextCompat.getColor(this, R.color.defaultStatusBar)
        )
        finishObserver = EventRouter.createFinishObserver().also {
            it.register { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishObserver.unregister()
    }

    override fun onIsMultiPane(): Boolean = isXLargeTablet(this)

    override fun isValidFragment(fragmentName: String?): Boolean =
        (PreferenceFragmentCompat::class.java.name == fragmentName
            || PlaybackPreferenceFragment::class.java.name == fragmentName
            || FunctionPreferenceFragment::class.java.name == fragmentName
            || ViewPreferenceFragment::class.java.name == fragmentName
            || ExpertPreferenceFragment::class.java.name == fragmentName
            || InformationPreferenceFragment::class.java.name == fragmentName)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class PlaybackPreferenceFragment : PreferenceFragmentBase() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_playback)
        }
    }

    class FunctionPreferenceFragment : PreferenceFragmentBase() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_function)
            setUpCustomTabs()
        }

        private fun setUpCustomTabs() {
            val customTabs: SwitchPreference = findPreference(Key.USE_CUSTOM_TABS.name) ?: return
            customTabs.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    Repository.get().openUriModel.setUseCustomTabs(newValue)
                }
                true
            }
            if (canUseChromeCustomTabs()) {
                return
            }
            if (customTabs.isChecked) {
                customTabs.isChecked = false
            }
            customTabs.isEnabled = false
        }
    }

    class ViewPreferenceFragment : PreferenceFragmentBase() {
        private var setFromCode: Boolean = false

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val context = activity
            val finishNotifier = EventRouter.createFinishNotifier()
            addPreferencesFromResource(R.xml.pref_view)
            findPreference<Preference>(Key.DARK_THEME.name)?.setOnPreferenceChangeListener { preference, _ ->
                if (setFromCode) {
                    setFromCode = false
                    return@setOnPreferenceChangeListener true
                }
                val switchPreference = preference as SwitchPreference
                val checked = switchPreference.isChecked
                AlertDialog.Builder(context!!)
                    .setTitle(R.string.dialog_title_change_theme)
                    .setMessage(R.string.dialog_message_change_theme)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        setFromCode = true
                        switchPreference.isChecked = !checked
                        finishNotifier.send()
                        Handler().postDelayed({ ServerListActivity.start(context) }, 500)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                false
            }
        }
    }

    class ExpertPreferenceFragment : PreferenceFragmentBase() {
        private lateinit var orientationSettingsNotifier: EventNotifier

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            orientationSettingsNotifier = EventRouter.createOrientationSettingsNotifier()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity!!)
            addPreferencesFromResource(R.xml.pref_expert)
            val preferences = ORIENTATION_KEYS.mapNotNull { findPreference<ListPreference>(it) }
            val listener = createBindSummaryListener()
            for (p in preferences) {
                p.onPreferenceChangeListener = listener
                val value = sharedPreferences.getString(p.key, "")
                listener.onPreferenceChange(p, value)
            }
            findPreference<Preference>(Key.ORIENTATION_COLLECTIVE.name)
                ?.onPreferenceChangeListener = createCollectiveSettingListener(preferences)
        }

        private fun createBindSummaryListener(): OnPreferenceChangeListener {
            return OnPreferenceChangeListener { preference, value ->
                val orientation = Orientation.of(value.toString())
                preference.summary = orientation.getName(preference.context)
                orientationSettingsNotifier.send()
                true
            }
        }

        private fun createCollectiveSettingListener(preferences: List<ListPreference>): OnPreferenceChangeListener {
            return OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()
                val summary = Orientation.of(stringValue).getName(preference.context)
                for (p in preferences) {
                    p.value = stringValue
                    p.summary = summary
                }
                orientationSettingsNotifier.send()
                false // Do not write the value of collective setting
            }
        }

        companion object {
            private val ORIENTATION_KEYS = arrayOf(
                Key.ORIENTATION_BROWSE.name,
                Key.ORIENTATION_MOVIE.name,
                Key.ORIENTATION_MUSIC.name,
                Key.ORIENTATION_PHOTO.name,
                Key.ORIENTATION_DMC.name
            )
        }
    }

    class InformationPreferenceFragment : PreferenceFragmentBase() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val activity = activity!!
            addPreferencesFromResource(R.xml.pref_information)
            findPreference<Preference>(Key.PLAY_STORE.name)?.setOnPreferenceClickListener {
                LaunchUtils.openGooglePlay(activity, Const.PACKAGE_NAME)
                true
            }
            findPreference<Preference>(Key.VERSION_NUMBER.name)?.summary = BuildConfig.VERSION_NAME
            findPreference<Preference>(Key.SOURCE_CODE.name)?.setOnPreferenceClickListener {
                openUrl(activity, Const.URL_GITHUB_PROJECT)
                true
            }
            findPreference<Preference>(Key.PRIVACY_POLICY.name)?.setOnPreferenceClickListener {
                openUrl(activity, Const.URL_PRIVACY_POLICY)
                true
            }
            val settings = Settings.get()
            findPreference<Preference>(Key.LICENSE.name)?.setOnPreferenceClickListener {
                val query = settings.themeParams.htmlQuery
                WebViewActivity.start(
                    activity,
                    getString(R.string.pref_title_license),
                    Const.URL_OPEN_SOURCE_LICENSE + "?" + query
                )
                true
            }
        }
    }

    companion object {
        /**
         * このActivityを起動するためのIntentを作成する。
         *
         * Extraの設定と読み出しをこのクラス内で完結させる。
         * 現時点ではExtraは設定していない。
         *
         * @param context コンテキスト
         * @return このActivityを起動するためのIntent
         */
        fun makeIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)

        fun start(context: Context) {
            context.startActivity(makeIntent(context))
        }

        private fun isXLargeTablet(context: Context): Boolean =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

        private fun canUseChromeCustomTabs(): Boolean =
            !CustomTabsHelper.packageNameToBind.isNullOrEmpty()

        private fun openUrl(context: Context, url: String) {
            Repository.get().openUriModel.openUri(context, url)
        }
    }
}
