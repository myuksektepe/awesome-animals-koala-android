package awesome.animals.koala.prensentation.view.fragment

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import awesome.animals.koala.R
import awesome.animals.koala.databinding.FragmentPageBinding
import awesome.animals.koala.domain.model.BookPageModel
import awesome.animals.koala.prensentation.base.BaseFragment
import awesome.animals.koala.prensentation.viewmodel.PageFragmentViewModel
import awesome.animals.koala.util.BOOK_NAME
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.ViewExtensions.animFadeOut
import awesome.animals.koala.util.ViewExtensions.animSlideInDown
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint


private const val TITLE = "title"
private const val MESSAGE = "message"
private const val VIDEO = "video"
private const val VIDEO_COVER = "video_cover"

private const val PAGE_MODEL = "page_model"

@AndroidEntryPoint
class PageFragment : BaseFragment<PageFragmentViewModel, FragmentPageBinding>() {

    //private var title: String? = null
    //private var message: String? = null
    //private var video: String? = null
    //private var video_cover: String? = null

    private lateinit var pageModel: BookPageModel
    private lateinit var destinationFolder: String
    private lateinit var video: String
    private lateinit var video_cover: String
    private lateinit var voice: String
    private var mediaPlayer: MediaPlayer? = null
    override val layoutRes: Int = R.layout.fragment_page
    override val viewModel: PageFragmentViewModel by viewModels()
    override fun observeViewModel() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //title = it.getString(TITLE)
            //message = it.getString(MESSAGE)
            //video = it.getString(video)
            pageModel = it.getParcelable("page_model")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destinationFolder = "${requireActivity().getDir("packages", Context.MODE_PRIVATE)}/$BOOK_NAME"
        video = "$destinationFolder/${pageModel.video}"
        video_cover = "$destinationFolder/${pageModel.video_cover}"
        voice = "$destinationFolder/${pageModel.voice}"

        // Title
        binding.txtPageTitle.text = pageModel.title

        // Message
        binding.txtPageMessage.text = pageModel.message

        // Video Cover Image
        video_cover.let {
            Glide
                .with(requireContext())
                .load(it)
                .centerCrop()
                .into(binding.imageBackground)
        }
    }

    override fun onResume() {
        super.onResume()

        // Video
        video.let {
            binding.videoBackground.apply {
                setVideoPath(it)
                start()
                setOnPreparedListener {
                    Log.i(TAG, "setOnCompletionListener")
                    it.isLooping = true

                    val videoRatio = it.videoWidth / it.videoHeight.toFloat()
                    val screenRatio = this.width / this.height.toFloat()
                    val scaleX = videoRatio / screenRatio
                    if (scaleX >= 1f) {
                        this.scaleX = scaleX
                    } else {
                        this.scaleY = 1f / scaleX
                    }
                }
                setOnCompletionListener {
                    Log.i(TAG, "setOnCompletionListener")
                    it.start()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "setOnErrorListener what: $what")
                    Log.e(TAG, "setOnErrorListener extra: $extra")
                    binding.imageBackground.visibility = View.VISIBLE
                    true
                }
                setOnInfoListener { mp, what, extra ->
                    Log.i(TAG, "setOnInfoListener what: $what")
                    Log.i(TAG, "setOnInfoListener extra: $extra")

                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        binding.imageBackground.startAnimation(requireContext().animFadeOut())
                        true
                    }
                    false
                }
            }

        }

        // Voice
        voice.let {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(requireContext(), Uri.parse(voice))
                prepare()
                start()
            }
        }

        binding.txtPageMessage.run {
            visibility = View.VISIBLE
            startAnimation(requireContext().animSlideInDown())
        }

    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            //title: String, message: String, image: String?
            pageModel: BookPageModel
        ) =
            PageFragment().apply {
                arguments = Bundle().apply {
                    //putString(TITLE, title)
                    //putString(MESSAGE, message)
                    //putString(video, image)

                    putParcelable(PAGE_MODEL, pageModel as Parcelable)
                }
            }
    }
}