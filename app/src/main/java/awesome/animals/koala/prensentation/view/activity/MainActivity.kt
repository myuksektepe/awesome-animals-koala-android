package awesome.animals.koala.prensentation.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import awesome.animals.koala.R
import awesome.animals.koala.data.network.downloadFile
import awesome.animals.koala.databinding.ActivityMainBinding
import awesome.animals.koala.domain.model.DownloadStatus
import awesome.animals.koala.domain.model.PageModel
import awesome.animals.koala.domain.model.PagesModel
import awesome.animals.koala.prensentation.adapter.ViewPagerAdapter
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.view.fragment.PageFragment
import awesome.animals.koala.prensentation.viewmodel.MainActivityViewModel
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.getJsonDataFromAsset
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    private val context: Context = this@MainActivity
    override fun obverseViewModel() {
    }

    private var job: Job? = null
    private val client = HttpClient(CIO)
    private val url = "https://api.rit.im/obi-dahi/awesome-animals/koala/package.zip"
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        file = File("${getDir("packages", Context.MODE_PRIVATE)}/koala.zip")

        downloadWithFlow()

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

        /*
        this.showCustomDialog(
            title = "Selam",
            message = "Koalaları Tanımaya Hazır Mısın?",
            positiveButtonText = "Evet",
            negativeButtonText = "Belki Sonra",
            positiveButtonCallback = { null },
            negativeButtonCallback = {},
            cancelable = false
        )
         */
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

    @SuppressLint("SetTextI18n")
    private fun downloadWithFlow() {
        CoroutineScope(Dispatchers.IO).launch {
            client.downloadFile(file!!, url).collect {
                withContext(Dispatchers.Main) {
                    binding.frmLoading.visibility = View.VISIBLE
                    when (it) {
                        is DownloadStatus.Success -> {
                            binding.txtProgress.text = "İndirme Başarılı: ${it}"
                        }
                        is DownloadStatus.Error -> {
                            binding.txtProgress.text = it.message
                        }
                        is DownloadStatus.Progress -> {
                            binding.txtProgress.text = "${it.progress}%"
                            binding.progress.progress = it.progress
                        }
                    }
                }
            }
        }
    }
}