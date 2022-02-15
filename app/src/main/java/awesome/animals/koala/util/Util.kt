package awesome.animals.koala.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.IOException


fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        Log.e(TAG, "getJsonDataFromAsset: ${ioException.message}")
        return null
    }
    return jsonString
}

fun Activity.openWifiSettings() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings")
    startActivity(intent)
}