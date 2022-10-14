package obidahi.books.animals.prensentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import obidahi.books.animals.R
import obidahi.books.animals.databinding.BooksItemBinding
import obidahi.books.animals.databinding.TitleItemBinding
import obidahi.books.animals.domain.model.BookItemButtonType
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem


/**
 * Created by Murat Yüksektepe on 30.08.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */
class BooksAdapter() : RecyclerView.Adapter<DashboardRecyclerViewHolder>() {

    internal var onItemSelected: (position: Int, item: DashboardRecyclerViewItem.Book, clickedButton: BookItemButtonType) -> Unit = { _, _, _ -> }

    var items = listOf<DashboardRecyclerViewItem>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DashboardRecyclerViewItem.Title -> R.layout.title_item
            is DashboardRecyclerViewItem.Book -> R.layout.books_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardRecyclerViewHolder {
        return when (viewType) {
            R.layout.title_item -> DashboardRecyclerViewHolder.TitleViewHolder(
                TitleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            R.layout.books_item -> DashboardRecyclerViewHolder.BookViewHolder(
                BooksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> throw  IllegalArgumentException("Invalid ViewType!!")
        }
    }

    override fun onBindViewHolder(holder: DashboardRecyclerViewHolder, position: Int) {
        when (holder) {
            is DashboardRecyclerViewHolder.TitleViewHolder -> holder.bind(
                (items[position] as DashboardRecyclerViewItem.Title).title,
            )
            is DashboardRecyclerViewHolder.BookViewHolder -> holder.bind(
                items[position] as DashboardRecyclerViewItem.Book,
                position,
                onItemSelected
            )
        }
    }

}