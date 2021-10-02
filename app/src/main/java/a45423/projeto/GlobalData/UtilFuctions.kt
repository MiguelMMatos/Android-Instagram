package a45423.projeto.GlobalData

import a45423.projeto.R
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat

class UtilFuctions {
    fun disableButton(button : Button, context : Context){
        button?.isEnabled = false
        button?.isClickable = false
        button?.setBackgroundColor(ContextCompat.getColor(context, R.color.browser_actions_bg_grey))
    }

    fun enableButton(button : Button, context : Context){
        button?.isEnabled = true
        button?.isClickable = true
        button?.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))
    }

    fun getFileExtension(uri : Uri, context : Context): String? {
        val cR = context?.contentResolver
        val mime = MimeTypeMap.getSingleton()

        if (cR != null) {
            return mime.getExtensionFromMimeType(cR.getType(uri))
        }

        return ""
    }

}