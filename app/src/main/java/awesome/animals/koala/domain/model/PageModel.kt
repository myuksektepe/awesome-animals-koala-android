package awesome.animals.koala.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PageModel(
    val title: String?,
    val message: String,
    val video: String?,
    val video_cover: String?
) : Parcelable