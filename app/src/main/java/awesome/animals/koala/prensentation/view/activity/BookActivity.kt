package awesome.animals.koala.prensentation.view.activity

import android.content.Context
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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
import awesome.animals.koala.util.ViewExtensions.animSlideInDown
import awesome.animals.koala.util.ViewExtensions.animSlideOutDown
import awesome.animals.koala.util.ViewExtensions.nextPage
import awesome.animals.koala.util.ViewExtensions.previousPage
import awesome.animals.koala.util.ViewExtensions.showCustomDialog
import com.bumptech.glide.Glide
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
    private var mediaPlayerLength = 0
    private var jobTimer: Job? = null
    private val context: Context = this
    private var bookData: BookDataModel? = null
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$BOOK_NAME"

        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.imgBackground.setColorFilter(ContextCompat.getColor(context, R.color.black_90), android.graphics.PorterDuff.Mode.DARKEN)
                binding.imgCircle.setColorFilter(ContextCompat.getColor(context, R.color.black_90), android.graphics.PorterDuff.Mode.DARKEN)
            }
        }

        bookData = intent.getParcelableExtra("book_data")

        lifecycleScope.launchWhenCreated {

            val fragmentList = mutableListOf<Fragment>()
            fragmentList.clear()

            bookData?.let {

                // Background Image
                val backgroundImage = "$destinationFolder/${it.backgroundImage}"
                if (File(backgroundImage).exists()) {
                    Glide
                        .with(context)
                        .load(backgroundImage)
                        .centerCrop()
                        .into(binding.imgBackground)
                } else {
                    Glide
                        .with(context)
                        .load(R.drawable.page_background)
                        .centerCrop()
                        .into(binding.imgBackground)
                }

                // Pages
                for (page in it.pages) {
                    if (page.isActive) {
                        val pageModel = BookPageModel(
                            title = page.title,
                            message = page.message,
                            video = page.video,
                            videoCover = page.videoCover,
                            voice = page.voice,
                            timeSeconds = page.timeSeconds,
                            isActive = page.isActive
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
        binding.btnNext.setOnClickListener { binding.viewPager2.nextPage() }
        binding.btnPrev.setOnClickListener { binding.viewPager2.previousPage() }
        binding.btnMute.setOnClickListener { button ->
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    mediaPlayerLength = it.currentPosition
                    button.setBackgroundResource(R.drawable.ic_music_off)
                } else {
                    it.seekTo(mediaPlayerLength)
                    it.start()
                    button.setBackgroundResource(R.drawable.ic_music_on)
                }
            }
        }
        binding.btnExit.setOnClickListener { closeBook() }
    }

    private fun pageChanged(pageNumber: Int) {
        Log.i(TAG, "Page Changed: $pageNumber")

        // Disable Buttons
        disableButtons()

        // Find Page Time
        val seconds = bookData!!.pages[pageNumber].timeSeconds + 1
        val timeLong = (seconds * 1000).toLong()
        Log.i(TAG, "Time Long: $timeLong")

        // Button Hide/Show
        if (pageNumber == 0) {
            binding.btnPrev.visibility = View.GONE
        } else {
            binding.btnPrev.visibility = View.VISIBLE
        }

        if (pageNumber == (bookData!!.pages.size - 1)) {
            binding.btnNext.visibility = View.GONE
        } else {
            binding.btnNext.visibility = View.VISIBLE
        }

        jobTimer?.cancel()
        jobTimer = lifecycleScope.launch {
            delay(timeLong)
            enableButtons()
        }
    }

    private fun disableButtons() {
        binding.frmButtons.startAnimation(animSlideOutDown())
        binding.frmButtons.isEnabled = false
        binding.frmButtons.isClickable = false
        binding.frmButtons.children.forEach {
            it.isEnabled = false
            it.isClickable = false
        }
        binding.btnPrev.isClickable = false
        binding.btnNext.isClickable = false
        //binding.frmButtons.visibility = View.GONE
        //binding.frmButtons.alpha = 0f
    }

    private fun enableButtons() {
        binding.frmButtons.visibility = View.VISIBLE
        binding.frmButtons.isEnabled = true
        binding.frmButtons.isClickable = true
        binding.btnPrev.isClickable = true
        binding.btnNext.isClickable = true
        binding.frmButtons.children.forEach {
            it.isEnabled = true
            it.isClickable = true
        }
        binding.frmButtons.startAnimation(animSlideInDown())
        //binding.frmButtons.alpha = 0f
    }

    private fun closeBook() {
        showCustomDialog(
            title = getString(R.string.are_you_sure),
            message = getString(R.string.exit_to_book),
            cancelable = false,
            positiveButtonText = getString(R.string.no),
            negativeButtonText = getString(R.string.yes),
            positiveButtonCallback = { null },
            negativeButtonCallback = { finish() }
        )
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        // Song
        val destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$BOOK_NAME"
        val songPath = "$destinationFolder/${bookData?.backgroundSong}"

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaPlayer = MediaPlayer().apply {
            isLooping = true
            setVolume(.1f, .1f)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            //setDataSource(context, Uri.parse(song))
        }

        if (!bookData?.backgroundSong.isNullOrBlank() && File(songPath).exists()) {
            mediaPlayer?.setDataSource(context, Uri.parse(songPath))
        } else {
            val descriptor = assets.openFd("song.mp3")
            mediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
        }

        mediaPlayer?.apply {
            prepareAsync()
            setOnPreparedListener {
                it.start()
            }
            //prepare()
            //start()
            setOnErrorListener { _, _, _ -> true }
        }
    }

    override fun onBackPressed() {
        if (binding.viewPager2.currentItem == 0) {
            //super.onBackPressed()
            closeBook()
        } else {
            //binding.viewPager2.currentItem = binding.viewPager2.currentItem - 1
        }
    }

    private var viewpagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageChanged(position)
            Log.i(TAG, "ViewPager ___ Position: $position")
        }
    }

    /*
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
     */
}