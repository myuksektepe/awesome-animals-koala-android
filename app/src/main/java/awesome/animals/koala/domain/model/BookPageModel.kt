package awesome.animals.koala.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BookPageModel(
    val title: String?,
    val message: String,
    val video: String?,
    val videoCover: String?,
    val voice: String?,
    val timeSeconds: Int,
    val isActive: Boolean,
) : Parcelable