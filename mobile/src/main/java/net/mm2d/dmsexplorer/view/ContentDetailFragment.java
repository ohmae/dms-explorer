/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding;
import net.mm2d.dmsexplorer.util.ItemSelectUtils;
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

    private ContentDetailFragmentBinding mBinding;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final Repository repository = Repository.getInstance();
        final MediaServer server = repository.getControlPointModel().getSelectedMediaServer();
        final CdsObject object = repository.getMediaServerModel().getSelectedObject();
        mBinding = DataBindingUtil.inflate(inflater, R.layout.content_detail_fragment, container, false);
        if (server == null || object == null) {
            getActivity().finish();
            return mBinding.getRoot();
        }
        mBinding.setModel(new ContentDetailFragmentModel(getActivity(), object));

        setUpPlayButton(getActivity(), mBinding.fabPlay, object);
        setUpSendButton(getActivity(), mBinding.fabSend, server.getUdn(), object);
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.getModel().terminate();
    }

    private static void setUpPlayButton(
            @NonNull final Activity activity,
            @NonNull final FloatingActionButton fab,
            @NonNull final CdsObject object) {
        final boolean protectedResource = object.hasProtectedResource();
        if (protectedResource) {
            fab.setOnClickListener(ContentDetailFragment::showNotSupportDrmSnackbar);
            fab.setOnLongClickListener(view -> {
                Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
                return true;
            });
            return;
        }
        fab.setOnClickListener(view -> ItemSelectUtils.play(activity, object, 0));
        fab.setOnLongClickListener(view -> {
            ItemSelectUtils.play(activity, object);
            return true;
        });
    }

    private static void showNotSupportDrmSnackbar(@NonNull View view) {
        Snackbar.make(view, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show();
    }

    private static void setUpSendButton(
            @NonNull final Activity activity,
            @NonNull final FloatingActionButton fab,
            @NonNull final String udn,
            @NonNull final CdsObject object) {
        final MrControlPoint cp = Repository.getInstance().getControlPointModel().getMrControlPoint();
        if (cp.getDeviceListSize() == 0 || !hasResource(object)) {
            return;
        }
        fab.setOnClickListener(v -> ItemSelectUtils.send(activity, udn, object));
    }

    private static boolean hasResource(@NonNull final CdsObject object) {
        return object.getTagList(CdsObject.RES) != null;
    }
}
