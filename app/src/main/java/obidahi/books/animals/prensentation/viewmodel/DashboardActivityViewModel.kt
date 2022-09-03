package obidahi.books.animals.prensentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import obidahi.books.animals.data.repository.RemoteRepository
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem
import obidahi.books.animals.prensentation.base.BaseViewModel
import obidahi.books.animals.util.ResultState
import javax.inject.Inject

@HiltViewModel
class DashboardActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    private val _books: MutableLiveData<ResultState<List<DashboardRecyclerViewItem.Book>>> = MutableLiveData()
    val books: LiveData<ResultState<List<DashboardRecyclerViewItem.Book>>> get() = _books

    suspend fun fetchBooks() {
        remoteRepository.getBooks().collectLatest {
            _books.postValue(it)
        }
    }
}