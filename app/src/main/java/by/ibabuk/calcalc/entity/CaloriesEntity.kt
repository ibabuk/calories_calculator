package by.ibabuk.calcalc.entity

import androidx.annotation.StringRes
import by.ibabuk.calcalc.R
import by.ibabuk.calcalc.ext.formatTime
import by.ibabuk.calcalc.utils.Constants
import java.util.*

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
data class CaloriesEntity(
    val calories: Int,
    val time: Date? = null,
    val timePeriod: String,
    val period: EatPeriod
) {

    fun getMinutes(): Int {
        return if (time == null) {
            period.initialTime
        } else {
            val calendar = Calendar.getInstance()
            calendar.time = time
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            hours * 60
        }
    }

    fun getTime(): String {
        return time?.formatTime() ?: "${time.formatTime()} $timePeriod"
    }

    companion object {
        fun breakfast(): CaloriesEntity {
            return CaloriesEntity(
                calories = 0,
                timePeriod = "am",
                period = EatPeriod.BREAKFAST
            )
        }

        fun lunch(): CaloriesEntity {
            return CaloriesEntity(
                calories = 0,
                timePeriod = "pm",
                period = EatPeriod.LUNCH
            )
        }

        fun dinner(): CaloriesEntity {
            return CaloriesEntity(
                calories = 0,
                timePeriod = "pm",
                period = EatPeriod.DINNER
            )
        }
    }
}


enum class EatPeriod(
    @StringRes val title: Int,
    val initialTime: Int
) {
    BREAKFAST(R.string.breakfast, initialTime = Constants.MINUTES_PER_DAY / 10),
    LUNCH(R.string.lunch, initialTime = Constants.MINUTES_PER_DAY / 2),
    DINNER(R.string.dinner, initialTime = (Constants.MINUTES_PER_DAY / 1.1).toInt())
}
