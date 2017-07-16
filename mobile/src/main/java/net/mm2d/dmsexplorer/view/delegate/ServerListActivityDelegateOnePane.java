/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.SharedElementCallback;
import android.transition.Transition;
import android.util.Pair;
import android.view.View;

import net.mm2d.android.util.ActivityUtils;
import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding;
import net.mm2d.dmsexplorer.view.ServerDetailActivity;
import net.mm2d.dmsexplorer.view.base.BaseActivity;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ServerListActivityDelegateOnePane extends ServerListActivityDelegate {
    private static final String KEY_HAS_REENTER_TRANSITION = "KEY_HAS_REENTER_TRANSITION";
    private boolean mHasReenterTransition;

    ServerListActivityDelegateOnePane(@NonNull final BaseActivity activity,
                                      @NonNull final ServerListActivityBinding binding) {
        super(activity, binding);
    }

    @Override
    protected boolean isTwoPane() {
        return false;
    }

    @Override
    public void onSelect(@NonNull final View v) {
        startServerDetailActivity(v);
    }

    @Override
    public void onLostSelection() {
    }

    @Override
    public void onExecute(@NonNull final View v) {
        startCdsListActivity(getActivity(), v);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mHasReenterTransition = savedInstanceState.getBoolean(KEY_HAS_REENTER_TRANSITION);
        }
        setSharedElementCallback();
    }

    private void setSharedElementCallback() {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        getActivity().setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(final List<String> names, final Map<String, View> sharedElements) {
                sharedElements.clear();
                final View shared = getBinding().getModel().findSharedView();
                if (shared != null) {
                    sharedElements.put(Const.SHARE_ELEMENT_NAME_DEVICE_ICON, shared);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_REENTER_TRANSITION, mHasReenterTransition);
    }

    @Override
    public void onStart() {
        if (mHasReenterTransition) {
            mHasReenterTransition = false;
            execAfterTransitionOnce(() -> getBinding().getModel().updateListAdapter());
            return;
        }
        getBinding().getModel().updateListAdapter();
    }

    private void execAfterTransitionOnce(@NonNull final Runnable task) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        getActivity().getWindow().getSharedElementExitTransition().addListener(new TransitionListenerAdapter() {
            @TargetApi(VERSION_CODES.KITKAT)
            @Override
            public void onTransitionEnd(Transition transition) {
                task.run();
                transition.removeListener(this);
            }
        });

    }

    private void startServerDetailActivity(@NonNull final View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServerDetailActivityLollipop(v);
        } else {
            startServerDetailActivityJellyBean(v);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void startServerDetailActivityLollipop(@NonNull final View v) {
        final Intent intent = ServerDetailActivity.makeIntent(getActivity());
        final View accent = v.findViewById(R.id.accent);
        getActivity().startActivity(intent, ActivityOptions
                .makeSceneTransitionAnimation(getActivity(),
                        new Pair<>(accent, Const.SHARE_ELEMENT_NAME_DEVICE_ICON))
                .toBundle());
        mHasReenterTransition = true;
    }

    private void startServerDetailActivityJellyBean(@NonNull final View v) {
        final Intent intent = ServerDetailActivity.makeIntent(getActivity());
        getActivity().startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v));
    }
}
