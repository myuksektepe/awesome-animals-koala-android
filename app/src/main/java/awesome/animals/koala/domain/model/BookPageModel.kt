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
    val video_cover: String?,
    val voice: String?,
    val timeSeconds: Int,
    val is_active: Boolean,
) : Parcelable