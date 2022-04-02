package obidahi.books.animals.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import obidahi.books.animals.R

object ViewExtensions {

    fun Context.animSlideInDown() = AnimationUtils.loadAnimation(this, R.anim.slide_in_down)
    fun Context.animSlideInLeft() = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left)
    fun Context.animSlideInUp() = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
    fun Context.animSlideOutDown() = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
    fun Context.animFadeOut() = AnimationUtils.loadAnimation(this, R.anim.fade_out)
    fun Context.animFadeIn() = AnimationUtils.loadAnimation(this, R.anim.fade_in)
    fun Context.animBounce() = AnimationUtils.loadAnimation(this, R.anim.bounce)

    fun Activity.showCustomDialog(
        title: String,
        message: String,
        positiveButtonText: String?,
        negativeButtonText: String?,
        positiveButtonCallback: () -> (Unit)?,
        negativeButtonCallback: () -> (Unit)?,
        cancelable: Boolean = false
    ) {
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(cancelable)
            setContentView(R.layout.custom_dialog)
        }

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        val dialogPossitiveButton = dialog.findViewById<Button>(R.id.dialogPossitiveButton)
        val dialogNegativeButton = dialog.findViewById<Button>(R.id.dialogNegativeButton)

        dialogTitle.text = title
        dialogMessage.text = message

        if (!positiveButtonText.isNullOrBlank()) {
            dialogPossitiveButton.visibility = View.VISIBLE
            dialogPossitiveButton.text = positiveButtonText
        } else {
            dialogPossitiveButton.visibility = View.GONE
        }

        if (!negativeButtonText.isNullOrBlank()) {
            dialogNegativeButton.visibility = View.VISIBLE
            dialogNegativeButton.text = negativeButtonText
        } else {
            dialogNegativeButton.visibility = View.GONE
        }

        dialogPossitiveButton.setOnClickListener {
            positiveButtonCallback() ?: run {
                dialog.dismiss()
            }
        }

        dialogNegativeButton.setOnClickListener {
            negativeButtonCallback() ?: run {
                dialog.dismiss()
            }
        }

        if (!dialog.isShowing) dialog.show()
    }


    fun ViewPager2.nextPage(smoothScroll: Boolean = true): Boolean {
        if ((currentItem + 1) < adapter?.itemCount ?: 0) {
            this.post {
                //Timer().schedule(1000) { }
                setCurrentItem(currentItem + 1, smoothScroll)
            }
            return true
        }
        //can't move to next page, maybe current page is last or adapter not set.
        return false
    }

    fun ViewPager2.previousPage(smoothScroll: Boolean = true): Boolean {
        if ((currentItem - 1) >= 0) {
            //Timer().schedule(1000) {}
            this.post {
                setCurrentItem(currentItem - 1, smoothScroll)
            }
            return true
        }
        //can't move to previous page, maybe current page is first or adapter not set.
        return false
    }
}