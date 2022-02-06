package awesome.animals.koala.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import awesome.animals.koala.R

object ViewExtensions {

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

        dialog.show()
    }
}