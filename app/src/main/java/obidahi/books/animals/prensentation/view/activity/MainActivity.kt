package obidahi.books.animals.prensentation.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import obidahi.books.animals.R
import obidahi.books.animals.databinding.ActivityMainBinding
import obidahi.books.animals.domain.model.BookDataModel
import obidahi.books.animals.domain.model.DownloadStatus
import obidahi.books.animals.domain.model.UnzipStatus
import obidahi.books.animals.prensentation.base.BaseActivity
import obidahi.books.animals.prensentation.viewmodel.MainActivityViewModel
import obidahi.books.animals.util.CustomDialog
import obidahi.books.animals.util.ResultState
import obidahi.books.animals.util.TAG
import obidahi.books.animals.util.ViewExtensions.animBounce
import obidahi.books.animals.util.ViewExtensions.animFadeIn
import obidahi.books.animals.util.ViewExtensions.animFadeOut
import obidahi.books.animals.util.openWifiSettings
import java.io.File


@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    private val context: Context = this@MainActivity
    private var runJob: Job? = null
    private var downloadJob: Job? = null
    private var downloadState: DownloadStatus? = null
    private var unzipState: UnzipStatus? = null
    private var tempUnit: () -> Unit? = { null }

    private var bookData: BookDataModel? = null
    private var filePackageFile: File? = null
    private lateinit var destinationFolder: String

    private var downloadPermissionGranted = false
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var folderName: String
    private lateinit var coverImage: String

    private lateinit var customDialog: CustomDialog

    override fun obverseViewModel() {
        // Network Status
        networkConnection.observe(viewLifeCycleOwner) {
            Log.i(TAG, "Network ___ Connection: $it")
            when (it) {
                false -> {
                    if (downloadState == DownloadStatus.Started) {
                        downloadJob?.cancel()
                        Log.i(TAG, "Download ___ Job: $downloadJob")
                        noNetworkConnection()
                    }
                }
                true -> {
                    runJob()
                }
            }
        }

        // Book Data Status
        viewModel.getBookDataState.observe(viewLifeCycleOwner) {
            when (it) {
                is ResultState.FAIL -> {
                    binding.lnrDownloading.visibility = View.GONE
                    binding.lnrDownloadThisBook.visibility = View.VISIBLE
                    binding.txtDownloadState.text = getString(R.string.book_data_could_not_fetched)
                    binding.btnDownloadBook.text = getString(R.string.try_again)
                    tryAgain(getString(R.string.book_data_could_not_fetched))
                    Log.e(TAG, "getBookData ___ ${it.message}")
                }
                is ResultState.LOADING -> {
                    Log.i(TAG, "getBookData ___ LOADING")
                }
                is ResultState.SUCCESS -> {
                    bookData = it.data
                    Log.i(TAG, "getBookData ___ ${bookData.toString()}")
                    filePackageFile = File("${getDir("packages", Context.MODE_PRIVATE)}/${bookData!!.packageFile}")
                    runJob()
                }
            }
        }

        // Download Status
        viewModel.downloadState.observe(viewLifeCycleOwner) {
            when (it) {
                is DownloadStatus.Started -> {}
                is DownloadStatus.Success -> {
                    binding.txtDownloadState.text = getString(R.string.completed)
                    runJob()
                }
                is DownloadStatus.Error -> {
                    binding.lnrDownloading.visibility = View.GONE
                    binding.lnrDownloadThisBook.visibility = View.VISIBLE
                    binding.txtDownloadState.text = getString(R.string.book_could_not_downloaded)
                    binding.btnDownloadBook.text = getString(R.string.try_again)
                    tryAgain(getString(R.string.book_could_not_downloaded))
                }
                is DownloadStatus.Progress -> {
                    binding.txtProgress.text = "${it.progress}%"
                    binding.progress.progress = it.progress
                }
            }
        }

        // Unzip Status
        viewModel.unzipState.observe(viewLifeCycleOwner) {
            when (it) {
                is UnzipStatus.Success -> {
                    binding.txtDownloadState.text = getString(R.string.completed)
                    deletePackageFile()
                    openBook()
                }
                is UnzipStatus.Error -> {
                    binding.lnrDownloading.visibility = View.GONE
                    binding.lnrDownloadThisBook.visibility = View.VISIBLE
                    binding.txtDownloadState.text = getString(R.string.book_could_not_prepared)
                    binding.btnDownloadBook.text = getString(R.string.try_again)
                    tryAgain(getString(R.string.book_could_not_prepared))
                }
                is UnzipStatus.Progress -> {
                    binding.txtProgress.text = "${it.progress}%"
                    binding.progress.progress = it.progress
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customDialog = CustomDialog.getInstance(context)
        filePackageFile = null
        tempUnit = { runJob() }

        coverImage = intent.getStringExtra("coverImage").toString()
        folderName = intent.getStringExtra("folderName").toString()
        destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$folderName"
        //val destination = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/animals"

        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.imgBackground.setColorFilter(ContextCompat.getColor(context, R.color.black_90), android.graphics.PorterDuff.Mode.DARKEN)
            }
        }

        // Book Image
        Glide
            .with(context)
            .load(coverImage)
            .centerCrop()
            .into(binding.imgBookCoverImage)


        permissionRequest()
        runJob()
    }

    @SuppressLint("SetTextI18n")
    private fun runJob() {
        hideDialog()
        hideLoading()

        runJob?.cancel()
        runJob = lifecycleScope.launch {
            bookData?.let {

                if (isBookExtracted()) {
                    Log.i(TAG, "Book is extracted")
                    openBook()
                } else {
                    Log.i(TAG, "Book is NOT extracted")
                    if (filePackageFile != null && isBookDownloaded()) {
                        Log.i(TAG, "Book is downloaded")
                        extractBook()
                    } else {
                        Log.i(TAG, "Book is NOT downloaded")
                        if (downloadPermissionGranted) {
                            Log.i(TAG, "Download permission is granted")
                            if (isNetworkAvailable()) {
                                Log.i(TAG, "Network is available")
                                if (readPermissionGranted) {
                                    Log.i(TAG, "Read external storage permission is granted")
                                    downloadBook()
                                } else {
                                    Log.i(TAG, "Read external storage permission is NOT granted")
                                    updateOrRequestPermissions()
                                }
                            } else {
                                Log.i(TAG, "Network is NOT available")
                                noNetworkConnection()
                            }
                        } else {
                            Log.i(TAG, "Download permission is NOT granted")

                            binding.txtDownloadState.text = "${getString(R.string.download_this_book)}\n${bookData!!.packageSize}"
                            binding.lnrDownloadThisBook.visibility = View.VISIBLE
                            binding.btnDownloadBook.setOnClickListener {
                                downloadPermissionGranted = true
                                runJob()
                            }
                        }
                    }
                }
            } ?: run {
                if (bookData == null) {
                    getBookData()
                    Log.i(TAG, "Book data is null")
                }
            }

            Log.i(TAG, "----------------------------------\n")
        }
    }

    private suspend fun getBookData() {
        if (isNetworkAvailable()) {
            viewModel.getBookData(folderName)
        } else {
            noNetworkConnection()
        }
    }

    private fun isBookDownloaded(): Boolean = filePackageFile!!.exists()

    private fun isBookExtracted(): Boolean {
        val destFolder = File(destinationFolder)
        if (destFolder.exists()) {
            val fileList = destFolder.list()
            if (fileList.size == bookData!!.packageItemsCount) {
                return true
            } else {
                fileList.forEachIndexed { index, file ->
                    File(file).let {
                        if (it.exists()) {
                            val deletion = it.delete()
                            Log.w(TAG, "$index - $file - Deletion is $deletion")
                        }
                    }
                }
            }
        }
        return false
    }

    private fun downloadBook() {
        downloadJob?.cancel()
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            downloadState = DownloadStatus.Started

            binding.lnrDownloadThisBook.visibility = View.GONE
            binding.lnrDownloading.visibility = View.VISIBLE
            binding.txtDownloadState.text = getString(R.string.downloading)

            viewModel.downloadFile(folderName, filePackageFile!!, bookData!!.packageFile)
        }
        Log.i(TAG, "Download ___ Job: $downloadJob")
    }

    private fun extractBook() {
        binding.lnrDownloadThisBook.visibility = View.GONE
        binding.lnrDownloading.visibility = View.VISIBLE
        binding.txtDownloadState.text = getString(R.string.book_is_preparing)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.unzipFile(filePackageFile!!, destinationFolder)
            } catch (e: Exception) {
                Log.e(TAG, "Unzip ___ ${e.message}")
            }
        }
    }

    private fun deletePackageFile(): Boolean {
        try {
            filePackageFile?.let {
                if (it.exists()) {
                    val deletion = it.delete()
                    Log.i(TAG, "File ___ Book file deletion is: $deletion")
                    return deletion
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "File ___ Delete: ${e.message}")
            return false
        }
    }

    private fun openBook() {
        binding.lnrDownloading.visibility = View.GONE
        binding.lnrOpenTheBook.visibility = View.VISIBLE
        binding.txtDownloadState.text = getString(R.string.open_this_book)
        binding.btnOpenBook.setOnClickListener {
            val intent = Intent(context, BookActivity::class.java)
            intent.putExtra("BOOK_DATA", bookData)
            intent.putExtra("folderName", folderName)
            startActivity(intent)
        }
    }

    /* Popup Messages */
    private fun noNetworkConnection() {
        customDialog.show(
            title = getString(R.string.network_connection_error_title),
            message = getString(R.string.network_connection_error_message),
            positiveButtonText = getString(R.string.yes),
            negativeButtonText = getString(R.string.go_to_settings),
            positiveButtonCallback = { runJob() },
            negativeButtonCallback = { openWifiSettings() },
        )
    }

    private fun tryAgain(message: String) {
        downloadJob?.cancel()
        runJob?.cancel()
        hideLoading()

        customDialog.show(
            title = getString(R.string.warning),
            message = message,
            positiveButtonText = getString(R.string.try_again),
            negativeButtonText = null,
            positiveButtonCallback = { runJob() },
            negativeButtonCallback = { null },
        )
    }
    /* -------------- */

    /* Permission */
    private fun permissionRequest() {
        permissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if (readPermissionGranted) {
                tempUnit.let { it() }
            } else {
                downloadJob?.cancel()
                hideDialog()
                Log.i(TAG, "Download ___ Job: $downloadJob")

                binding.lnrDownloading.visibility = View.GONE
                binding.txtDownloadState.text = getString(R.string.cant_download_book_without_permission)
                binding.btnDownloadBook.text = getString(R.string.try_again)
                tryAgain(getString(R.string.cant_download_book_without_permission))
            }
        }

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(context, "Environment.isExternalStorageManager()", Toast.LENGTH_SHORT).show()
            } else {
                //request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_fileS_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            //below android 11=======
            //startActivity(Intent(this, MainActivity::class.java))
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1313)
        }
         */
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }

        if (readPermissionGranted) {
            tempUnit.let { it() }
        }
    }
    /* -------------- */


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            downloadJob?.let {
                if (it.isActive) {
                    hideDialog()
                }
            }
        }
    }

    /* Custom Dialog */
    private fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String?,
        negativeButtonText: String?,
        positiveButtonCallback: () -> (Unit)?,
        negativeButtonCallback: () -> (Unit)?,
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.dialogTitle.text = title
            binding.dialogMessage.text = message

            if (!positiveButtonText.isNullOrBlank()) {
                binding.dialogPositiveButton.visibility = View.VISIBLE
                binding.dialogPositiveButton.text = positiveButtonText
            } else {
                binding.dialogPositiveButton.visibility = View.GONE
            }

            if (!negativeButtonText.isNullOrBlank()) {
                binding.dialogNegativeButton.visibility = View.VISIBLE
                binding.dialogNegativeButton.text = negativeButtonText
            } else {
                binding.dialogNegativeButton.visibility = View.GONE
            }

            binding.dialogPositiveButton.setOnClickListener {
                positiveButtonCallback() ?: run {
                    binding.frmDialog.startAnimation(this@MainActivity.animFadeOut())
                    binding.frmDialog.visibility = View.GONE
                }
            }

            binding.dialogNegativeButton.setOnClickListener {
                negativeButtonCallback() ?: run {
                    binding.frmDialog.startAnimation(this@MainActivity.animFadeOut())
                    binding.frmDialog.visibility = View.GONE
                }
            }

            binding.frmDialog.visibility = View.VISIBLE
            binding.frmDialog.startAnimation(this@MainActivity.animFadeIn())
            binding.lnrDialog.startAnimation(this@MainActivity.animBounce())
        }
    }

    private fun hideDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (binding.frmDialog.isShown) {
                binding.frmDialog.startAnimation(context.animFadeOut())
                binding.frmDialog.visibility = View.GONE

                binding.dialogTitle.text = ""
                binding.dialogMessage.text = ""
                binding.dialogPositiveButton.text = ""
                binding.dialogNegativeButton.text = ""
            }
        }
    }
    /* -------------- */
}