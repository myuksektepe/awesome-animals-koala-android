package obidahi.books.animals.prensentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<T : BaseViewModel, B : ViewDataBinding> : Fragment() {

    abstract val layoutRes: Int
    abstract val viewModel: T
    private var _binding: B? = null
    val binding get() = _binding!!
    abstract fun observeViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this._binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        observeViewModel()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}