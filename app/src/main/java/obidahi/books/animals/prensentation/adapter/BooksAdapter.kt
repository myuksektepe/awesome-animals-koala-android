package obidahi.books.animals.prensentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import obidahi.books.animals.R
import obidahi.books.animals.databinding.BooksItemBinding
import obidahi.books.animals.domain.model.BookItemButtonType
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem


/**
 * Created by Murat Yüksektepe on 30.08.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */
class BooksAdapter() : RecyclerView.Adapter<DashboardRecyclerViewHolder>() {

    internal var onItemSelected: (position: Int, item: DashboardRecyclerViewItem.Book, clickedButton: BookItemButtonType) -> Unit = { _, _, _ -> }

    var bookList = listOf<DashboardRecyclerViewItem>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = bookList.size

    override fun getItemViewType(position: Int): Int {
        return when (bookList[position]) {
            is DashboardRecyclerViewItem.Book -> R.layout.books_item
            //is DashboardRecyclerViewItem.Book -> R.layout.books_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardRecyclerViewHolder {
        return when (viewType) {
            R.layout.books_item -> DashboardRecyclerViewHolder.BookViewHolder(
                BooksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            /*
             R.layout.books_item -> DashboardRecyclerViewHolder.BookViewHolder(
                BooksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
             */

            else -> throw  IllegalArgumentException("Invalid ViewType!!")
        }
    }

    override fun onBindViewHolder(holder: DashboardRecyclerViewHolder, position: Int) {
        when (holder) {
            is DashboardRecyclerViewHolder.BookViewHolder -> holder.bind(
                bookList[position] as DashboardRecyclerViewItem.Book,
                position,
                onItemSelected
            )
            /*            
            is DashboardRecyclerViewHolder.BookViewHolder -> holder.bind(
                bookList[position] as DashboardRecyclerViewItem.Book,
                position,
                onItemSelected
            )
             */
        }
    }

}