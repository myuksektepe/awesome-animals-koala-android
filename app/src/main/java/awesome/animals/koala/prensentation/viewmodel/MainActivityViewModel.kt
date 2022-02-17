package awesome.animals.koala.prensentation.viewmodel

import androidx.lifecycle.MutableLiveData
import awesome.animals.koala.data.repository.RemoteRepository
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.prensentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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

    private val _countdown: MutableLiveData<DownloadStatus> = MutableLiveData()
    val countdown get() = _countdown


    /*
    fun downloadFile(file: File, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepository.downloadFile(file, url).collect {
                _downloadState.postValue(it)
            }
        }
    }
     */

    suspend fun downloadFile(file: File, url: String){
        remoteRepository.downloadFile(file, url).collect {
            _downloadState.postValue(it)
        }
    }

    fun countdown(): Flow<DownloadStatus> = flow {
        for (i in 0..100) {
            delay(500)
            if (i == 100) {
                emit(DownloadStatus.Success)
            }
            emit(DownloadStatus.Progress(i))
        }
    }
}