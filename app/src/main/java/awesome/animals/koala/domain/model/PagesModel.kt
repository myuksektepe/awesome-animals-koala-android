package awesome.animals.koala.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PagesModel(
    val pages: List<PageModel>,
) : Parcelable