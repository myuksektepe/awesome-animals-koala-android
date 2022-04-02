package obidahi.books.animals.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BookPageModel(
    val title: String?,
    val message: String,
    val video: String?,
    val videoOwner: String?,
    val image: String?,
    val imageOwner: String?,
    val voice: String?,
    val time: Int,
    val isActive: Boolean,
) : Parcelable