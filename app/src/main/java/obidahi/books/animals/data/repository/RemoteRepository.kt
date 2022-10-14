package obidahi.books.animals.data.repository

import android.util.Log
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import obidahi.books.animals.data.network.KtorClient
import obidahi.books.animals.domain.model.BookDataModel
import obidahi.books.animals.domain.model.BooksModel
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem
import obidahi.books.animals.domain.model.DownloadStatus
import obidahi.books.animals.util.*
import java.io.File
import kotlin.math.roundToInt

object RemoteRepository {

    suspend fun getBooks() =
        channelFlow<ResultState<List<DashboardRecyclerViewItem.Book>>> {
            send(ResultState.LOADING())
            runCatching {
                try {
                    val httpResponse: BooksModel = KtorClient.httpClient.get("$API_URL/books.php?language=$LANGUAGE")
                    if (httpResponse.status) {
                        send(ResultState.SUCCESS(httpResponse.books!!))
                    } else {
                        send(ResultState.FAIL(httpResponse.message!!))
                    }
                } catch (e: Exception) {
                    send(ResultState.FAIL(e.message.toString()))
                }
            }
        }.flowOn(Dispatchers.IO)

    suspend fun getBookData(folderName: String) =
        channelFlow<ResultState<BookDataModel>> {
            send(ResultState.LOADING())
            runCatching {
                try {
                    val httpResponse: BookDataModel = KtorClient.httpClient.get("$booksUrl/$folderName/data.json")
                    send(ResultState.SUCCESS(httpResponse))
                } catch (e: Exception) {
                    send(ResultState.FAIL(e.message.toString()))
                }
            }
        }.flowOn(Dispatchers.IO)

    suspend fun downloadFile(folderName: String, file: File, fileName: String) =
        channelFlow {
            runCatching {
                try {
                    val httpResponse: HttpResponse = KtorClient.httpClient.get("$booksUrl/$folderName/$fileName") {
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