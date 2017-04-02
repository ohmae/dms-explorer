/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.ActivityUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding;
import net.mm2d.dmsexplorer.viewmodel.ServerDetailFragmentModel;

/**
 * メディアサーバの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerDetailFragment extends Fragment {
    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @return インスタンス
     */
    public static ServerDetailFragment newInstance() {
        return new ServerDetailFragment();
    }

    private ServerDetailFragmentBinding mBinding;

    public ServerDetailFragmentBinding getBinding() {
        return mBinding;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        mBinding = DataBindingUtil.inflate(inflater, R.layout.server_detail_fragment, container, false);
        final MediaServer server = Repository.getInstance()
                .getControlPointModel().getSelectedMediaServer();
        if (server == null) {
            activity.finish();
            return mBinding.getRoot();
        }
        mBinding.setModel(new ServerDetailFragmentModel(getActivity(), server));

        setUpGoButton(activity, mBinding.fab);
        return mBinding.getRoot();
    }

    public void setUpGoButton(@NonNull final Context context,
                              @NonNull final View button) {
        button.setOnClickListener(view -> {
            final Intent intent = CdsListActivity.makeIntent(context);
            context.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(view));
        });
    }
}
