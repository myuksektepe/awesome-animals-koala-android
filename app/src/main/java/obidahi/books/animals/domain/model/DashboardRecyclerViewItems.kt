package obidahi.books.animals.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Murat Yüksektepe on 2.09.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */
sealed class DashboardRecyclerViewItem {

    @Parcelize
    @Serializable
    class Book(
        @SerialName("name")
        val name: String,

        @SerialName("folder_name")
        val folderName: String,

        @SerialName("cover_image")
        val coverImage: String,

        @SerialName("package_size")
        val packageSize: String,

        @SerialName("package_item_count")
        val packageItemCount: Int,

        var isDownloaded: Boolean? = null,
        var isExtracted: Boolean? = null
    ) : DashboardRecyclerViewItem(), Parcelable

}