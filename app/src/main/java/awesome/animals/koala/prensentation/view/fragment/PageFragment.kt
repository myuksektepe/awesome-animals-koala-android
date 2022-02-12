package awesome.animals.koala.prensentation.view.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import awesome.animals.koala.R
import awesome.animals.koala.databinding.FragmentPageBinding
import awesome.animals.koala.domain.model.PageModel
import awesome.animals.koala.prensentation.base.BaseFragment
import awesome.animals.koala.prensentation.viewmodel.PageFragmentViewModel
import awesome.animals.koala.util.TAG
import awesome.animals.koala.util.ViewExtensions.animFadeOut
import awesome.animals.koala.util.ViewExtensions.animSlideInLeft
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

    private var pageModel: PageModel? = null


    override val layoutRes: Int = R.layout.fragment_page
    override val viewModel: PageFragmentViewModel by viewModels()
    override fun observeViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //title = it.getString(TITLE)
            //message = it.getString(MESSAGE)
            //video = it.getString(video)

            pageModel = it.getParcelable<PageModel>("page_model")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Title
        binding.txtPageTitle.text = pageModel!!.title

        // Message
        binding.txtPageMessage.text = pageModel!!.message

        // Video Cover Image
        pageModel!!.video_cover.let {
            Glide
                .with(requireContext())
                .load(it)
                .centerCrop()
                .into(binding.imageBackground)
        }
    }

    override fun onResume() {
        super.onResume()

        //Log.i(TAG, "onResume")

        // Video
        pageModel!!.video.let {
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

        binding.txtPageMessage.run {
            visibility = View.VISIBLE
            startAnimation(requireContext().animSlideInLeft())
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(
            //title: String, message: String, image: String?
            pageModel: PageModel
        ) =
            PageFragment().apply {
                arguments = Bundle().apply {
                    //putString(TITLE, title)
                    //putString(MESSAGE, message)
                    //putString(video, image)

                    putParcelable(PAGE_MODEL, pageModel)
                }
            }
    }
}