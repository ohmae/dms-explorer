/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.ActivityUtils;
import net.mm2d.android.util.DrawableUtils;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.util.ToolbarThemeUtils;
import net.mm2d.dmsexplorer.view.ContentListActivity;
import net.mm2d.dmsexplorer.view.adapter.ServerPropertyAdapter;
import net.mm2d.upnp.Icon;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerDetailFragmentModel {
    public final int collapsedColor;
    public final int expandedColor;
    public final Drawable icon;
    public final String title;
    public final ServerPropertyAdapter propertyAdapter;

    private final Context mContext;

    public static ServerDetailFragmentModel create(@NonNull final Context context) {
        final MediaServer server = Repository.getInstance()
                .getControlPointModel().getSelectedMediaServer();
        if (server == null) {
            return null;
        }
        return new ServerDetailFragmentModel(context, server);
    }

    private ServerDetailFragmentModel(@NonNull final Context context,
                                      @NonNull final MediaServer server) {
        mContext = context;
        title = server.getFriendlyName();
        propertyAdapter = new ServerPropertyAdapter(context, server);
        final Bitmap iconBitmap = createIconBitmap(server.getIcon());
        icon = createIconDrawable(context, server, iconBitmap);

        ToolbarThemeUtils.setServerThemeColor(server, iconBitmap);
        expandedColor = server.getIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, Color.BLACK);
        collapsedColor = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
    }

    @Nullable
    private static Bitmap createIconBitmap(@Nullable Icon icon) {
        if (icon == null) {
            return null;
        }
        final byte[] binary = icon.getBinary();
        return BitmapFactory.decodeByteArray(binary, 0, binary.length);
    }

    @NonNull
    private static Drawable createIconDrawable(@NonNull final Context context,
                                               @NonNull final MediaServer server,
                                               @Nullable final Bitmap icon) {
        if (icon != null) {
            return new BitmapDrawable(context.getResources(), icon);
        }
        final Drawable drawable = DrawableUtils.get(context, R.drawable.ic_circle);
        DrawableCompat.setTint(drawable, ThemeUtils.getAccentColor(server.getFriendlyName()));
        return drawable;
    }

    public void onClickFab(View view) {
        final Intent intent = ContentListActivity.makeIntent(mContext);
        mContext.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(view));
    }
}
