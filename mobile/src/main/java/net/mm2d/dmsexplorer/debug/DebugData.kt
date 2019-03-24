package net.mm2d.dmsexplorer.debug

import android.content.Context
import net.mm2d.android.util.RuntimeEnvironment
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.log.Logger
import okio.Okio
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object DebugData {
    private var pinnedDeviceLocationList = emptyList<String>()

    fun initialize(context: Context) {
        if (!BuildConfig.DEBUG || !RuntimeEnvironment.isEmulator) {
            return
        }
        try {
            val inputStream = context.assets.open("locations.json")
            val json = Okio.buffer(Okio.source(inputStream)).readUtf8()
            val jsonObject = JSONObject(json)
            val list = ArrayList<String>()
            val i = jsonObject.keys()
            while (i.hasNext()) {
                list.add(jsonObject.getString(i.next()))
            }
            Logger.e { list.toString() }
            pinnedDeviceLocationList = list
        } catch (ignored: IOException) {
        } catch (ignored: JSONException) {
        }
    }

    @JvmStatic
    fun getPinnedDeviceLocationList(): List<String> {
        return pinnedDeviceLocationList
    }
}
