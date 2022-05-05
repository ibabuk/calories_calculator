package by.ibabuk.calcalc.di

import by.ibabuk.calcalc.feature.calculator.CalculatorViewModel
import by.ibabuk.calcalc.feature.enter.EnterCaloriesViewModel
import by.ibabuk.calcalc.interactor.CaloriesInteractor
import by.ibabuk.calcalc.interactor.mapper.CaloriesMapper
import by.ibabuk.calcalc.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
val calcModules = module {
    single { CaloriesMapper() }
    single { DataRepository(Dispatchers.IO) }
    single { CaloriesInteractor(dataRepository = get(), mapper = get()) }
    viewModel { CalculatorViewModel(caloriesInteractor = get()) }
    viewModel { EnterCaloriesViewModel() }
}