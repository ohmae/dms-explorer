/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.util.BitmapUtils
import net.mm2d.android.util.Toaster
import net.mm2d.android.util.ViewUtils
import net.mm2d.dmsexplorer.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ImageViewBindingAdapter {
    @SuppressLint("CheckResult")
    @BindingAdapter("imageBinary")
    @JvmStatic
    fun setImageBinary(
        imageView: ImageView,
        binary: ByteArray?
    ) {
        if (binary == null) {
            imageView.setImageDrawable(null)
            return
        }
        createDecoder(imageView, binary)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bitmap -> setImageBitmap(imageView, bitmap) },
                { showToast(imageView.context) }
            )
    }

    private fun createDecoder(
        imageView: ImageView,
        binary: ByteArray?
    ): Single<Bitmap> {
        val width = imageView.width
        val height = imageView.height
        return Single.create { emitter ->
            if (binary == null) {
                emitter.onError(IllegalStateException())
                return@create
            }
            try {
                val bitmap = BitmapUtils.decodeWithOrientation(binary, width, height)
                if (bitmap != null) {
                    emitter.onSuccess(bitmap)
                } else {
                    emitter.onError(IllegalStateException())
                }
            } catch (e: OutOfMemoryError) {
                emitter.onError(e)
            }
        }
    }

    private fun showToast(context: Context) {
        Toaster.show(context, R.string.toast_decode_error)
    }

    private fun setImageBitmap(
        imageView: ImageView,
        bitmap: Bitmap
    ) {
        ViewUtils.execAfterAllocateSize(imageView, { imageView.setImageBitmap(bitmap) })
    }
}
