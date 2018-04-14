/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import net.mm2d.log.Log;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class DebugSender implements Sender {
    DebugSender() {
    }

    @Override
    public void logEvent(
            @NonNull @Size(min = 1L, max = 40L) final String name,
            @Nullable final Bundle params) {
        Log.i("\nname: " + name + "\nparams: " + dumpBundle(params));
    }

    private String dumpBundle(@Nullable final Bundle params) {
        if (params == null) {
            return "null";
        }
        if (params.isEmpty()) {
            return "empty";
        }
        final StringBuilder sb = new StringBuilder();
        for (final String key : params.keySet()) {
            sb.append("\n\t");
            sb.append("key:");
            sb.append(key);
            sb.append(" value:");
            sb.append(params.get(key));
        }
        return sb.toString();
    }
}
