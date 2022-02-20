package awesome.animals.koala.data.repository

import android.util.Log
import awesome.animals.koala.data.network.KtorClient
import awesome.animals.koala.domain.model.BookDataModel
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.domain.model.ResultState
import awesome.animals.koala.util.API_URL
import awesome.animals.koala.util.BOOK_NAME
import awesome.animals.koala.util.TAG
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.math.roundToInt

object RemoteRepository {
    suspend fun getBookData(): Flow<ResultState<BookDataModel>> = channelFlow<ResultState<BookDataModel>> {
        send(ResultState.LOADING())
        runCatching {
            try {
                val httpResponse: BookDataModel = KtorClient.httpClient.get("$API_URL/$BOOK_NAME/data.json")
                send(ResultState.SUCCESS(httpResponse))
            } catch (e: Exception) {
                send(ResultState.FAIL(e.message.toString()))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun downloadFile(file: File, fileName: String): Flow<DownloadStatus> = channelFlow {
        runCatching {
            try {
                val httpResponse: HttpResponse = KtorClient.httpClient.get("$API_URL/$BOOK_NAME/$fileName") {
                    onDownload { bytesSentTotal, contentLength ->
                        //Log.i(TAG, "Received $bytesSentTotal bytes from $contentLength")
                        val progress = ((bytesSentTotal.toFloat() / contentLength.toFloat()) * 100).roundToInt()
                        send(DownloadStatus.Progress(progress))
                    }
                }

                val responseBody: ByteArray = httpResponse.receive()
                file.writeBytes(responseBody)
                Log.i(TAG, "Download ___ Dosya şuraya kaydedildi: ${file.path}")
                send(DownloadStatus.Success)

            } catch (e: Exception) {
                send(DownloadStatus.Error(e.message.toString()))
            }
        }
    }.flowOn(Dispatchers.IO)
}