/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import net.mm2d.android.util.LaunchUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.domain.CustomTabsSessionHelper;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class OpenUriModelImpl implements OpenUriModel {
    @NonNull
    private final CustomTabsSessionHelper mHelper;

    public OpenUriModelImpl(@NonNull final CustomTabsSessionHelper helper) {
        mHelper = helper;
    }

    @Override
    public void openUri(@NonNull final Context context, @NonNull final String uri) {
        if (!openUriOnCustomTabs(context, uri)) {
            LaunchUtils.openUri(context, uri);
        }
    }

    private boolean openUriOnCustomTabs(@NonNull final Context context, @NonNull final String uri) {
        final String packageNameToBind = mHelper.getPackageNameToBind();
        if (TextUtils.isEmpty(packageNameToBind)) {
            return false;
        }
        final CustomTabsIntent intent = new CustomTabsIntent.Builder(mHelper.getSession())
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.primary))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build();
        intent.intent.setPackage(packageNameToBind);
        try {
            intent.launchUrl(context, Uri.parse(uri));
        } catch (final ActivityNotFoundException ignored) {
            return false;
        }
        return true;
    }
}
