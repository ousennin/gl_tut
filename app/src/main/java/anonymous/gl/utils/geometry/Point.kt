package anonymous.gl.utils.geometry

class Point(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    fun translateY(distance: Float): Point {
        return Point(x, y + distance, z)
    }
}