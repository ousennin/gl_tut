package anonymous.gl.utils.geometry

import android.graphics.Point

class Circle(val center: Point, val radius: Float) {
    fun scale(scale: Float): Circle {
        return Circle(center, radius * scale)
    }
}