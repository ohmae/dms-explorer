/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

/**
 * ARIB特有の処理を行うユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object AribUtils {
    private val map: Map<Int, String> = mapOf(
            // 四角で囲んだ文字、フォントが無いため文字列で置換
            0xE0F8 to "[HV]",
            0xE0F9 to "[SD]",
            0xE0FA to "[P]",
            0xE0FB to "[W]",
            0xE0FC to "[MV]",
            0xE0FD to "[手]",
            0xE0FE to "[字]",
            0xE0FF to "[双]",
            0xE180 to "[デ]",
            0xE181 to "[S]",
            0xE182 to "[二]",
            0xE183 to "[多]",
            0xE184 to "[解]",
            0xE185 to "[SS]",
            0xE186 to "[B]",
            0xE187 to "[N]",
            0xE188 to "■",
            0xE189 to "●",
            0xE18A to "[天]",
            0xE18B to "[交]",
            0xE18C to "[映]",
            0xE18D to "[無]",
            0xE18E to "[料]",
            0xE18F to "[鍵]",
            0xE190 to "[前]",
            0xE191 to "[後]",
            0xE192 to "[再]",
            0xE193 to "[新]",
            0xE194 to "[初]",
            0xE195 to "[終]",
            0xE196 to "[生]",
            0xE197 to "[販]",
            0xE198 to "[声]",
            0xE199 to "[吹]",
            0xE19A to "[PPV]",
            0xE19B to "[秘]",
            0xE19C to "[ほか]",
            // かつて外字に割り当てられていたがUnicodeに追加されたもの、Unicodeに置換
            0xE080 to "㐂",
            0xE082 to "㔟",
            0xE083 to "詹",
            0xE085 to "﨑",
            0xE086 to "㟢",
            0xE08F to "㻚",
            0xE090 to "䂓",
            0xE094 to "䉤",
            0xE2F0 to "⁉",
            0xE2FF to "㉑",
            0xE380 to "㉒",
            0xE381 to "㉓",
            0xE382 to "㉔",
            0xE39D to "㉕",
            0xE39E to "㉖",
            0xE39F to "㉗",
            0xE3A0 to "㉘",
            0xE3A1 to "㉙",
            0xE3A2 to "㉚",
            0xE3A3 to "⓫",
            0xE3A4 to "⓬",
            0xE3A5 to "㉛"
    )

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
            sb.append(map.getOrDefault(it.toInt(), it.toString()))
        }
        return sb.toString()
    }
}
