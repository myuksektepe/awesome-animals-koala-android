package awesome.animals.koala.prensentation.view.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityBookBinding
import awesome.animals.koala.domain.model.BookDataModel
import awesome.animals.koala.domain.model.BookPageModel
import awesome.animals.koala.prensentation.adapter.ViewPagerAdapter
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.view.fragment.PageFragment
import awesome.animals.koala.prensentation.viewmodel.BookActivityViewModel
import awesome.animals.koala.util.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job

@AndroidEntryPoint
class BookActivity : BaseActivity<BookActivityViewModel, ActivityBookBinding>() {
    override val layoutRes: Int = R.layout.activity_book
    override val viewModel: BookActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    override fun obverseViewModel() {}
    private var job: Job? = null
    private val context: Context = this
    private var bookData: BookDataModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookData = intent.getParcelableExtra("book_data")


        job = lifecycleScope.launchWhenCreated {

            val fragmentList = mutableListOf<Fragment>()
            //val json = getJsonDataFromAsset(context, "koala.json")
            //val gson = Gson().fromJson(json, BookDataModel::class.java)

            bookData?.let {
                for (page in it.pages) {
                    Log.i(TAG, "Page: $page")
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

                val pageAdapter = ViewPagerAdapter(this@BookActivity, fragmentList)

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

        }
        //job!!.cancel()
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