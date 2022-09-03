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
import obidahi.books.animals.prensentation.adapter.BooksAdapter
import obidahi.books.animals.prensentation.base.BaseActivity
import obidahi.books.animals.prensentation.viewmodel.DashboardActivityViewModel
import obidahi.books.animals.util.ResultState
import obidahi.books.animals.util.TAG
import obidahi.books.animals.util.ViewExtensions.showCustomDialog
import obidahi.books.animals.util.isBookExtracted

@AndroidEntryPoint
class DashboardActivity : BaseActivity<DashboardActivityViewModel, ActivityDashboardBinding>() {
    override val layoutRes = R.layout.activity_dashboard
    override val viewModel: DashboardActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    lateinit var booksAdapter: BooksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booksAdapter = BooksAdapter()
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
                    showCustomDialog(
                        getString(R.string.error),
                        it.message,
                        getString(R.string.try_again), null, { getBooks() }, {})
                    Log.e(TAG, it.message)
                }
                is ResultState.SUCCESS -> {
                    it.data.forEach { book ->
                        book.isExtracted = isBookExtracted("${getDir("packages", Context.MODE_PRIVATE)}/${book.folderName}", book.packageItemCount)
                    }
                    booksAdapter.bookList = it.data
                    binding.rcyBooks.apply {
                        setHasFixedSize(true)
                        adapter = booksAdapter
                        layoutManager = LinearLayoutManager(this@DashboardActivity)
                        booksAdapter.onItemSelected = { position, book, clickedButton ->
                            val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                            intent.putExtra("FOLDER_NAME", book.folderName)
                            intent.putExtra("coverImage", book.coverImage)
                            startActivity(intent)
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