package awesome.animals.koala.util

import android.content.Context
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