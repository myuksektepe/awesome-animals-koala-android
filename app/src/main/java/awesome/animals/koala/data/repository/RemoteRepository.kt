package awesome.animals.koala.data.repository

import awesome.animals.koala.data.network.KtorClient
import awesome.animals.koala.domain.model.DownloadStatus
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.math.roundToInt

object RemoteRepository {
    suspend fun getEmployees(): Response = KtorClient.httpClient.use {
        it.get("https://dummy.restapiexample.com/api/v1/employees")
    }

    suspend fun downloadFile(file: File, url: String): Flow<DownloadStatus> = flow {
        val response: HttpResponse = KtorClient.httpClient.use {
            it.get(url)
        }

        val byteArray = ByteArray(response.contentLength()!!.toInt())
        var offset = 0
        do {
            val currentRead = response.content.readAvailable(byteArray, offset, byteArray.size)
            offset += currentRead
            val progress = (offset * 100f / byteArray.size).roundToInt()
            emit(DownloadStatus.Progress(progress))
        } while (currentRead > 0)

        response.close()

        if (response.status.isSuccess()) {
            file.writeBytes(byteArray)
            emit(DownloadStatus.Success)
        } else {
            emit(DownloadStatus.Error("File not downloaded"))
        }
    }
}