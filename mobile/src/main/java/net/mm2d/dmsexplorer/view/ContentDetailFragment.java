/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding;
import net.mm2d.dmsexplorer.viewmodel.ContentDetailFragmentModel;

/**
 * CDSアイテムの詳細情報を表示するFragment。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ContentDetailFragment extends Fragment {
    /**
     * インスタンスを作成する。
     *
     * <p>Bundleの設定と読み出しをこのクラス内で完結させる。
     *
     * @return インスタンス。
     */
    @NonNull
    public static ContentDetailFragment newInstance() {
        return new ContentDetailFragment();
    }

    private ContentDetailFragmentModel mModel;

    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final ContentDetailFragmentBinding binding
                = DataBindingUtil.inflate(inflater, R.layout.content_detail_fragment, container, false);
        try {
            mModel = new ContentDetailFragmentModel(getActivity(), Repository.get());
            binding.setModel(mModel);
        } catch (final IllegalStateException ignored) {
            getActivity().finish();
            return binding.getRoot();
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mModel != null) {
            mModel.terminate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mModel != null) {
            mModel.onResume();
        }
    }
}
