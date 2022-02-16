package awesome.animals.koala.prensentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import awesome.animals.koala.data.repository.RemoteRepository
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.prensentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.cio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
/*
class MainActivityViewModel @Inject constructor() : BaseViewModel(){
    val murat = "dsfdsf"
}
 */

class MainActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    private val _downloadState: MutableLiveData<DownloadStatus> = MutableLiveData()
    val downloadState get() = _downloadState

    fun downloadFile(file: File, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepository.downloadFile(file, url).collect {
                _downloadState.postValue(it)
            }
        }
    }
}