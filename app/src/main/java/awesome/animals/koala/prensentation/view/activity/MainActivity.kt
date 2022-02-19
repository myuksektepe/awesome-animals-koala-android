package awesome.animals.koala.prensentation.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityMainBinding
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.viewmodel.MainActivityViewModel
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.UnzipUtils.unzip
import awesome.animals.koala.util.ViewExtensions.animBounce
import awesome.animals.koala.util.ViewExtensions.animFadeIn
import awesome.animals.koala.util.ViewExtensions.animFadeOut
import awesome.animals.koala.util.openWifiSettings
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var file: File
    private var downloadState: DownloadStatus? = null

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>


    override fun obverseViewModel() {
        networkConnection.observe(viewLifeCycleOwner) {
            Log.i(TAG, "Network Connection: $it")

            when (it) {
                false -> {
                    stopJob()
                }
                true -> {
                    runJob()
                }
            }
        }

        viewModel.downloadState.observe(viewLifeCycleOwner) {
            when (it) {
                is DownloadStatus.Success -> {
                    binding.txtProgress.text = "İndirme Başarılı: ${it}"
                    unzipFile()
                }
                is DownloadStatus.Error -> {
                    binding.txtProgress.text = it.message
                }
                is DownloadStatus.Progress -> {
                    binding.txtProgress.text = "${it.progress}%"
                    binding.progress.progress = it.progress
                    Log.i(TAG, "Progress: ${it.progress} %")
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


        permissionRequest()
        updateOrRequestPermissions()

        file = File("${getDir("packages", Context.MODE_PRIVATE)}/koala.zip")

        binding.btnStop.setOnClickListener {
            stopJob()
        }

        binding.btnDownload.setOnClickListener {
            runJob()
        }

    }

    private fun runJob() {
        hideDialog()
        if (!checkFileExists()) {
            if (isNetworkAvailable()) {
                downloadWithFlow()
            } else {
                noNetworkConnection()
            }
        } else {
            unzipFile()
        }
    }

    private fun stopJob() {
        downloadJob?.cancel()
        Log.i(TAG, "DownloadJob: $downloadJob")

        if (downloadState == DownloadStatus.Started) {
            noNetworkConnection()
        }
    }

    private fun unzipFile() {
        binding.txtProgress.text = "Dosya açılıyor"
        //val destination = "${getDir("packages", Context.MODE_PRIVATE)}/koala/"
        val destination = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/koala/"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                unzip(file, destination)
            } catch (e: Exception) {
                Log.e(TAG, "Unzip: ${e.message}")
            }
        }
    }

    private fun checkFileExists(): Boolean {
        if (file.exists()) {
            Log.i(TAG, "Dosya zaten mevcut! ${file.absolutePath}")
            return true
        } else {
            return false
        }
    }

    private fun downloadWithFlow() {
        downloadJob?.cancel()
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            downloadState = DownloadStatus.Started
            binding.frmDownloading.visibility = View.VISIBLE
            viewModel.downloadFile(file, url)
        }
        Log.i(TAG, "downloadJob: $downloadJob")
    }


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
                //tempUnit?.let {it()}
            } else {
                val snackbar = Snackbar.make(
                    binding.root,
                    getString(R.string.cant_read_files_without_permission),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

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

        //Log.i(TAG, "readPermissionGranted: $readPermissionGranted")

        if (readPermissionGranted) {
            //tempUnit?.let { it() }
        }
    }
}