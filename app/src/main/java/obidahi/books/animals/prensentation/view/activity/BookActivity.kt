package obidahi.books.animals.prensentation.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import obidahi.books.animals.R
import obidahi.books.animals.databinding.ActivityBookBinding
import obidahi.books.animals.domain.model.BookDataModel
import obidahi.books.animals.domain.model.BookPageModel
import obidahi.books.animals.prensentation.adapter.ViewPagerAdapter
import obidahi.books.animals.prensentation.base.BaseActivity
import obidahi.books.animals.prensentation.view.fragment.PageFragment
import obidahi.books.animals.prensentation.viewmodel.BookActivityViewModel
import obidahi.books.animals.prensentation.viewmodel.MainActivityViewModel
import obidahi.books.animals.util.ResultState
import obidahi.books.animals.util.TAG
import obidahi.books.animals.util.ViewExtensions.animSlideInDown
import obidahi.books.animals.util.ViewExtensions.animSlideOutDown
import obidahi.books.animals.util.ViewExtensions.nextPage
import obidahi.books.animals.util.ViewExtensions.previousPage
import obidahi.books.animals.util.ViewExtensions.showCustomDialog
import java.io.File


@AndroidEntryPoint
class BookActivity : BaseActivity<BookActivityViewModel, ActivityBookBinding>() {
    override val layoutRes: Int = R.layout.activity_book
    override val viewModel: BookActivityViewModel by viewModels()
    private val viewModel2: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    private var currentPage = 0
    private var mediaPlayerLength = 0
    private var isSongStarted = false
    private var isSongPlaying = false
    private var jobTimer: Job? = null
    private val context: Context = this
    private var bookData: BookDataModel? = null
    private var folderName: String = ""
    private var mediaPlayer: MediaPlayer? = null
    private var destinationFolder: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                //binding.imgBackground.setColorFilter(ContextCompat.getColor(context, R.color.black_90), android.graphics.PorterDuff.Mode.DARKEN)
                //binding.imgCircle.setColorFilter(ContextCompat.getColor(context, R.color.black_90), android.graphics.PorterDuff.Mode.DARKEN)
            }
        }

        folderName = intent.getStringExtra("folderName").toString()
        destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$folderName"

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel2.getBookData(folderName)
        }

        /*
        bookData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("BOOK_DATA", BookDataModel::class.java)
        } else {
            intent.getParcelableExtra<BookDataModel>("BOOK_DATA")
        }
         */

        // Buttons
        binding.btnNext.setOnClickListener { binding.viewPager.nextPage() }
        binding.btnPrev.setOnClickListener { binding.viewPager.previousPage() }
        binding.btnClose.setOnClickListener { closeBook() }
        binding.btnMute.setOnClickListener { button ->
            mediaPlayer?.let {
                if (isSongPlaying) {
                    it.pause()
                    mediaPlayerLength = it.currentPosition
                    button.setBackgroundResource(R.drawable.ic_music_off)
                    isSongPlaying = false
                } else {
                    it.seekTo(mediaPlayerLength)
                    it.start()
                    button.setBackgroundResource(R.drawable.ic_music_on)
                    isSongPlaying = true
                }
            }
        }
        binding.btnExit.setOnClickListener { closeBook() }
    }

    override fun obverseViewModel() {
        // Book Data Status
        viewModel2.getBookDataState.observe(viewLifeCycleOwner) {
            when (it) {
                is ResultState.LOADING -> {
                    showLoading()
                }
                is ResultState.FAIL -> {
                    hideLoading()
                    showCustomDialog(
                        getString(R.string.error),
                        it.message,
                        null,
                        null,
                        { null },
                        { null }
                    )
                }
                is ResultState.SUCCESS -> {
                    hideLoading()
                    bookData = it.data
                    setBook()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pageChanged(pageNumber: Int) {
        Log.i(TAG, "Page Changed: $pageNumber")

        // Change page number
        binding.txtPageNumber.text = "${pageNumber + 1} / ${bookData!!.pages.size}"

        // Disable Buttons
        disableButtons()

        // Find Page Time
        val seconds = bookData!!.pages[pageNumber].time + 1
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
            binding.btnClose.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnClose.visibility = View.GONE
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
            message = getString(R.string.are_you_sure_exit_to_book),
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
        if (isSongStarted && isSongPlaying) {
            playSong()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
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

    private fun setBook() {
        lifecycleScope.launch(Dispatchers.Main) {

            val fragmentList = mutableListOf<Fragment>()
            fragmentList.clear()

            bookData?.let {

                // Book Title
                binding.txtBookTitle.text = it.bookTitle

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
                            videoOwner = page.videoOwner,
                            image = page.image,
                            imageOwner = page.imageOwner,
                            voice = page.voice,
                            time = page.time,
                            isActive = page.isActive
                        )
                        fragmentList.add(PageFragment.newInstance(pageModel, folderName))
                    }
                }

                // Adapter
                val pageAdapter = ViewPagerAdapter(this@BookActivity, fragmentList)

                // ViewPager 2
                binding.viewPager.apply {
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

                playSong()
                pageChanged(currentPage)
            }
        }
    }

    private fun playSong() {
        // Song
        val destinationFolder = "${getDir("packages", Context.MODE_PRIVATE)}/$folderName"
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
                binding.btnMute.setBackgroundResource(R.drawable.ic_music_on)
                isSongStarted = true
                isSongPlaying = true
            }
            //prepare()
            //start()
            setOnErrorListener { _, _, _ -> true }
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