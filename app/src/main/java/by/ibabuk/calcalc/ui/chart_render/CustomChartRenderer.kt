package by.ibabuk.calcalc.ui.chart_render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * Created by Artem Babuk on 4.05.22
 * Skype: archiecrown
 * Telegram: @iBabuk
 */
class CustomChartRenderer(
    chart: LineDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val mCirclesBuffer = FloatArray(2)

    private val mImageCaches = HashMap<IDataSet<*>, DataSetImageCache>()

    override fun drawValues(c: Canvas?) {
        if (isDrawingValuesAllowed(mChart)) {
            val dataSets = mChart.lineData.dataSets

            for (i in dataSets.indices) {
                val dataSet = dataSets[i]

                if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) continue

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)

                val trans = mChart.getTransformer(dataSet.axisDependency)

                // make sure the values do not interfear with the circles
                var valOffset = (dataSet.circleRadius * 10).toInt()

                if (!dataSet.isDrawCirclesEnabled)
                    valOffset /= 2

                mXBounds.set(mChart, dataSet)

                val positions = trans.generateTransformedValuesLine(
                    dataSet,
                    mAnimator.phaseX,
                    mAnimator.phaseY,
                    mXBounds.min,
                    mXBounds.max
                )
                val formatter = dataSet.valueFormatter

                val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

                for (j in positions.indices step 2) {
                    val x = positions[j]
                    val y = positions[j + 1]

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue

                    val entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min)

                    if (dataSet.isDrawValuesEnabled) {
                        drawValue(
                            c,
                            formatter.getPointLabel(entry),
                            x,
                            y - valOffset,
                            dataSet.getValueTextColor(j / 2),
                            dataSet.getEntryForIndex(j / 2),
                            dataSet.xMax
                        )
                    }

                    if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                        val icon = entry.icon

                        Utils.drawImage(
                            c,
                            icon,
                            (x + iconsOffset.x).toInt(),
                            (y + iconsOffset.y).toInt(),
                            icon.intrinsicWidth,
                            icon.intrinsicHeight
                        )
                    }
                }

                MPPointF.recycleInstance(iconsOffset)
            }
        }
    }

    private fun drawValue(
        c: Canvas?,
        valueText: String?,
        x: Float,
        y: Float,
        color: Int,
        entry: Entry,
        xMax: Float
    ) {
        mValuePaint.color = color
        if (entry.x == 0f && entry.y == 0f) return
        if (entry.x == xMax && entry.y == 0f) return

        c?.drawText(valueText ?: "", x, y, mValuePaint)
    }

    override fun drawCircles(c: Canvas?) {
        mRenderPaint.style = Paint.Style.FILL

        val phaseY = mAnimator.phaseY

        mCirclesBuffer[0] = 0f
        mCirclesBuffer[1] = 0f

        val dataSets = mChart.lineData.dataSets

        for (i in dataSets.indices) {
            val dataSet = dataSets[i]

            if (!dataSet.isVisible || !dataSet.isDrawCirclesEnabled ||
                dataSet.entryCount == 0
            ) {
                continue
            }

            mCirclePaintInner.color = dataSet.circleHoleColor

            val trans = mChart.getTransformer(dataSet.axisDependency)

            mXBounds.set(mChart, dataSet)

            val circleRadius = dataSet.circleRadius
            val circleHoleRadius = dataSet.circleHoleRadius
            val drawCircleHole = dataSet.isDrawCircleHoleEnabled &&
                    circleHoleRadius < circleRadius &&
                    circleHoleRadius > 0f
            val drawTransparentCircleHole = drawCircleHole &&
                    dataSet.circleHoleColor == ColorTemplate.COLOR_NONE

            val imageCache: DataSetImageCache?

            if (mImageCaches.containsKey(dataSet)) {
                imageCache = mImageCaches[dataSet]
            } else {
                imageCache = DataSetImageCache()
                mImageCaches[dataSet] = imageCache
            }

            val changeRequired = imageCache?.init(dataSet) ?: false

            // only fill the cache with new bitmaps if a change is required
            if (changeRequired) {
                imageCache?.fill(dataSet, drawCircleHole, drawTransparentCircleHole)
            }

            val boundsRangeCount = mXBounds.range + mXBounds.min

            for (j in mXBounds.min..boundsRangeCount) {
                val e = dataSet.getEntryForIndex(j) ?: break
                if (e.y == 0f && e.x == 0f) continue
                if (j == boundsRangeCount && e.y == 0f) continue

                mCirclesBuffer[0] = e.x
                mCirclesBuffer[1] = e.y * phaseY

                trans.pointValuesToPixel(mCirclesBuffer)

                if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0])) break

                if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                    !mViewPortHandler.isInBoundsY(mCirclesBuffer[1])
                ) continue

                val circleBitmap = imageCache?.getBitmap(j)

                circleBitmap?.let {
                    c?.drawBitmap(
                        it,
                        mCirclesBuffer[0] - circleRadius,
                        mCirclesBuffer[1] - circleRadius,
                        null
                    )
                }
            }
        }
    }

    inner class DataSetImageCache {
        private val mCirclePathBuffer = Path()
        private var circleBitmaps: Array<Bitmap?>? = null

        /**
         * Sets up the cache, returns true if a change of cache was required.
         *
         * @param set
         * @return
         */
        internal fun init(set: ILineDataSet): Boolean {
            val size = set.circleColorCount
            var changeRequired = false
            if (circleBitmaps == null) {
                circleBitmaps = arrayOfNulls(size)
                changeRequired = true
            } else if (circleBitmaps?.size != size) {
                circleBitmaps = arrayOfNulls(size)
                changeRequired = true
            }
            return changeRequired
        }

        /**
         * Fills the cache with bitmaps for the given dataset.
         *
         * @param set
         * @param drawCircleHole
         * @param drawTransparentCircleHole
         */
        internal fun fill(
            set: ILineDataSet,
            drawCircleHole: Boolean,
            drawTransparentCircleHole: Boolean
        ) {
            val colorCount = set.circleColorCount
            val circleRadius = set.circleRadius
            val circleHoleRadius = set.circleHoleRadius
            for (i in 0 until colorCount) {
                val conf = Bitmap.Config.ARGB_4444
                val circleBitmap = Bitmap.createBitmap(
                    (circleRadius * 2.1).toInt(),
                    (circleRadius * 2.1).toInt(), conf
                )
                val canvas = Canvas(circleBitmap)
                circleBitmaps?.set(i, circleBitmap)
                mRenderPaint.color = set.getCircleColor(i)
                if (drawTransparentCircleHole) {
                    // Begin path for circle with hole
                    mCirclePathBuffer.reset()
                    mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        Path.Direction.CW
                    )

                    // Cut hole in path
                    mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleHoleRadius,
                        Path.Direction.CCW
                    )

                    // Fill in-between
                    canvas.drawPath(mCirclePathBuffer, mRenderPaint)
                } else {
                    canvas.drawCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        mRenderPaint
                    )
                    if (drawCircleHole) {
                        canvas.drawCircle(
                            circleRadius,
                            circleRadius,
                            circleHoleRadius,
                            mCirclePaintInner
                        )
                    }
                }
            }
        }

        /**
         * Returns the cached Bitmap at the given index.
         *
         * @param index
         * @return
         */
        internal fun getBitmap(index: Int): Bitmap? {
            return circleBitmaps?.get(index % (circleBitmaps?.size ?: 0))
        }
    }
}