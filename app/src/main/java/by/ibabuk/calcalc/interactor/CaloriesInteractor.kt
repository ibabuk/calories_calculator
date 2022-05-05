package by.ibabuk.calcalc.interactor

import by.ibabuk.calcalc.entity.CaloriesEntity
import by.ibabuk.calcalc.entity.EatPeriod
import by.ibabuk.calcalc.interactor.mapper.CaloriesMapper
import by.ibabuk.calcalc.repository.DataRepository
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import java.util.*

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CaloriesInteractor(
    private val dataRepository: DataRepository,
    private val mapper: CaloriesMapper
) {

    suspend fun getEatingCalories(): Flow<Int> {
        return dataRepository
            .getEatingCalories()
    }

    suspend fun getTotalCalories(): Flow<Int> {
        return dataRepository
            .getTotalCalories()
    }

    suspend fun getCalories(): Flow<CaloriesWithChart> {
        return dataRepository
            .getCalories()
            .map {
                val dataSet = mapper.mapCaloriesForChat(it)
                return@map CaloriesWithChart(it, dataSet)
            }
    }

    suspend fun getPeriodCalories(timePeriod: EatPeriod): Flow<Int> {
        return dataRepository
            .getPeriodCalories(timePeriod)
    }

    suspend fun setCalories(
        timePeriod: EatPeriod,
        calories: Int,
        time: Date
    ): Flow<Triple<CaloriesWithChart, Int, Int>> {
        return dataRepository
            .setCalories(timePeriod, calories, time)
            .map {
                val dataSet = mapper.mapCaloriesForChat(it)
                return@map CaloriesWithChart(it, dataSet)
            }
            .zip(dataRepository.getEatingCalories()) { f1, f2 ->
                Pair(f1, f2)
            }
            .zip(dataRepository.getTotalCalories()) { f1, f2 ->
                Triple(f1.first, f1.second, f2)
            }
    }
}

data class CaloriesWithChart(
    val calories: List<CaloriesEntity>,
    val dataSet: List<Entry>
)