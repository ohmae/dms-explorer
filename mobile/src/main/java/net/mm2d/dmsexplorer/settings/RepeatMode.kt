/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import android.util.SparseArray
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.mm2d.dmsexplorer.R

/**
 * 連続再生モードを表現するenum。
 *
 *
 * 設定としてname()の文字列を保存しているため、シンボル名の変更を行う場合はマイグレーション処理必須
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 *
 * @constructor
 * @param order     トグル順序
 * @param iconId    モードアイコンのID
 * @param messageId Toastで表示するメッセージのID
 */
enum class RepeatMode(
    private val order: Int,
    @DrawableRes val iconId: Int,
    @StringRes val messageId: Int
) {
    /**
     * １項目のみ再生。
     */
    PLAY_ONCE(
        0,
        R.drawable.ic_play_once,
        R.string.toast_repeat_play_once
    ),
    /**
     * フォルダ内を最後まで連続再生。
     */
    SEQUENTIAL(
        1,
        R.drawable.ic_sequential,
        R.string.toast_repeat_sequential
    ),
    /**
     * 全体をループ再生。
     */
    REPEAT_ALL(
        2,
        R.drawable.ic_repeat_all,
        R.string.toast_repeat_repeat_all
    ),
    /**
     * １項目をループ再生。
     */
    REPEAT_ONE(
        3,
        R.drawable.ic_repeat_one,
        R.string.toast_repeat_repeat_one
    );

    /**
     * トグルしたときの次のモードを返す。
     *
     * @return 次のモード
     */
    operator fun next(): RepeatMode {
        return orderOf((order + 1) % length)
    }

    companion object {
        private val orderMap: SparseArray<RepeatMode>
        private val nameMap = values().map { it.name to it }.toMap()
        private val length: Int

        init {
            val values = values()
            length = values.size
            orderMap = SparseArray()
            values().forEach {
                orderMap.put(it.order, it)
            }
        }

        /**
         * 順序値から該当するEnum値を返す。
         *
         * @param order 順序値
         * @return 該当するEnum値、該当するものがない場合デフォルト値
         */
        private fun orderOf(order: Int): RepeatMode {
            return orderMap.get(order, PLAY_ONCE)
        }

        /**
         * モードを表現する文字列から該当するEnum値を返す。
         *
         * @param value モードを表現する文字列
         * @return 該当するEnum、該当するものがない場合デフォルト値
         */
        fun of(value: String): RepeatMode {
            val mode = nameMap[value]
            return mode ?: PLAY_ONCE
        }
    }
}
