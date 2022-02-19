package awesome.animals.koala.domain.model

sealed class UnzipStatus {
    object Started : UnzipStatus()
    object Success : UnzipStatus()
    data class Error(val message: String) : UnzipStatus()
    data class Progress(val progress: Int) : UnzipStatus()
}