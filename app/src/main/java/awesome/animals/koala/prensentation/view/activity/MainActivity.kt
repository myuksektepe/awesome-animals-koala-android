package awesome.animals.koala.prensentation.view.activity

import android.os.Bundle
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityMainBinding
import awesome.animals.koala.prensentation.adapter.ViewPagerAdapter
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.view.fragment.PageFragment
import awesome.animals.koala.prensentation.viewmodel.MainActivityViewModel
import awesome.animals.koala.util.TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    override fun obverseViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentList = listOf<Fragment>(
            PageFragment.newInstance(
                "Koalalar",
                "Avustralya’nın doğu ve güneydoğu kısımlarında yaşayan keseli memelilerdendir.",
                "https://bilimteknik.tubitak.gov.tr/sites/default/files/styles/770px_node/public/ekran_resmi_2020-01-31_16.41.06.png"
            ),
            PageFragment.newInstance(
                "Kesede Büyüyorlar",
                "Yavruları gelişimlerini tamamlamadan (2-3 cm boylarında) doğar ve annelerinin keselerinde gelişimlerini tamamlarlar.",
                "https://i.cnnturk.com/i/cnnturk/75/1200x720/572c9a4bae7849287835a156.jpg"
            ),
            PageFragment.newInstance(
                "Çok Tatlılar 0x1F60D",
                "İlk 6 ay tamamen annelerinin keselerinde yaşarlar.",
                "https://news.griffith.edu.au/wp-content/uploads/2020/04/dog-training-koala-sml--1200x720.jpg"
            ),
        )

        val pageAdapter = ViewPagerAdapter(this, fragmentList)

        binding.viewPager.run {
            currentItem = 1
            adapter = pageAdapter
            //setPageTransformer(DepthPageTransformer())
            setPageTransformer { page, position ->
                setParallaxTransformation(page, position)
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.i(TAG, "Pager Position: $position")
                    //when (position) { }
                }
            })
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
            val parallaxView = this.findViewById<AppCompatImageView>(R.id.imgBackground)
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