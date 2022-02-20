package awesome.animals.koala.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BookDataModel(
    val packageFile: String,
    val packageSize: String,
    val packageItemsCount: Int,
    val song: String?,
    val pages: List<BookPageModel>,
) : Parcelable {
    override fun toString(): String = "$packageFile - $packageSize - $packageItemsCount"
}