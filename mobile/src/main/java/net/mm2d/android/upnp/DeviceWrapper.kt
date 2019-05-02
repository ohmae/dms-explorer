/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp

import android.os.Bundle

import net.mm2d.upnp.Device
import net.mm2d.upnp.Icon

/**
 * 特定のDeviceTypeへのインターフェースを備えるDeviceWrapperの共通の親。
 *
 * Deviceはhas-a関係で保持するが、Wrapperと1対1である保証はなく、
 * 複数のWrapperから一つのDeviceが参照される可能性もある。
 * WrapperとしてDeviceへアクセスするためのアクセッサをここで定義し、
 * 継承したクラスではその他の機能を実装する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class DeviceWrapper(
    /**
     * このクラスがwrapしているDeviceのインスタンスを返す。
     *
     * protectedであるが、内部実装を理解したサブクラスから参照される想定であり、
     * 直接Deviceをサブクラス以外から参照しないこと。
     * Deviceの各プロパティへは用意されたアクセッサを利用する。
     *
     * @return Device
     */
    private val device: Device
) {
    private val arguments = Bundle()
    private var iconSearched: Boolean = false
    private var icon: Icon? = null

    /**
     * Deviceの有効なIconを返す。
     *
     * MsControlPointにてダウンロードするIconを一つのみに限定しているため、
     * Binaryデータがダウンロード済みのものがあればそれを返す。
     *
     * @return Iconインスタンス、ない場合はnullが返る。
     */
    fun getIcon(): Icon? {
        if (iconSearched) {
            return icon
        }
        iconSearched = true
        icon = searchIcon()
        return icon
    }

    /**
     * Locationに記述のIPアドレスを返す。
     *
     * 記述に異常がある場合は空文字が返る。
     *
     * @return IPアドレス
     */
    val ipAddress: String
        get() = device.ipAddress

    /**
     * UDNタグの値を返す。
     *
     * @return UDNタグの値
     */
    val udn: String
        get() = device.udn

    /**
     * friendlyNameタグの値を返す。
     *
     * @return friendlyNameタグの値
     */
    val friendlyName: String
        get() = device.friendlyName

    /**
     * manufacturerタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return manufacturerタグの値
     */
    val manufacture: String?
        get() = device.manufacture

    /**
     * manufacturerURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return manufacturerURLタグの値
     */
    val manufactureUrl: String?
        get() = device.manufactureUrl

    /**
     * modelNameタグの値を返す。
     *
     * @return modelNameタグの値
     */
    val modelName: String
        get() = device.modelName

    /**
     * modelURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelURLタグの値
     */
    val modelUrl: String?
        get() = device.modelUrl

    /**
     * modelDescriptionタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelDescriptionタグの値
     */
    val modelDescription: String?
        get() = device.modelDescription

    /**
     * modelNumberタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return modelNumberタグの値
     */
    val modelNumber: String?
        get() = device.modelNumber

    /**
     * serialNumberタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return serialNumberタグの値
     */
    val serialNumber: String?
        get() = device.serialNumber

    /**
     * presentationURLタグの値を返す。
     *
     * 値が存在しない場合nullが返る。
     *
     * @return presentationURLタグの値
     */
    val presentationUrl: String?
        get() = device.presentationUrl

    /**
     * SSDPパケットに記述されているLocationヘッダの値を返す。
     *
     * @return Locationヘッダの値
     */
    val location: String?
        get() = device.location

    private fun searchIcon(): Icon? {
        val iconList = device.iconList
        for (icon in iconList) {
            if (icon.binary != null) {
                return icon
            }
        }
        return null
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    fun putBooleanTag(
        name: String,
        value: Boolean
    ) {
        arguments.putBoolean(name, value)
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    fun getBooleanTag(
        name: String,
        defaultValue: Boolean
    ): Boolean {
        return arguments.getBoolean(name, defaultValue)
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    fun putIntTag(
        name: String,
        value: Int
    ) {
        arguments.putInt(name, value)
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    fun getIntTag(
        name: String,
        defaultValue: Int
    ): Int {
        return arguments.getInt(name, defaultValue)
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    fun putLongTag(
        name: String,
        value: Long
    ) {
        arguments.putLong(name, value)
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    fun getLongTag(
        name: String,
        defaultValue: Long
    ): Long {
        return arguments.getLong(name, defaultValue)
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    fun putStringTag(
        name: String,
        value: String?
    ) {
        arguments.putString(name, value)
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name データの名前
     * @return データの値
     */
    fun getStringTag(name: String): String? {
        return arguments.getString(name)
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) {
            return true
        }
        if (o !is DeviceWrapper) {
            return false
        }
        val m = o as DeviceWrapper?
        return device == m!!.device
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }

    override fun toString(): String {
        return friendlyName
    }
}
