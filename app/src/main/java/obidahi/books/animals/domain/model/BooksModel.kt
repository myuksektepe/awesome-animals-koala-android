package obidahi.books.animals.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.annotation.Nullable

/**
 * Created by Murat Yüksektepe on 30.08.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */

@Parcelize
@Serializable
data class BooksModel(
    @SerialName("status")
    val status: Boolean,

    @SerialName("books")
    val books: @RawValue MutableList<DashboardRecyclerViewItem.Book>? = null,

    @Nullable
    @SerialName("message")
    val message: String? = ""
) : Parcelable {
}