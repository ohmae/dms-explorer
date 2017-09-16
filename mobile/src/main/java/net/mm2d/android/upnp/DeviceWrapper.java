/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.upnp.Device;
import net.mm2d.upnp.Icon;

import java.util.List;

/**
 * 特定のDeviceTypeへのインターフェースを備えるDeviceWrapperの共通の親。
 *
 * <p>Deviceはhas-a関係で保持するが、Wrapperと1対1である保証はなく、
 * 複数のWrapperから一つのDeviceが参照される可能性もある。
 * WrapperとしてDeviceへアクセスするためのアクセッサをここで定義し、
 * 継承したクラスではその他の機能を実装する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class DeviceWrapper {
    @NonNull
    private final Device mDevice;
    @NonNull
    private final Bundle mArguments = new Bundle();

    private boolean mIconSearched;
    @Nullable
    private Icon mIcon;

    public DeviceWrapper(@NonNull final Device device) {
        mDevice = device;
    }

    /**
     * このクラスがwrapしているDeviceのインスタンスを返す。
     *
     * <p><b>取扱い注意！</b>
     * <p>protectedであるが、内部実装を理解したサブクラスから参照される想定であり、
     * 直接Deviceをサブクラス以外から参照しないこと。
     * Deviceの各プロパティへは用意されたアクセッサを利用する。
     *
     * @return Device
     */
    @NonNull
    protected Device getDevice() {
        return mDevice;
    }

    /**
     * Deviceの有効なIconを返す。
     *
     * <p>MsControlPointにてダウンロードするIconを一つのみに限定しているため、
     * Binaryデータがダウンロード済みのものがあればそれを返す。
     *
     * @return Iconインスタンス、ない場合はnullが返る。
     */
    @Nullable
    public Icon getIcon() {
        if (mIconSearched) {
            return mIcon;
        }
        mIconSearched = true;
        mIcon = searchIcon();
        return mIcon;
    }

    @Nullable
    private Icon searchIcon() {
        final List<Icon> iconList = mDevice.getIconList();
        for (final Icon icon : iconList) {
            if (icon.getBinary() != null) {
                return icon;
            }
        }
        return null;
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    public void putBooleanTag(
            @NonNull final String name,
            final boolean value) {
        mArguments.putBoolean(name, value);
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    public boolean getBooleanTag(
            @NonNull final String name,
            final boolean defaultValue) {
        return mArguments.getBoolean(name, defaultValue);
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    public void putIntTag(
            @NonNull final String name,
            final int value) {
        mArguments.putInt(name, value);
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    public int getIntTag(
            @NonNull final String name,
            final int defaultValue) {
        return mArguments.getInt(name, defaultValue);
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    public void putLongTag(
            @NonNull final String name,
            final long value) {
        mArguments.putLong(name, value);
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name         データの名前
     * @param defaultValue 格納されていなかった場合のデフォルト値
     * @return データの値
     */
    public long getLongTag(
            @NonNull final String name,
            final long defaultValue) {
        return mArguments.getLong(name, defaultValue);
    }

    /**
     * 任意の値を登録する。
     *
     * @param name  データの名前
     * @param value 格納する値
     */
    public void putStringTag(
            @NonNull final String name,
            @Nullable final String value) {
        mArguments.putString(name, value);
    }

    /**
     * 格納された任意の値を取り出す。
     *
     * @param name データの名前
     * @return データの値
     */
    @Nullable
    public String getStringTag(@NonNull final String name) {
        return mArguments.getString(name);
    }

    /**
     * Locationに記述のIPアドレスを返す。
     *
     * 記述に異常がある場合は空文字が返る。
     *
     * @return IPアドレス
     */
    @NonNull
    public String getIpAddress() {
        return mDevice.getIpAddress();
    }

    /**
     * UDNタグの値を返す。
     *
     * @return UDNタグの値
     */
    @NonNull
    public String getUdn() {
        return mDevice.getUdn();
    }

    /**
     * friendlyNameタグの値を返す。
     *
     * @return friendlyNameタグの値
     */
    @NonNull
    public String getFriendlyName() {
        return mDevice.getFriendlyName();
    }

    /**
     * manufacturerタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return manufacturerタグの値
     */
    @Nullable
    public String getManufacture() {
        return mDevice.getManufacture();
    }

    /**
     * manufacturerURLタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return manufacturerURLタグの値
     */
    @Nullable
    public String getManufactureUrl() {
        return mDevice.getManufactureUrl();
    }

    /**
     * modelNameタグの値を返す。
     *
     * @return modelNameタグの値
     */
    @NonNull
    public String getModelName() {
        return mDevice.getModelName();
    }

    /**
     * modelURLタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return modelURLタグの値
     */
    @Nullable
    public String getModelUrl() {
        return mDevice.getModelUrl();
    }

    /**
     * modelDescriptionタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return modelDescriptionタグの値
     */
    @Nullable
    public String getModelDescription() {
        return mDevice.getModelDescription();
    }

    /**
     * modelNumberタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return modelNumberタグの値
     */
    @Nullable
    public String getModelNumber() {
        return mDevice.getModelNumber();
    }

    /**
     * serialNumberタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return serialNumberタグの値
     */
    @Nullable
    public String getSerialNumber() {
        return mDevice.getSerialNumber();
    }

    /**
     * presentationURLタグの値を返す。
     *
     * <p>値が存在しない場合nullが返る。
     *
     * @return presentationURLタグの値
     */
    @Nullable
    public String getPresentationUrl() {
        return mDevice.getPresentationUrl();
    }

    /**
     * SSDPパケットに記述されているLocationヘッダの値を返す。
     *
     * @return Locationヘッダの値
     */
    @Nullable
    public String getLocation() {
        return mDevice.getLocation();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DeviceWrapper)) {
            return false;
        }
        final DeviceWrapper m = (DeviceWrapper) o;
        return mDevice.equals(m.mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    @Override
    public String toString() {
        return getFriendlyName();
    }
}
