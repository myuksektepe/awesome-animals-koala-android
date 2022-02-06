package awesome.animals.koala.prensentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import awesome.animals.koala.R
import awesome.animals.koala.databinding.FragmentPageBinding
import awesome.animals.koala.prensentation.base.BaseFragment
import awesome.animals.koala.prensentation.viewmodel.PageFragmentViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint


private const val TITLE = "title"
private const val MESSAGE = "message"
private const val IMAGE = "image"

@AndroidEntryPoint
class PageFragment : BaseFragment<PageFragmentViewModel, FragmentPageBinding>() {

    private var title: String? = null
    private var message: String? = null
    private var image: String? = null


    override val layoutRes: Int = R.layout.fragment_page
    override val viewModel: PageFragmentViewModel by viewModels()
    override fun observeViewModel() {
        image.let {
            Glide
                .with(requireContext())
                .load(image)
                .centerCrop()
                .into(binding.imgBackground)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
            message = it.getString(MESSAGE)
            image = it.getString(IMAGE)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.txtPageTitle.text = title
        binding.txtPageMessage.text = message

        val anim_slide_in_down = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_down)
        binding.lnrTexts.run {
            visibility = View.VISIBLE
            startAnimation(anim_slide_in_down)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(title: String, message: String, image: String?) =
            PageFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(MESSAGE, message)
                    putString(IMAGE, image)
                }
            }
    }
}