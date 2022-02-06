package awesome.animals.koala.prensentation.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import awesome.animals.koala.R
import awesome.animals.koala.databinding.ActivityMainBinding
import awesome.animals.koala.prensentation.base.BaseActivity
import awesome.animals.koala.prensentation.viewmodel.MainActivityViewModel
import awesome.animals.koala.util.ViewExtensions.showCustomDialog

class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainActivityViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    override fun obverseViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.showCustomDialog(
            title = "Selam",
            message = "Koalaları Tanımaya Hazır Mısın?",
            positiveButtonText = "Evet",
            negativeButtonText = "Belki Sonra",
            positiveButtonCallback = {null},
            negativeButtonCallback = {},
            cancelable = false
        )
    }
}