package awesome.animals.koala.prensentation.viewmodel

import androidx.lifecycle.viewModelScope
import awesome.animals.koala.data.repository.RemoteRepository
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.prensentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.cio.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : BaseViewModel(){
    val murat = "dsfdsf"
}
/*
class MainActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    val responseFlow = MutableStateFlow<Response?>(null)

    fun getEmployees() {
        viewModelScope.launch {
            kotlin.runCatching {
                remoteRepository.getEmployees()
            }.onSuccess {
                responseFlow.value = it
            }.onFailure {
                responseFlow.value = null
            }
        }
    }

    suspend fun downloadFile(file: File, url: String) = remoteRepository.downloadFile(file, url)
}
 */