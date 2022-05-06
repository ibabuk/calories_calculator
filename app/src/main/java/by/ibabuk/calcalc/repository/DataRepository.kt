package by.ibabuk.calcalc.repository

import by.ibabuk.calcalc.entity.CaloriesEntity
import by.ibabuk.calcalc.entity.EatPeriod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
private const val BURN = 300

class DataRepository(private val dispatcher: CoroutineDispatcher) {

    private val calories = ArrayList<CaloriesEntity>()

    init {
        reset()
    }

    private fun reset() {
        calories.clear()
        calories.add(CaloriesEntity.breakfast())
        calories.add(CaloriesEntity.lunch())
        calories.add(CaloriesEntity.dinner())
    }

    suspend fun getEatingCalories(): Flow<Int> {
        return flow {
            val sumCalories = this@DataRepository.calories.sumOf { it.calories }
            emit(sumCalories)
        }
            .flowOn(dispatcher)
    }

    suspend fun getTotalCalories(): Flow<Int> {
        return flow {
            var sumCalories = this@DataRepository.calories.sumOf { it.calories }
            sumCalories -= BURN
            if (sumCalories < 0) {
                sumCalories = 0
            }
            emit(sumCalories)
        }
            .flowOn(dispatcher)
    }

    suspend fun getCalories(): Flow<List<CaloriesEntity>> {
        return flow {
            emit(calories)
        }
            .flowOn(dispatcher)
    }

    suspend fun setCalories(
        timePeriod: EatPeriod,
        calories: Int,
        time: Date
    ): Flow<List<CaloriesEntity>> {
        return flow {
            //check current date
            checkDataCalories()

            val index = this@DataRepository.calories.indexOfFirst { it.period == timePeriod }
            if (index != -1) {
                this@DataRepository.calories[index] = this@DataRepository.calories[index].copy(
                    calories = calories,
                    time = time
                )
            }

            emit(this@DataRepository.calories)
        }
            .flowOn(dispatcher)
    }

    suspend fun getPeriodCalories(timePeriod: EatPeriod): Flow<Int> {
        return flow {
            val index = this@DataRepository.calories.indexOfFirst { it.period == timePeriod }
            if (index != -1) {
                emit(this@DataRepository.calories[index].calories)
            } else {
                emit(0)
            }
        }
            .flowOn(dispatcher)
    }

    suspend fun checkDataCalories(date: Date? = null) = coroutineScope {
        val calendar = if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar
        } else {
            Calendar.getInstance()
        }
        val findCalories = this@DataRepository.calories.find { it.time != null }
        findCalories?.let {
            val caloriesCalendar = Calendar.getInstance()
            caloriesCalendar.time = it.time!!
            val calorieDay = caloriesCalendar.get(Calendar.DAY_OF_YEAR)
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            if (calorieDay != currentDay) {
                reset()
            }
        }
    }
}