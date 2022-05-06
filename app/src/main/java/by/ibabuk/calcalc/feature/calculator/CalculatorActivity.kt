package by.ibabuk.calcalc.feature.calculator

import android.app.Activity
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.SpannedString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.ibabuk.calcalc.R
import by.ibabuk.calcalc.databinding.ActivityCalculatorBinding
import by.ibabuk.calcalc.databinding.AddCaloriesViewBinding
import by.ibabuk.calcalc.entity.CaloriesEntity
import by.ibabuk.calcalc.entity.EatPeriod
import by.ibabuk.calcalc.ext.animateInt
import by.ibabuk.calcalc.feature.base.BaseActivity
import by.ibabuk.calcalc.feature.enter.EnterCaloriesActivity
import by.ibabuk.calcalc.ui.chart_render.CustomChartRenderer
import by.ibabuk.calcalc.utils.Constants.ANIMATION_DURATION
import by.ibabuk.calcalc.utils.Constants.MINUTES_PER_DAY
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CalculatorActivity : BaseActivity() {

    private lateinit var binding: ActivityCalculatorBinding

    private val viewModel: CalculatorViewModel by viewModel()

    private var previousEatCalories = 0
    private var previousTotalCalories = 0

    private var caloriesBinding: AddCaloriesViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpChart()
        setUpCaloriesView()
        observeViewModel()
        viewModel.initData()
    }

    private fun setUpChart() {
        binding.chart.description.isEnabled = false
        binding.chart.legend.isEnabled = false
        binding.chart.axisLeft.isEnabled = false
        binding.chart.axisLeft.setDrawGridLines(false)
        binding.chart.axisRight.isEnabled = false
        binding.chart.axisRight.setDrawGridLines(false)
        binding.chart.xAxis.isEnabled = false
        binding.chart.xAxis.setDrawGridLines(false)
        binding.chart.setDrawBorders(false)
        binding.chart.setDrawGridBackground(false)
        binding.chart.isDoubleTapToZoomEnabled = false
        binding.chart.setScaleEnabled(false)
        binding.chart.setPinchZoom(false)
        binding.chart.xAxis.axisMinimum = 0f
        binding.chart.xAxis.axisMaximum = MINUTES_PER_DAY.toFloat()
        binding.chart.xAxis.setDrawAxisLine(true)
        binding.chart.axisLeft.axisMinimum = 0f
        binding.chart.setTouchEnabled(false)
    }

    private fun setUpCaloriesView() {
        binding.breakfast.parent.setOnClickListener {
            highlightCaloriesView(EatPeriod.BREAKFAST, binding.breakfast)
        }
        binding.lunch.parent.setOnClickListener {
            highlightCaloriesView(EatPeriod.LUNCH, binding.lunch)
        }
        binding.dinner.parent.setOnClickListener {
            highlightCaloriesView(EatPeriod.DINNER, binding.dinner)
        }
    }

    private fun highlightCaloriesView(
        timePeriod: EatPeriod,
        binding: AddCaloriesViewBinding
    ) {
        this.caloriesBinding = binding
        startAnimationForCaloriesView(binding)
        binding.parent.elevation = 6f
        binding.parent.animate().translationZ(6f).setDuration(ANIMATION_DURATION.toLong()).start()

        viewModel.getCalories(timePeriod)
    }

    private fun unhighlightCaloriesView() {
        val transition = caloriesBinding?.parent?.background as? TransitionDrawable
        transition?.reverseTransition(ANIMATION_DURATION)
        caloriesBinding?.parent?.elevation = 0f
        caloriesBinding?.parent?.animate()?.translationZ(0f)
            ?.setDuration(ANIMATION_DURATION.toLong())?.start()
        caloriesBinding = null
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getEatingData()
                        .collect(this@CalculatorActivity::handleEatingData)
                }
                launch {
                    viewModel.getTotalData()
                        .collect(this@CalculatorActivity::handleTotalData)
                }
                launch {
                    viewModel.getCaloriesData()
                        .collect(this@CalculatorActivity::handleCaloriesData)
                }
                launch {
                    viewModel.getChartData()
                        .collect(this@CalculatorActivity::handleChartData)
                }
                launch {
                    viewModel.getEditCaloriesData()
                        .collect(this@CalculatorActivity::showEnterCaloriesScreen)
                }
            }
        }
    }

    private fun handleEatingData(eatingData: Int) {
        if (eatingData != previousEatCalories) {
            animateInt(previousEatCalories, eatingData) {
                setEatingData(it)
            }
            previousEatCalories = eatingData
        } else {
            setEatingData(eatingData)
        }
    }

    private fun setEatingData(eatingData: Int) {
        binding.eating.text = getString(R.string.format_kcal, eatingData)
    }

    private fun handleTotalData(totalData: Int) {
        if (totalData != previousTotalCalories) {
            animateInt(previousTotalCalories, totalData) {
                setTotalData(it)
            }
            previousTotalCalories = totalData
        } else {
            setTotalData(totalData)
        }
    }

    private fun setTotalData(totalData: Int) {
        binding.totalCalories.text = totalData.toString()
    }

    private fun handleCaloriesData(calories: List<CaloriesEntity>) {
        calories.forEach {
            when (it.period) {
                EatPeriod.BREAKFAST -> setUpPeriodEat(binding.breakfast, it)
                EatPeriod.LUNCH -> setUpPeriodEat(binding.lunch, it)
                EatPeriod.DINNER -> setUpPeriodEat(binding.dinner, it)
            }
        }
    }

    private fun handleChartData(calories: List<Entry>) {
        val dataSet = LineDataSet(calories, "")
        dataSet.color = ContextCompat.getColor(this, R.color.chart_line_color)
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.black))
        dataSet.circleRadius = 1.66f
        dataSet.lineWidth = 3f
        dataSet.circleHoleColor = ContextCompat.getColor(this, R.color.black)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.chart_value_color)
        dataSet.valueTextSize = 12f
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val lineData = LineData(dataSet)

        binding.chart.renderer = CustomChartRenderer(
            binding.chart,
            binding.chart.animator,
            binding.chart.viewPortHandler
        )
        binding.chart.data = lineData
        binding.chart.notifyDataSetChanged()
        binding.chart.animateXY(ANIMATION_DURATION, ANIMATION_DURATION)
        binding.chart.invalidate()
    }

    private fun setUpPeriodEat(binding: AddCaloriesViewBinding, caloriesEntity: CaloriesEntity) {
        binding.time.text = caloriesEntity.getTime()
        binding.kcal.text = getCalories(caloriesEntity.calories)
        binding.dayPeriod.setText(caloriesEntity.period.title)
        if (caloriesEntity.time != null) {
            val state = binding.parent.tag as? Boolean
            if (state != true) {
                startAnimationForCaloriesView(binding)
            }
        } else {
            val transition = binding.parent.background as TransitionDrawable
            transition.resetTransition()
        }
    }

    private fun startAnimationForCaloriesView(binding: AddCaloriesViewBinding) {
        val transition = binding.parent.background as TransitionDrawable
        transition.startTransition(ANIMATION_DURATION)
        binding.parent.tag = true
    }

    private fun getCalories(calories: Int): SpannedString {
        return buildSpannedString {
            bold {
                append(calories.toString())
                append("\n")
            }
            scale(0.75f) {
                append(getString(R.string.kcal))
            }
        }
    }

    private fun showEnterCaloriesScreen(data: Pair<EatPeriod, Int>) {
        enterContract.launch(EnterCaloriesActivity.intent(this, data.first, data.second))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private val enterContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val calories = EnterCaloriesActivity.getCalories(it.data)
                val time = EnterCaloriesActivity.getTime(it.data)
                val timePeriod = EnterCaloriesActivity.getEatPeriod(it.data)
                if (time != null && timePeriod != null) {
                    viewModel.setCalories(timePeriod, calories, time)
                } else {
                    showSnackbarMessage(R.string.general_error)
                }
            } else {
                unhighlightCaloriesView()
            }
        }
}