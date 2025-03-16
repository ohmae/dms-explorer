/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object PropertyParser {
    private val FORMAT_D = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    private val FORMAT_T = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN)
    private val FORMAT_Z = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.JAPAN)

    @Throws(ParseException::class)
    private fun parseD(
        value: String,
    ): Date? =
        synchronized(FORMAT_D) {
            FORMAT_D.parse(value)
        }

    @Throws(ParseException::class)
    private fun parseT(
        value: String,
    ): Date? =
        synchronized(FORMAT_T) {
            FORMAT_T.parse(value)
        }

    @Throws(ParseException::class)
    private fun parseZ(
        value: String,
    ): Date? =
        synchronized(FORMAT_Z) {
            FORMAT_Z.parse(value)
        }

    /**
     * 与えられた文字列をパースしてDateとして戻す。
     *
     * CDSで使用される日付フォーマットにはいくつかバリエーションがあるが、
     * 該当するフォーマットでパースを行う。
     *
     * @param value パースする文字列
     * @return パース結果、パースできない場合null
     */
    fun parseDate(
        value: String?,
    ): Date? {
        if (value.isNullOrEmpty()) {
            return null
        }
        return try {
            when {
                value.length <= 10 ->
                    parseD(value)

                value.length <= 19 ->
                    parseT(value)

                value.lastIndexOf(':') == 22 ->
                    parseZ(value.substring(0, 22) + value.substring(23))

                else ->
                    parseZ(value)
            }
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * protocolInfoの文字列からMimeTypeの文字列を抽出する。
     *
     * @param protocolInfo protocolInfo
     * @return MimeTypeの文字列。抽出に失敗した場合null
     */
    fun extractMimeTypeFromProtocolInfo(
        protocolInfo: String?,
    ): String? {
        if (protocolInfo.isNullOrEmpty()) {
            return null
        }
        val protocols = protocolInfo.split(';')
        if (protocols.isEmpty()) {
            return null
        }
        val sections = protocols[0].split(':')
        return if (sections.size < 3) null else sections[2]
    }

    /**
     * protocolInfoの文字列からProtocolの文字列を抽出する。
     *
     * @param protocolInfo protocolInfo
     * @return Protocolの文字列。抽出に失敗した場合null
     */
    fun extractProtocolFromProtocolInfo(
        protocolInfo: String?,
    ): String? {
        if (protocolInfo.isNullOrEmpty()) {
            return null
        }
        val protocols = protocolInfo.split(';')
        if (protocols.isEmpty()) {
            return null
        }
        val sections = protocols[0].split(':')
        return if (sections.size < 3) null else sections[0]
    }
}
