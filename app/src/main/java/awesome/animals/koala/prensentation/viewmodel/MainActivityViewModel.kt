package awesome.animals.koala.prensentation.viewmodel

import androidx.lifecycle.MutableLiveData
import awesome.animals.koala.data.repository.RemoteRepository
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.domain.model.UnzipStatus
import awesome.animals.koala.prensentation.base.BaseViewModel
import awesome.animals.koala.util.UnzipUtils.unzip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    private val _downloadState: MutableLiveData<DownloadStatus> = MutableLiveData()
    val downloadState get() = _downloadState

    private val _unzipState: MutableLiveData<UnzipStatus> = MutableLiveData()
    val unzipState get() = _unzipState

    suspend fun downloadFile(file: File, url: String) {
        remoteRepository.downloadFile(file, url).collect {
            _downloadState.postValue(it)
        }
    }

    suspend fun unzipFile(file: File, destination: String) {
        unzip(file, destination).collect {
            _unzipState.postValue(it)
        }
    }
}