package net.palmut.speedometer

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class SpeedometerView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.speedometerViewStyle) :
        View(context, attrs, defStyleAttr) {

    private val borderPaint: Paint
    private val scalePaint: Paint
    private val numbersPaint: TextPaint
    private val scaleStartAngle: Float
    private val scaleSweepAngle: Float
    private val scalePoints: Int
    private val scalePadding: Float
    private var centerX = 0f
    private var centerY = 0f
    private var borderRadius = 0f
    private var scaleRadius = 0f
    private val scalePointWidth: Float
    private val scalePointStep: Int
    private val numbers: Array<Number>
    private val numberPadding: Float

    private val temp = Rect()

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.SpeedometerView, defStyleAttr, 0)

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = styledAttributes.getColor(R.styleable.SpeedometerView_borderColor, DEFAULT_BORDER_COLOR)
            strokeWidth = styledAttributes.getDimension(R.styleable.SpeedometerView_borderWidth, DEFAULT_BORDER_WIDTH)
            style = Paint.Style.STROKE
        }

        scalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = styledAttributes.getColor(R.styleable.SpeedometerView_scaleColor, DEFAULT_SCALE_COLOR)
            strokeWidth = styledAttributes.getDimension(R.styleable.SpeedometerView_scaleWidth, DEFAULT_SCALE_WIDTH)
            style = Paint.Style.STROKE
        }

        numbersPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = styledAttributes.getColor(R.styleable.SpeedometerView_numberColor, DEFAULT_SCALE_COLOR)
            style = Paint.Style.FILL_AND_STROKE
            textSize = styledAttributes.getDimension(R.styleable.SpeedometerView_numberTextSize, 0f)
        }

        val startAngle = styledAttributes.getInt(R.styleable.SpeedometerView_scaleStartAngle, DEFAULT_SCALE_START_ANGLE)
        val endAngle = styledAttributes.getInt(R.styleable.SpeedometerView_scaleEndAngle, DEFAULT_SCALE_END_ANGLE)
        scaleStartAngle = startAngle.toFloat()
        val sweepAngle = (endAngle - startAngle).toFloat()
        scaleSweepAngle = sweepAngle.takeIf { it > 0 } ?: 360 + sweepAngle

        scalePoints = styledAttributes.getInt(R.styleable.SpeedometerView_points, DEFAULT_POINTS)
        scalePointWidth = styledAttributes.getDimension(R.styleable.SpeedometerView_scalePointWidth, 0f)
        scalePointStep = styledAttributes.getInt(R.styleable.SpeedometerView_scalePointStep, 0)
        scalePadding = styledAttributes.getDimension(R.styleable.SpeedometerView_scalePadding, 0f)

        numbers = Array(scalePoints) { Number() }
        numberPadding = styledAttributes.getDimension(R.styleable.SpeedometerView_numberPadding, 0f)

        styledAttributes.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        centerX = paddingStart + (this@SpeedometerView.width - paddingStart - paddingEnd) / 2f
        centerY = paddingTop + (this@SpeedometerView.height - paddingTop - paddingBottom) / 2f
        borderRadius =
            (centerX - (paddingStart + paddingEnd) / 2).coerceAtMost(centerY - (paddingTop + paddingBottom) / 2) - borderPaint.strokeWidth

        scaleRadius = borderRadius - scalePadding

        var startRd = Math.toRadians(-scaleStartAngle.toDouble()) + PI
        val stepRd = Math.toRadians(scaleSweepAngle / (scalePoints - 1).toDouble())

        val insets = intArrayOf(numberPadding.toInt())
        for (step in 0 until scalePoints) {
            val text = (step * scalePointStep).toString()
            val desiredWidth = StaticLayout.getDesiredWidth(text, numbersPaint).toInt()
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, numbersPaint, desiredWidth + numberPadding.toInt() * 2)
                    .setIndents(insets, insets).setLineSpacing(numberPadding, 0f).build()
            layout.getLineBounds(0, temp)
            val numberPosition = scaleRadius - scalePointWidth - hypot(temp.width().toDouble() / 2, temp.height().toDouble() / 2)
            with(numbers[step]) {
                x = (centerX - numberPosition * cos(startRd) - temp.centerX()).toFloat()
                y = (centerY + numberPosition * sin(startRd) - temp.centerY()).toFloat()
                content = layout
            }
            startRd -= stepRd
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.draw {
            // border
            drawCircle(centerX, centerY, borderRadius, borderPaint)
            // TODO: scale background???

            // scale
            drawArc(
                centerX - scaleRadius,
                centerY - scaleRadius,
                centerX + scaleRadius,
                centerY + scaleRadius,
                scaleStartAngle,
                scaleSweepAngle,
                false,
                scalePaint)

            val drawScale = save()
            rotate(scaleStartAngle, centerX, centerY)
            val stepAngle = scaleSweepAngle / (scalePoints - 1)
            for (step in 0 until scalePoints) {
                drawLine(centerX + scaleRadius - scalePointWidth, centerY, centerX + scaleRadius, centerY, scalePaint)
                rotate(stepAngle, centerX, centerY)
            }
            restoreToCount(drawScale)

            for (number in numbers) {
                val save = save()
                canvas.translate(number.x, number.y)
                number.content?.draw(this)
                restoreToCount(save)
            }
        }
    }

    companion object {
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_BORDER_WIDTH = 1f
        private const val DEFAULT_SCALE_COLOR = Color.WHITE
        private const val DEFAULT_SCALE_WIDTH = 1f
        private const val DEFAULT_SCALE_START_ANGLE = 135
        private const val DEFAULT_SCALE_END_ANGLE = 45
        private const val DEFAULT_POINTS = 10
    }

    private class Number(var x: Float = 0f, var y: Float = 0f, var content: Layout? = null)
}