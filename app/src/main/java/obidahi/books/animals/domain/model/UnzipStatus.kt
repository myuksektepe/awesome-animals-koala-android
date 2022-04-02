package obidahi.books.animals.domain.model

sealed class UnzipStatus {
    object Success : UnzipStatus()
    data class Error(val message: String) : UnzipStatus()
    data class Progress(val progress: Int) : UnzipStatus()
}