package awesome.animals.koala.prensentation.view.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityMainBinding
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.domain.model.UnzipStatus
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.viewmodel.MainActivityViewModel
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.ViewExtensions.animBounce
import awesome.animals.koala.util.ViewExtensions.animFadeIn
import awesome.animals.koala.util.ViewExtensions.animFadeOut
import awesome.animals.koala.util.openWifiSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    private val context: Context = this@MainActivity
    private var job: Job? = null
    private var downloadJob: Job? = null
    private val url = "https://api.rit.im/obi-dahi/awesome-animals/koala/package.zip"
    private var downloadState: DownloadStatus? = null
    private var unzipState: UnzipStatus? = null
    private var tempUnit: () -> Unit? = { null }
    private lateinit var packageFile: File
    private lateinit var destinationFolder: String

    private var downloadPermissionGranted = false
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

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
                    if (downloadPermissionGranted) {
                        runJob()
                    }
                }
            }
        }

        // Download Status
        viewModel.downloadState.observe(viewLifeCycleOwner) {
            when (it) {
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
        /*
        job = lifecycleScope.launchWhenCreated {

            val fragmentList = mutableListOf<Fragment>()
            val json = getJsonDataFromAsset(context, "koala.json")
            val gson = Gson().fromJson(json, PagesModel::class.java)

            for (page in gson.pages) {
                Log.i(TAG, "Page: $page")
                val pageModel = PageModel(
                    title = page.title,
                    message = page.message,
                    video = page.video,
                    video_cover = page.video_cover
                )
                fragmentList.add(PageFragment.newInstance(pageModel))
            }

            val pageAdapter = ViewPagerAdapter(this@MainActivity, fragmentList)

            binding.viewPager.run {
                currentItem = 1
                adapter = pageAdapter
                //setPageTransformer(DepthPageTransformer())
                setPageTransformer { page, position ->
                    setParallaxTransformation(page, position)
                }
                /*
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        Log.i(TAG, "Pager Position: $position")
                        //when (position) { }
                    }
                })
                 */
            }

        }
        job!!.cancel()
         */

        tempUnit = { runJob() }
        packageFile = File("${getDir("packages", Context.MODE_PRIVATE)}/koala.zip")
        destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/koala"
        //val destination = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/koala"
        permissionRequest()

        runJob()
    }

    private fun runJob() {
        hideDialog()
        if (isBookDownloaded()) {
            Log.i(TAG, "Book is downloaded")
            if (isBookExtracted()) {
                Log.i(TAG, "Book is extracted")
                openBook()
            } else {
                Log.i(TAG, "Book is NOT extracted")
                extractBook()
            }
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

                binding.txtDownloadState.text = getString(R.string.download_this_book)
                binding.lnrDownloadThisBook.visibility = View.VISIBLE
                binding.btnDownloadBook.setOnClickListener {
                    downloadPermissionGranted = true
                    runJob()
                }
            }
        }
        Log.i(TAG, "----------------------------------\n")
    }

    private fun isBookDownloaded(): Boolean = packageFile.exists()

    private fun isBookExtracted(): Boolean = (File(destinationFolder).exists() && unzipState == UnzipStatus.Success)

    private fun downloadBook() {
        downloadJob?.cancel()
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            downloadState = DownloadStatus.Started

            binding.lnrDownloadThisBook.visibility = View.GONE
            binding.lnrDownloading.visibility = View.VISIBLE
            binding.txtDownloadState.text = getString(R.string.downloading)

            viewModel.downloadFile(packageFile, url)
        }
        Log.i(TAG, "Download ___ Job: $downloadJob")
    }

    private fun extractBook() {
        binding.lnrDownloadThisBook.visibility = View.GONE
        binding.lnrDownloading.visibility = View.VISIBLE
        binding.txtDownloadState.text = getString(R.string.book_is_preparing)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.unzipFile(packageFile, destinationFolder)
            } catch (e: Exception) {
                Log.e(TAG, "Unzip ___ ${e.message}")
            }
        }
    }

    private fun openBook() {
        Log.i(TAG, "Open the book bitch!")
    }

    /* Popup Messages */
    private fun noNetworkConnection() {
        showDialog(
            title = getString(R.string.network_connection_error_title),
            message = getString(R.string.network_connection_error_message),
            positiveButtonText = getString(R.string.yes),
            negativeButtonText = getString(R.string.go_to_settings),
            positiveButtonCallback = { runJob() },
            negativeButtonCallback = { openWifiSettings() },
        )
    }

    private fun tryAgain(message: String) {
        showDialog(
            title = getString(R.string.warning),
            message = message,
            positiveButtonText = getString(R.string.try_again),
            negativeButtonText = null,
            positiveButtonCallback = { runJob() },
            negativeButtonCallback = { null },
        )
    }
    /* -------------- */

    
    private fun setParallaxTransformation(page: View, position: Float) {
        page.apply {
            val parallaxView = this.findViewById<CardView>(R.id.crdPager)
            when {
                position < -1 -> // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 1f
                position <= 1 -> { // [-1,1]
                    parallaxView.translationX = -position * (width / 2) //Half the normal speed
                }
                else -> // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 1f
            }
        }
    }

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
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
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