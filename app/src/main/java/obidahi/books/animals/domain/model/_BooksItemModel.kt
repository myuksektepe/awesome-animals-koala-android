package obidahi.books.animals.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Murat Yüksektepe on 30.08.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */

@Parcelize
@Serializable
data class _BooksItemModel(
    @SerialName("name")
    val name: String,

    @SerialName("coverImage")
    val coverImage: String,

    @SerialName("packageSize")
    val packageSize: String,
) : Parcelable {
    override fun toString(): String = "$name - $coverImage - $packageSize"
}