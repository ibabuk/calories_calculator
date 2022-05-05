package by.ibabuk.calcalc.feature.base

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import by.ibabuk.calcalc.R
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Artem Babuk on 5.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
open class BaseActivity : AppCompatActivity() {

    protected fun showSnackbarMessage(@StringRes stringRes: Int) {
        showSnackbarMessage(getString(stringRes))
    }

    private fun showSnackbarMessage(message: String?): Snackbar {
        return if (message == null) {
            snackbar("", 0)
        } else showSnackbarMessage(message, Snackbar.LENGTH_SHORT)
    }

    private fun showSnackbarMessage(message: String, duration: Int): Snackbar {
        val snackbar = snackbar(message, duration)
        snackbar.show()
        return snackbar
    }

    private fun snackbar(message: String, duration: Int): Snackbar {
        val coordinatorSnackBar = findViewById<View>(R.id.snackbarContent)
        val snackbar = Snackbar.make(
            coordinatorSnackBar
                ?: findViewById(android.R.id.content), message, duration
        )
        val textView =
            snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 4
        return snackbar
    }
}