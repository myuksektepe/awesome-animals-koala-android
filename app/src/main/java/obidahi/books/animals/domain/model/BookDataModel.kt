package obidahi.books.animals.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BookDataModel(
    val packageFile: String,
    val packageSize: String,
    val packageItemsCount: Int,
    val backgroundSong: String?,
    val backgroundImage: String?,
    val pages: List<BookPageModel>,
) : Parcelable {
    override fun toString(): String = "$packageFile - $packageSize - $packageItemsCount"
}