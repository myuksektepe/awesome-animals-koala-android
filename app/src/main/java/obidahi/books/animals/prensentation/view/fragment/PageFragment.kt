package obidahi.books.animals.prensentation.view.fragment

import android.animation.ObjectAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import obidahi.books.animals.R
import obidahi.books.animals.databinding.FragmentPageAltBinding
import obidahi.books.animals.domain.model.BookPageModel
import obidahi.books.animals.prensentation.base.BaseFragment
import obidahi.books.animals.prensentation.viewmodel.PageFragmentViewModel
import obidahi.books.animals.util.BOOK_NAME
import obidahi.books.animals.util.TAG
import obidahi.books.animals.util.ViewExtensions.animFadeIn
import obidahi.books.animals.util.ViewExtensions.animFadeOut
import obidahi.books.animals.util.ViewExtensions.animSlideInDown
import obidahi.books.animals.util.ViewExtensions.animSlideInUp
import java.io.File

private const val PAGE_MODEL = "PAGE_MODEL"

@AndroidEntryPoint
class PageFragment : BaseFragment<PageFragmentViewModel, FragmentPageAltBinding>() {
    private lateinit var pageModel: BookPageModel
    private lateinit var video: String
    private lateinit var image: String
    private lateinit var voice: String
    private val alt = false
    private var mediaPlayer: MediaPlayer? = null
    override val layoutRes: Int = R.layout.fragment_page_alt
    override val viewModel: PageFragmentViewModel by viewModels()
    override fun observeViewModel() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageModel = if (Build.VERSION.SDK_INT >= 33) {
                it.getParcelable(PAGE_MODEL, BookPageModel::class.java)!!
            } else {
                it.getParcelable(PAGE_MODEL)!!
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val destinationFolder = "${requireActivity().getDir("packages", Context.MODE_PRIVATE)}/$BOOK_NAME"
        video = "$destinationFolder/${pageModel.video}"
        image = "$destinationFolder/${pageModel.image}"
        voice = "$destinationFolder/${pageModel.voice}"

        // Message
        binding.txtPageMessage.text = ""
        binding.txtPageMessage.visibility = View.INVISIBLE

        // Video Cover Image
        if (File(image).exists()) {
            Glide
                .with(requireContext())
                .load(image)
                .centerCrop()
                .into(binding.imageBackground)

            // Image Owner
            pageModel.imageOwner.let {
                binding.txtMediaOwner.run {
                    this.visibility = View.VISIBLE
                    text = it
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        ObjectAnimator.ofFloat(binding.cardView, View.ROTATION, -8f, 8f).apply {
            duration = 5000
            repeatCount = 10
            repeatMode = ObjectAnimator.REVERSE
            start()
        }


        // Video
        if (File(video).exists()) {
            binding.videoBackground.apply {
                visibility = View.VISIBLE
                setVideoPath(video)
                setOnPreparedListener {
                    it.isLooping = true
                    it.setVolume(0f, 0f)

                    /*
                    val videoRatio = it.videoWidth / it.videoHeight.toFloat()
                    val screenRatio = this.width / this.height.toFloat()
                    val scaleX = videoRatio / screenRatio
                    if (scaleX >= 1f) {
                        this.scaleX = scaleX
                    } else {
                        this.scaleY = 1f / scaleX
                    }
                    //setZOrderOnTop(true)
                     */

                    it.start()
                }
                setOnCompletionListener {
                    binding.videoBackground.visibility = View.VISIBLE
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Video setOnErrorListener what: $what")
                    Log.e(TAG, "Video setOnErrorListener extra: $extra")
                    binding.videoBackground.visibility = View.GONE
                    true
                }
                setOnInfoListener { mp, what, extra ->
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        binding.imageBackground.startAnimation(requireContext().animFadeOut())

                        // Video Owner
                        binding.txtMediaOwner.visibility = View.GONE
                        pageModel.videoOwner.let {
                            binding.txtMediaOwner.run {
                                this.visibility = View.VISIBLE
                                text = it
                            }
                        }

                        true
                    }
                    false
                }
            }
        }

        // Voice
        if (File(voice).exists()) {

            Log.i(TAG, "Voice: $voice")
            Log.i(TAG, "File Voice: ${File(voice)}")
            Log.i(TAG, "Uri Parse Voice: ${Uri.parse(voice)}")

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer().apply {
                setVolume(1f, 1f)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(requireContext(), Uri.parse(voice))
                //prepare()
                prepareAsync()
                setOnPreparedListener {
                    lifecycleScope.launch {
                        delay(1000)
                        it.start()
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Voice setOnErrorListener what: $what")
                    Log.e(TAG, "Voice setOnErrorListener extra: $extra")
                    true
                }
                setOnInfoListener { mp, what, extra ->
                    Log.e(TAG, "Voice setOnInfoListener what: $what")
                    Log.e(TAG, "Voice setOnInfoListener extra: $extra")
                    true
                }
            }
        }

        // Message
        binding.txtPageMessage.run {
            visibility = View.VISIBLE
            this.text = pageModel.message
            if (alt) {
                startAnimation(requireContext().animSlideInDown())
            } else {
                startAnimation(requireContext().animSlideInUp())
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        binding.videoBackground.apply {
            stopPlayback()
            visibility = View.GONE
        }
        binding.imageBackground.startAnimation(requireContext().animFadeIn())
        binding.txtPageMessage.text = ""
        binding.txtPageMessage.visibility = View.INVISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance(
            pageModel: BookPageModel
        ) =
            PageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PAGE_MODEL, pageModel as Parcelable)
                }
            }
    }
}