package by.ibabuk.calcalc

import android.app.Application
import by.ibabuk.calcalc.di.calcModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CalcApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setUpKoin()
    }

    private fun setUpKoin() {
        startKoin {
            androidContext(this@CalcApplication)
            modules(calcModules)
        }
    }
}