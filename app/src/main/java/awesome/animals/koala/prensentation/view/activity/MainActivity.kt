package awesome.animals.koala.prensentation.view.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityMainBinding
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

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    private val context: Context = this@MainActivity
    override fun obverseViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {

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
}