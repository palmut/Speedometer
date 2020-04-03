package net.palmut.speedometer

import android.graphics.Canvas

inline fun Canvas.saveDraw(block: Canvas.() -> Unit) {
    val checkpoint = save()
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}