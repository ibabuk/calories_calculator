package by.ibabuk.calcalc.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.ibabuk.calcalc.entity.CaloriesEntity
import by.ibabuk.calcalc.entity.EatPeriod
import by.ibabuk.calcalc.interactor.CaloriesInteractor
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CalculatorViewModel(private val caloriesInteractor: CaloriesInteractor) : ViewModel() {


    private val eatingData = MutableSharedFlow<Int>(replay = 1)
    private val totalData = MutableSharedFlow<Int>(replay = 1)
    private val caloriesData = MutableSharedFlow<List<CaloriesEntity>>(replay = 1)
    private val chartData = MutableSharedFlow<List<Entry>>(replay = 1)
    private val editCaloriesData = MutableSharedFlow<Pair<EatPeriod, Int>>()


    fun getEatingData() = eatingData.asSharedFlow()
    fun getTotalData() = totalData.asSharedFlow()
    fun getCaloriesData() = caloriesData.asSharedFlow()
    fun getChartData() = chartData.asSharedFlow()
    fun getEditCaloriesData() = editCaloriesData.asSharedFlow()

    fun initData() {
        runBlocking {
            val job = viewModelScope.launch {
                caloriesInteractor
                    .checkDateCalories()
            }

            job.join()
            viewModelScope.launch {
                caloriesInteractor
                    .getEatingCalories()
                    .onEach { eatingData.emit(it) }
                    .launchIn(viewModelScope)

                caloriesInteractor
                    .getTotalCalories()
                    .onEach { totalData.emit(it) }
                    .launchIn(viewModelScope)

                caloriesInteractor
                    .getCalories()
                    .onEach {
                        caloriesData.emit(it.calories)
                        chartData.emit(it.dataSet)
                    }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun setCalories(timePeriod: EatPeriod, calories: Int, time: Date) {
        viewModelScope.launch {
            caloriesInteractor
                .setCalories(timePeriod, calories, time)
                .onEach {
                    caloriesData.emit(it.first.calories)
                    chartData.emit(it.first.dataSet)
                    eatingData.emit(it.second)
                    totalData.emit(it.third)
                }
                .launchIn(viewModelScope)


        }

    }

    fun getCalories(timePeriod: EatPeriod) {
        viewModelScope.launch {
            caloriesInteractor
                .getPeriodCalories(timePeriod)
                .onEach { editCaloriesData.emit(it) }
                .launchIn(viewModelScope)
        }
    }
}