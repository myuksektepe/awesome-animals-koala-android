package awesome.animals.koala.prensentation.view.activity

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityBookBinding
import awesome.animals.koala.domain.model.BookDataModel
import awesome.animals.koala.domain.model.BookPageModel
import awesome.animals.koala.prensentation.adapter.ViewPager2Adapter
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.view.fragment.PageFragment
import awesome.animals.koala.prensentation.viewmodel.BookActivityViewModel
import awesome.animals.koala.util.BOOK_NAME
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.ViewExtensions.animFadeOut
import awesome.animals.koala.util.ViewExtensions.animSlideInDown
import awesome.animals.koala.util.ViewExtensions.nextPage
import awesome.animals.koala.util.ViewExtensions.previousPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class BookActivity : BaseActivity<BookActivityViewModel, ActivityBookBinding>() {
    override val layoutRes: Int = R.layout.activity_book
    override val viewModel: BookActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    override fun obverseViewModel() {}

    private var currentPage = 0
    private var timeSeconds = 999
    private var jobTimer: Job? = null
    private val context: Context = this
    private var bookData: BookDataModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private var buttonClicked = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookData = intent.getParcelableExtra("book_data")

        lifecycleScope.launchWhenCreated {

            val fragmentList = mutableListOf<Fragment>()
            fragmentList.clear()

            bookData?.let {

                // Pages
                for (page in it.pages) {
                    if (page.is_active) {
                        val pageModel = BookPageModel(
                            title = page.title,
                            message = page.message,
                            video = page.video,
                            video_cover = page.video_cover,
                            voice = page.voice,
                            timeSeconds = page.timeSeconds,
                            is_active = page.is_active
                        )
                        fragmentList.add(PageFragment.newInstance(pageModel))
                    }
                }

                // Adapter
                val pageAdapter = ViewPager2Adapter(this@BookActivity, fragmentList)

                // ViewPager 2
                binding.viewPager2.apply {
                    currentItem = currentPage
                    offscreenPageLimit = 1
                    isUserInputEnabled = false
                    adapter = pageAdapter
                    //setPageTransformer(ZoomOutPageTransformer())
                    /*
                    setPageTransformer { page, position ->
                        setParallaxTransformation(page, position)
                    }
                     */
                    registerOnPageChangeCallback(viewpagerPageChangeCallback)
                }

                pageChanged(currentPage)
            }
        }

        // Buttons
        binding.imgNextPage.setOnClickListener { binding.viewPager2.nextPage() }
        binding.imgPrevPage.setOnClickListener { binding.viewPager2.previousPage() }
    }

    private fun pageChanged(pageNumber: Int) {
        Log.i(TAG, "Page Changed: $pageNumber")

        // Disable Buttons
        disableButtons()

        // Find Page Time
        val seconds = bookData!!.pages[pageNumber].timeSeconds + 1
        val timeLong = (seconds * 1000).toLong()
        Log.i(TAG, "Time Long: $timeLong")

        jobTimer?.cancel()
        jobTimer = lifecycleScope.launch {
            delay(timeLong)
            enableButtons()
        }
    }

    private fun disableButtons() {
        binding.frmButtons.isEnabled = false
        binding.frmButtons.isClickable = false
        binding.frmButtons.children.forEach {
            it.isEnabled = false
            it.isClickable = false
        }
        binding.imgPrevPage.isClickable = false
        binding.imgNextPage.isClickable = false
        binding.frmButtons.startAnimation(animFadeOut())
        //binding.frmButtons.visibility = View.GONE
        //binding.frmButtons.alpha = 0f
    }

    private fun enableButtons() {
        binding.frmButtons.visibility = View.VISIBLE
        binding.frmButtons.isEnabled = true
        binding.frmButtons.isClickable = true
        binding.imgPrevPage.isClickable = true
        binding.imgNextPage.isClickable = true
        binding.frmButtons.children.forEach {
            it.isEnabled = true
            it.isClickable = true
        }
        binding.frmButtons.startAnimation(animSlideInDown())
        //binding.frmButtons.alpha = 0f
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        // Song
        val destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$BOOK_NAME"
        val song = "$destinationFolder/${bookData?.song}"
        if (File(song).exists()) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer().apply {
                isLooping = true
                setVolume(.2f, .2f)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(song))
                prepare()
                start()
                setOnErrorListener { mp, what, extra -> true }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //binding.viewPager2.unregisterOnPageChangeCallback(viewpagerPageChangeCallback)
    }

    var viewpagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageChanged(position)
            Log.i(TAG, "ViewPager ___ Position: $position")
        }
    }

    override fun onBackPressed() {
        if (binding.viewPager2.currentItem == 0) {
            super.onBackPressed()
        } else {
            //binding.viewPager2.currentItem = binding.viewPager2.currentItem - 1
        }
    }


    private fun setParallaxTransformation(page: View, position: Float) {
        page.apply {
            val parallaxView = this.findViewById<RelativeLayout>(R.id.rltBackground)
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
}