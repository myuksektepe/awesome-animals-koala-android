package obidahi.books.animals.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import obidahi.books.animals.R


/**
 * Created by Murat Yüksektepe on 6.09.2022.
 * muratyuksektepe.com
 * yuksektepemurat@gmail.com
 */
class CustomDialog {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var instance: CustomDialog

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context
        private lateinit var dialog: Dialog

        fun getInstance(context: Context): CustomDialog {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = CustomDialog()
                }
            }
            this.context = context
            dialog = Dialog(context, R.style.CustomDialog)
            dialog.apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.custom_dialog)
            }
            return instance
        }
    }

    fun hide() {
        if (dialog.isShowing) dialog.dismiss()
    }

    fun show(
        title: String,
        message: String,
        positiveButtonText: String?,
        negativeButtonText: String?,
        positiveButtonCallback: () -> (Unit)?,
        negativeButtonCallback: () -> (Unit)?,
        cancelable: Boolean = false
    ) {
        hide()

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
            dialog.dismiss()
            positiveButtonCallback()
        }

        dialogNegativeButton.setOnClickListener {
            dialog.dismiss()
            negativeButtonCallback()
        }
        dialog.run {
            setCancelable(cancelable)
        }
        if (!dialog.isShowing) dialog.show()
    }

}