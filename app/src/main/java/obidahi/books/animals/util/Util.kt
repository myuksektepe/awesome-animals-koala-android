package obidahi.books.animals.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import obidahi.books.animals.domain.model.UnzipStatus
import java.io.*
import java.util.zip.ZipFile
import kotlin.math.roundToInt

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

/**
 * UnzipUtils class extracts files and sub-directories of a standard zip file to
 * a destination directory.
 *
 */
object UnzipUtils {
    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String): Flow<UnzipStatus> = channelFlow {
        Log.i(TAG, "Unzip ___ $destDirectory")
        try {
            File(destDirectory).run {
                if (!exists()) {
                    Log.i(TAG, "Unzip ___ Klasör bulunamadı.")
                    if (mkdirs()) {
                        Log.i(TAG, "Unzip ___ Klasör oluşturuldu.")
                    } else {
                        Log.i(TAG, "Unzip ___ Klasör oluşturulamadı!")
                    }
                } else {
                    Log.i(TAG, "Unzip ___ Klasör bulundu.")
                }
            }

            ZipFile(zipFilePath).use { zip ->
                val entires = zip.entries().toList()
                //Log.i(TAG, "Unzip ___ Entries Count: ${entires.count()}")
                entires.forEachIndexed { index, entry ->
                    zip.getInputStream(entry).use { input ->
                        val filePath = destDirectory + File.separator + entry.name
                        if (!entry.isDirectory) {
                            // if the entry is a file, extracts it
                            extractFile(input, filePath)
                        } else {
                            // if the entry is a directory, make the directory
                            val dir = File(filePath)
                            dir.mkdir()
                        }
                    }

                    val progress = (((index.toFloat() + 1) / entires.size.toFloat()) * 100f).roundToInt()
                    send(UnzipStatus.Progress(progress))
                    kotlinx.coroutines.delay(10)
                }
                Log.i(TAG, "Unzip ___ Tüm dosyalar eklendi")
                send(UnzipStatus.Success)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unzip ___ Mkdir: ${e.message}")
            send(UnzipStatus.Error(e.message!!))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        Log.i(TAG, "Unzip ___ Eklendi: $destFilePath")

        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096
}


fun Context.isBookDownloaded(packageFile: String): Boolean {
    val file = File("${getDir("packages", Context.MODE_PRIVATE)}/${packageFile}")
    return file.exists()
}

fun isBookExtracted(destinationFolder: String, packageItemCount: Int): Boolean {
    val destFolder = File(destinationFolder)
    if (destFolder.exists()) {
        val fileList = destFolder.list()
        if (fileList != null) {
            if (fileList.size == packageItemCount) {
                return true
            }
        }
    }
    return false
}