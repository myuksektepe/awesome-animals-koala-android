package obidahi.books.animals.prensentation.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import obidahi.books.animals.R
import obidahi.books.animals.databinding.BooksItemBinding
import obidahi.books.animals.databinding.TitleItemBinding
import obidahi.books.animals.domain.model.BookItemButtonType
import obidahi.books.animals.domain.model.DashboardRecyclerViewItem


/**
 * Created by Murat Yüksektepe on 2.09.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */
sealed class DashboardRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class TitleViewHolder(private val binding: TitleItemBinding) : DashboardRecyclerViewHolder(binding) {
        fun bind(title: String) {
            binding.txtTitle.text = title
        }
    }

    class BookViewHolder(private val binding: BooksItemBinding) : DashboardRecyclerViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bind(
            book: DashboardRecyclerViewItem.Book,
            position: Int,
            onItemSelected: (Int, DashboardRecyclerViewItem.Book, BookItemButtonType) -> Unit
        ) {
            // Book Name
            binding.txtBookName.text = book.name

            // Page Count
            binding.txtPageCount.text = "${book.pageCount} ${binding.root.context.getString(R.string.pages)}"

            // Package Size
            binding.txtBookPackageSize.text = "(${book.packageSize})"

            // Book Image
            Glide
                .with(binding.root.context)
                .load(book.coverImage)
                .centerCrop()
                .into(binding.imgBooksItem)

            // On Item Selected
            binding.btnBookDownload.setOnClickListener {
                onItemSelected.invoke(position, book, BookItemButtonType.DOWNLOAD)
            }
            binding.btnBookRead.setOnClickListener {
                onItemSelected.invoke(position, book, BookItemButtonType.READ)
            }

            // If Book Is Downloaded & Extracted
            if (book.isExtracted == true) {
                binding.btnBookDownload.visibility = View.GONE
                binding.txtBookPackageSize.visibility = View.GONE
            } else {
                binding.btnBookRead.visibility = View.GONE
            }
        }
    }

}