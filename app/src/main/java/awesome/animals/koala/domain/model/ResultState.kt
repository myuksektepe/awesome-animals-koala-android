package awesome.animals.koala.domain.model

sealed class ResultState<out R> {
    class LOADING<T> : ResultState<T>()
    data class FAIL(val message: String) : ResultState<Nothing>()
    data class SUCCESS<out T>(val data: T) : ResultState<T>()
}