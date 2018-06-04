/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.util.SparseArray

/**
 * ARIB特有の処理を行うユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object AribUtils {
    private val array = SparseArray<String>().apply {
        // 四角で囲んだ文字、フォントが無いため文字列で置換
        put(0xE0F8, "[HV]")
        put(0xE0F9, "[SD]")
        put(0xE0FA, "[P]")
        put(0xE0FB, "[W]")
        put(0xE0FC, "[MV]")
        put(0xE0FD, "[手]")
        put(0xE0FE, "[字]")
        put(0xE0FF, "[双]")
        put(0xE180, "[デ]")
        put(0xE181, "[S]")
        put(0xE182, "[二]")
        put(0xE183, "[多]")
        put(0xE184, "[解]")
        put(0xE185, "[SS]")
        put(0xE186, "[B]")
        put(0xE187, "[N]")
        put(0xE188, "■")
        put(0xE189, "●")
        put(0xE18A, "[天]")
        put(0xE18B, "[交]")
        put(0xE18C, "[映]")
        put(0xE18D, "[無]")
        put(0xE18E, "[料]")
        put(0xE18F, "[鍵]")
        put(0xE190, "[前]")
        put(0xE191, "[後]")
        put(0xE192, "[再]")
        put(0xE193, "[新]")
        put(0xE194, "[初]")
        put(0xE195, "[終]")
        put(0xE196, "[生]")
        put(0xE197, "[販]")
        put(0xE198, "[声]")
        put(0xE199, "[吹]")
        put(0xE19A, "[PPV]")
        put(0xE19B, "[秘]")
        put(0xE19C, "[ほか]")
        // かつて外字に割り当てられていたがUnicodeに追加されたもの、Unicodeに置換
        put(0xE080, "㐂")
        put(0xE082, "㔟")
        put(0xE083, "詹")
        put(0xE085, "﨑")
        put(0xE086, "㟢")
        put(0xE08F, "㻚")
        put(0xE090, "䂓")
        put(0xE094, "䉤")
        put(0xE2F0, "⁉")
        put(0xE2FF, "㉑")
        put(0xE380, "㉒")
        put(0xE381, "㉓")
        put(0xE382, "㉔")
        put(0xE39D, "㉕")
        put(0xE39E, "㉖")
        put(0xE39F, "㉗")
        put(0xE3A0, "㉘")
        put(0xE3A1, "㉙")
        put(0xE3A2, "㉚")
        put(0xE3A3, "⓫")
        put(0xE3A4, "⓬")
        put(0xE3A5, "㉛")
    }

    /**
     * 文字列に含まれるARIB外字を通常のフォントでも表示できる文字コードに変換する。
     *
     * @param string 変換する文字列
     * @return 変換後の文字列
     */
    @JvmStatic
    fun toDisplayableString(string: String): String {
        val sb = StringBuilder()
        string.toCharArray().forEach {
            sb.append(array.get(it.toInt(), it.toString()))
        }
        return sb.toString()
    }
}
