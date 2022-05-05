package by.ibabuk.calcalc.interactor.mapper

import by.ibabuk.calcalc.entity.CaloriesEntity
import by.ibabuk.calcalc.utils.Constants
import com.github.mikephil.charting.data.Entry

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CaloriesMapper {

    fun mapCaloriesForChat(calories: List<CaloriesEntity>): List<Entry> {
        val list = calories.map {
            Entry(it.getMinutes().toFloat(), it.calories.toFloat())
        }
        val setList = ArrayList<Entry>()
        for (item in list) {
            if (setList.isEmpty()) {
                setList.add(item)
                continue
            }
            val index = setList.indexOfFirst { it.x == item.x }
            if (index != -1) {
                setList[index].y += item.y
            } else {
                setList.add(item)
            }
        }

        if (setList.first().x > 0) {
            setList.add(0, Entry(0f, 0f))
        }
        if (setList.last().x < Constants.MINUTES_PER_DAY) {
            setList.add(Entry(Constants.MINUTES_PER_DAY.toFloat(), 0f))
        }
        return setList
    }
}