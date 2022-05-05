package by.ibabuk.calcalc.ext

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
private val format = SimpleDateFormat("hh:mm aa", Locale.US)

fun Date?.formatTime(): String {
    return this?.let { format.format(it) } ?: "0:0"
}