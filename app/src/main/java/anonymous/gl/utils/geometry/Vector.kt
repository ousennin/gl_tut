package anonymous.gl.utils.geometry

import kotlin.math.sqrt

class Vector(
  val x: Float,
  val y: Float,
  val z: Float,
) {
  val length: Float
    get() = sqrt(
      x * x + y * y + z * z
    )

  fun crossProduct(other: Vector): Vector {
    return Vector(
      (y * other.z) - (z * other.y),
      (z * other.x) - (x * other.z),
      (x * other.y) - (y * other.x)
    )
  }

  fun dotProduct(other: Vector): Float {
    return x * other.x + y * other.y + z * other.z
  }

  fun scale(f: Float): Vector {
    return Vector(
      x * f,
      y * f,
      z * f,
    )
  }

  fun normalize(): Vector {
    return scale(1f / length)
  }
}