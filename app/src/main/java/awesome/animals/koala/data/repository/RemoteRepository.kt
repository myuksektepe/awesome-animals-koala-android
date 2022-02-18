package awesome.animals.koala.data.repository

import android.util.Log
import awesome.animals.koala.data.network.KtorClient
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.util.TAG
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.cio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.math.roundToInt

object RemoteRepository {
    suspend fun getEmployees(): Response = KtorClient.httpClient.use {
        it.get("https://dummy.restapiexample.com/api/v1/employees")
    }

    suspend fun downloadFile(file: File, url: String): Flow<DownloadStatus> = channelFlow {
        runCatching {
            try {

                val httpResponse: HttpResponse = KtorClient.httpClient.get(url) {
                    onDownload { bytesSentTotal, contentLength ->
                        //Log.i(TAG, "Received $bytesSentTotal bytes from $contentLength")
                        val progress = ((bytesSentTotal.toFloat() / contentLength.toFloat()) * 100).roundToInt()
                        send(DownloadStatus.Progress(progress))
                    }
                }

                val responseBody: ByteArray = httpResponse.receive()
                file.writeBytes(responseBody)
                Log.i(TAG, "A file saved to ${file.path}")
                send(DownloadStatus.Success)

            } catch (e: Exception) {
                send(DownloadStatus.Error(e.message.toString()))
            }
        }
    }.flowOn(Dispatchers.IO)

}