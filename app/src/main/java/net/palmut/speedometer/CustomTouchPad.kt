package net.palmut.speedometer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2

class CustomTouchPad
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    private var lastValue: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val viewPager = getChildAt(0) as? ViewPager2 ?: return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastValue = event.x
                viewPager.beginFakeDrag()
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) viewPager.fakeDragBy(event.x - lastValue)
                lastValue = event.x
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                viewPager.endFakeDrag()
            }
        }
        return true
    }

}