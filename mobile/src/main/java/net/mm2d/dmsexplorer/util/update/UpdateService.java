/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import net.mm2d.dmsexplorer.Const;

import io.reactivex.Single;
import retrofit2.http.GET;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface UpdateService {
    @GET(Const.URL_UPDATE_PATH)
    Single<UpdateInfo> get();
}
