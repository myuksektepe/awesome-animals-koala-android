package obidahi.books.animals.prensentation.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import obidahi.books.animals.R
import obidahi.books.animals.util.TAG
import java.io.File

abstract class BaseActivity<T : BaseViewModel, B : ViewDataBinding> : AppCompatActivity() {

    abstract val layoutRes: Int
    abstract val viewModel: T
    abstract var viewLifeCycleOwner: LifecycleOwner
    abstract fun obverseViewModel()

    private var _binding: B? = null
    val binding get() = _binding!!

    private var _networkConnection: MutableLiveData<Boolean>? = MutableLiveData()
    val networkConnection get() = _networkConnection!!

    fun initBinding() {
        _binding?.lifecycleOwner = this
        viewLifeCycleOwner = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.inflate(layoutInflater, layoutRes, null, false)
        setContentView(_binding!!.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _networkConnection?.postValue(true)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    _networkConnection?.postValue(false)
                }
            })
        }

        initBinding()
        obverseViewModel()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


    private val loadingAlertDialog by lazy {
        this.let {
            Dialog(it).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dialog_loading)
                setCancelable(false)
            }
        }
    }

    fun showLoading() = loadingAlertDialog.show()
    fun hideLoading() = loadingAlertDialog.hide()

    fun checkFileExists(file: File): Boolean {
        if (file.exists()) {
            Log.i(TAG, "File ___ Dosya zaten mevcut! ${file.absolutePath}")
            return true
        } else {
            return false
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @SuppressLint("NewApi")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

}