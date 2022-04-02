package obidahi.books.animals.domain.model

sealed class DownloadStatus {
    object Started : DownloadStatus()
    object Success : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
    data class Progress(val progress: Int) : DownloadStatus()
}