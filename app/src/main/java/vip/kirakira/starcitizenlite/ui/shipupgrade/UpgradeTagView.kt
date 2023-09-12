package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet

class UpgradeTagView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private val paint = Paint()
    private var lineRadius = 20f

    init {
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = RectF(2f, 2f, width.toFloat()-2, height.toFloat()-2)
        canvas.drawRoundRect(rect, lineRadius, lineRadius, paint) // 设置圆弧角的半径
    }

    fun setColor(color: Int) {
        setTextColor(color)
        paint.color = color
    }

    fun setRadius(radius: Float) {
        lineRadius = radius
    }

}