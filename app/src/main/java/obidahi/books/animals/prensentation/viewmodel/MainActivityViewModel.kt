package obidahi.books.animals.prensentation.viewmodel

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import obidahi.books.animals.data.repository.RemoteRepository
import obidahi.books.animals.domain.model.BookDataModel
import obidahi.books.animals.domain.model.DownloadStatus
import obidahi.books.animals.domain.model.ResultState
import obidahi.books.animals.domain.model.UnzipStatus
import obidahi.books.animals.prensentation.base.BaseViewModel
import obidahi.books.animals.util.UnzipUtils.unzip
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    private val _getBookDataState: MutableLiveData<ResultState<BookDataModel>> = MutableLiveData()
    val getBookDataState get() = _getBookDataState

    private val _downloadState: MutableLiveData<DownloadStatus> = MutableLiveData()
    val downloadState get() = _downloadState

    private val _unzipState: MutableLiveData<UnzipStatus> = MutableLiveData()
    val unzipState get() = _unzipState

    suspend fun getBookData() {
        remoteRepository.getBookData().collect {
            _getBookDataState.postValue(it)
        }
    }

    suspend fun downloadFile(file: File, fileName: String) {
        remoteRepository.downloadFile(file, fileName).collect {
            _downloadState.postValue(it)
        }
    }

    suspend fun unzipFile(file: File, destination: String) {
        unzip(file, destination).collect {
            _unzipState.postValue(it)
        }
    }
}