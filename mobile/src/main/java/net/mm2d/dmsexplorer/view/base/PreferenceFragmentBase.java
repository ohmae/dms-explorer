/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import net.mm2d.dmsexplorer.util.ViewLayoutUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public abstract class PreferenceFragmentBase extends PreferenceFragmentCompat {
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        getListView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        ViewLayoutUtils.setPaddingBottom(container, 0);
        return view;
    }

    @Override
    protected Adapter onCreateAdapter(final PreferenceScreen preferenceScreen) {
        return new CustomPreferenceGroupAdapter(preferenceScreen);
    }

    @SuppressLint("RestrictedApi")
    private static class CustomPreferenceGroupAdapter extends PreferenceGroupAdapter {
        CustomPreferenceGroupAdapter(final PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        @NonNull
        @Override
        public PreferenceViewHolder onCreateViewHolder(
                final ViewGroup parent,
                final int viewType) {
            final PreferenceViewHolder holder = super.onCreateViewHolder(parent, viewType);
            final View icon = holder.findViewById(android.R.id.icon);
            if (icon != null) {
                final ViewParent p = icon.getParent();
                if (p instanceof View) {
                    final View parentView = (View) p;
                    parentView.setMinimumWidth(0);
                    parentView.setPadding(0, 0, 0, 0);
                }
            }
            return holder;
        }
    }
}
