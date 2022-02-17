package awesome.animals.koala.data.repository

import android.util.Log
import awesome.animals.koala.data.network.KtorClient
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.util.TAG
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

object RemoteRepository {
    suspend fun getEmployees(): Response = KtorClient.httpClient.use {
        it.get("https://dummy.restapiexample.com/api/v1/employees")
    }

    fun downloadFile(file: File, url: String): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Error("dşkfjsdlkjf"))

        //GlobalScope.launch(Dispatchers.IO){
        try {
            val response: HttpResponse = KtorClient.httpClient.use {
                it.get(url)
            }
            var offset = 0
            val byteBufferSize = 1024 * 100
            val channel = response.receive<ByteReadChannel>()
            val contentLen = response.contentLength()?.toInt() ?: 0
            val data = ByteArray(contentLen)

            do {
                val currentRead = channel.readAvailable(data, offset, byteBufferSize)
                Log.i(TAG, "currentRead: $currentRead")

                val progress = if (contentLen == 0) 0 else (offset / contentLen.toDouble()) * 100
                emit(DownloadStatus.Progress(progress as Int))
                Log.i(TAG, "progress: $progress")
                offset += currentRead
            } while (currentRead > 0)

            /*
                    //val data = ByteArray(response.contentLength()!!.toInt())
                    val data = response.readBytes()
                    var offset = 0
                    do {
                        val currentRead = response.content.readAvailable(data, offset, data.size)
                        offset += currentRead
                        val progress = (offset * 100f / data.size).roundToInt()
                        emit(DownloadStatus.Progress(progress))
                        Log.i(TAG, "progress: $progress")
                    } while (currentRead > 0)

                    response.close()
             */

            if (response.status.isSuccess()) {
                file.writeBytes(data)
                emit(DownloadStatus.Success)
            } else {
                emit(DownloadStatus.Error("File not downloaded"))
            }
        } catch (e: Exception) {
            emit(DownloadStatus.Error(e.toString()))
        }

        //}
    }
}