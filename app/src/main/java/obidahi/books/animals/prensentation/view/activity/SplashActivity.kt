package obidahi.books.animals.prensentation.view.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import obidahi.books.animals.R
import obidahi.books.animals.databinding.ActivitySplashBinding
import obidahi.books.animals.prensentation.base.BaseActivity
import obidahi.books.animals.prensentation.viewmodel.SplashActivityViewModel
import obidahi.books.animals.util.TAG

class SplashActivity : BaseActivity<SplashActivityViewModel, ActivitySplashBinding>() {

    override val layoutRes: Int = R.layout.activity_splash
    override val viewModel: SplashActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this
    override fun obverseViewModel() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intro = "android.resource://$packageName/raw/intro"

        binding.videoSplash.apply {
            visibility = View.VISIBLE
            setVideoURI(Uri.parse(intro))
            setOnPreparedListener {
                it.isLooping = false
                it.setVolume(.2f, .2f)

                val videoRatio = it.videoWidth / it.videoHeight.toFloat()
                val screenRatio = this.width / this.height.toFloat()
                val scaleX = videoRatio / screenRatio
                if (scaleX >= 1f) {
                    this.scaleX = scaleX
                } else {
                    this.scaleY = 1f / scaleX
                }
                //setZOrderOnTop(true)
                it.start()
            }
            setOnCompletionListener {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
            setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "setOnErrorListener what: $what")
                Log.e(TAG, "setOnErrorListener extra: $extra")
                binding.videoSplash.visibility = View.GONE
                true
            }
            setOnInfoListener { mp, what, extra ->
                Log.i(TAG, "$mp, $what, $extra")
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    true
                }
                false
            }
        }.start()

    }
}