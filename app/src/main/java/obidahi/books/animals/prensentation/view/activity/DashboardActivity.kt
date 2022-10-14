package obidahi.books.animals.prensentation.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import obidahi.books.animals.R
import obidahi.books.animals.databinding.ActivityDashboardBinding
import obidahi.books.animals.domain.model.BookItemButtonType
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem
import obidahi.books.animals.prensentation.adapter.BooksAdapter
import obidahi.books.animals.prensentation.base.BaseActivity
import obidahi.books.animals.prensentation.viewmodel.DashboardActivityViewModel
import obidahi.books.animals.util.CustomDialog
import obidahi.books.animals.util.ResultState
import obidahi.books.animals.util.TAG
import obidahi.books.animals.util.isBookExtracted

@AndroidEntryPoint
class DashboardActivity : BaseActivity<DashboardActivityViewModel, ActivityDashboardBinding>() {
    override val layoutRes = R.layout.activity_dashboard
    override val viewModel: DashboardActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    lateinit var booksAdapter: BooksAdapter
    private var booksList: MutableList<DashboardRecyclerViewItem> = mutableListOf()
    lateinit var customDialog: CustomDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booksAdapter = BooksAdapter()
        customDialog = CustomDialog.getInstance(this)
        binding.swpLayout.setOnRefreshListener {
            getBooks()
        }
    }

    override fun onResume() {
        super.onResume()
        getBooks()
    }

    private fun getBooks() {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.fetchBooks()
        }
    }

    override fun obverseViewModel() {
        viewModel.books.observe(this) {
            when (it) {
                is ResultState.LOADING -> {
                    showLoading()
                }
                is ResultState.FAIL -> {
                    hideLoading()
                    customDialog.show(
                        getString(R.string.error),
                        it.message,
                        getString(R.string.try_again), null, { getBooks() }, {})
                    Log.e(TAG, it.message)
                }
                is ResultState.SUCCESS -> {
                    booksList.clear()

                    // Title
                    booksList.add(0, DashboardRecyclerViewItem.Title(getString(R.string.books)))

                    // Books
                    it.data.forEach { book ->
                        book.isExtracted = isBookExtracted("${getDir("packages", Context.MODE_PRIVATE)}/${book.folderName}", book.packageItemCount)
                    }
                    booksList.addAll(it.data)

                    booksAdapter.items = booksList
                    binding.rcyBooks.apply {
                        setHasFixedSize(true)
                        adapter = booksAdapter
                        layoutManager = LinearLayoutManager(this@DashboardActivity)
                        booksAdapter.onItemSelected = { _, book, clickedButton ->
                            when (clickedButton) {
                                BookItemButtonType.READ -> {
                                    val intent = Intent(this@DashboardActivity, BookActivity::class.java)
                                    intent.putExtra("folderName", book.folderName)
                                    startActivity(intent)
                                }
                                BookItemButtonType.DOWNLOAD -> {
                                    val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                                    intent.putExtra("folderName", book.folderName)
                                    intent.putExtra("coverImage", book.coverImage)
                                    startActivity(intent)
                                }
                                else -> {}
                            }
                        }
                    }
                    binding.swpLayout.isRefreshing = false
                    hideLoading()
                }
                else -> {}
            }
        }
    }
}