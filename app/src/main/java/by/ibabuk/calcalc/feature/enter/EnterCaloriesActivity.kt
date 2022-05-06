package by.ibabuk.calcalc.feature.enter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import app.futured.hauler.setOnDragDismissedListener
import by.ibabuk.calcalc.R
import by.ibabuk.calcalc.databinding.ActivityEnterCaloriesBinding
import by.ibabuk.calcalc.entity.EatPeriod
import by.ibabuk.calcalc.ext.closeScreen
import by.ibabuk.calcalc.ext.slideInView
import by.ibabuk.calcalc.feature.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by Artem Babuk on 5.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class EnterCaloriesActivity : BaseActivity() {

    companion object {
        private const val CALORIES = "calories"
        private const val EAT_PERIOD = "eat_period"
        private const val TIME = "time"

        fun getCalories(data: Intent?): Int {
            return data?.getIntExtra(CALORIES, 0) ?: 0
        }

        fun getTime(data: Intent?): Date? {
            return data?.getSerializableExtra(TIME) as? Date
        }

        fun getEatPeriod(data: Intent?): EatPeriod? {
            return data?.getSerializableExtra(EAT_PERIOD) as? EatPeriod
        }

        fun intent(
            context: Context,
            eatPeriod: EatPeriod,
            calories: Int
        ): Intent {
            return Intent(context, EnterCaloriesActivity::class.java).apply {
                putExtra(CALORIES, calories)
                putExtra(EAT_PERIOD, eatPeriod)
            }
        }
    }

    private val viewModel: EnterCaloriesViewModel by viewModel()

    private lateinit var binding: ActivityEnterCaloriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterCaloriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBinding()
    }

    private fun setUpBinding() {
        binding.dragLayout.setOnClickListener {
            closeScreen()
        }
        binding.confirm.setOnClickListener {
            handleConfirm()
        }
        binding.cancel.setOnClickListener {
            onBackPressed()
        }
        binding.dragLayout.setOnDragDismissedListener {
            closeScreen()
        }
        window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.black_60
                )
            )
        )
        binding.calories.setText(getCalories().toString())
        showScreen()
    }

    private fun getCalories(): Int {
        return intent.getIntExtra(CALORIES, 0)
    }

    private fun showScreen() {
        binding.dragLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.dragLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                slideInView(binding.snackbarContent).start()
            }
        })
    }

    override fun onBackPressed() {
        closeScreen {
            setResult(Activity.RESULT_CANCELED)
        }
    }

    private fun handleConfirm() {
        if (binding.calories.text.isEmpty()) {
            showSnackbarMessage(R.string.enter_calories_error)
        }
        val caloriesStr = binding.calories.text.toString()
        try {
            val calories = caloriesStr.toInt()
            val date = Calendar.getInstance().time
            closeScreen {
                val intent = Intent().apply {
                    putExtra(CALORIES, calories)
                    putExtra(TIME, date)
                    putExtra(EAT_PERIOD, getEatPeriod(intent))
                }
                setResult(Activity.RESULT_OK, intent)
            }
        } catch (e: Exception) {
            showSnackbarMessage(R.string.wrong_calories_error)
        }
    }
}