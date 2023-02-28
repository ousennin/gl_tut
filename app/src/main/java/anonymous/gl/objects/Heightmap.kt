package anonymous.gl.objects

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glDrawElements
import anonymous.gl.Constants.BYTES_PER_FLOAT
import anonymous.gl.data.IndexBuffer
import anonymous.gl.data.VertexBuffer
import anonymous.gl.programs.HeightmapShaderProgram
import anonymous.gl.utils.geometry.Geometry
import anonymous.gl.utils.geometry.Point
import kotlin.math.max
import kotlin.math.min

class Heightmap(bitmap: Bitmap) {
  companion object {
    const val POSITION_COMPONENT_COUNT = 3
    const val NORMAL_COMPONENT_COUNT = 3
    const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT
    const val STRIDE = (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT) * BYTES_PER_FLOAT
  }

  private val width: Int = bitmap.width
  private val height: Int = bitmap.height
  private val heightmapVertices = FloatArray(width * height * TOTAL_COMPONENT_COUNT)
  private val numElements: Int
  private val vertexBuffer: VertexBuffer
  private val indexBuffer: IndexBuffer

  init {
    if (width * height > 65536) {
      error("Heightmap is too large for the index buffer")
    }
    numElements = calculateNumElements()
    vertexBuffer = VertexBuffer(loadBitmapData(bitmap))
    indexBuffer = IndexBuffer(createIndexData())
  }

  private fun createIndexData(): ShortArray {
    val indexData = ShortArray(numElements)
    var offset: Int = 0
    for (row in 0 until (height - 1)) {
      for (col in 0 until (width - 1)) {
        val topLeftIndexNum: Short = (row * width + col).toShort()
        val topRightIndexNum: Short = (row * width + col + 1).toShort()
        val bottomLefIndexNum: Short = ((row + 1) * width + col).toShort()
        val bottomRightIndexNum: Short = ((row + 1) * width + col + 1).toShort()
        indexData[offset++] = topLeftIndexNum
        indexData[offset++] = bottomLefIndexNum
        indexData[offset++] = topRightIndexNum

        indexData[offset++] = topRightIndexNum
        indexData[offset++] = bottomLefIndexNum
        indexData[offset++] = bottomRightIndexNum
      }
    }
    return indexData
  }

  private fun calculateNumElements(): Int {
    return (width - 1) * (height - 1) * 2 * 3
  }

  private fun loadBitmapData(bitmap: Bitmap): FloatArray {
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    bitmap.recycle()
    val heightMapVertices = FloatArray(width * height * TOTAL_COMPONENT_COUNT)
    var offset: Int = 0
    for (row in 0 until height) {
      for (col in 0 until width) {
        val point: Point = getPoint(pixels, row, col)

        heightMapVertices[offset++] = point.x
        heightMapVertices[offset++] = point.y
        heightMapVertices[offset++] = point.z

        val top: Point = getPoint(pixels, row - 1, col)
        val left: Point = getPoint(pixels, row, col - 1)
        val right: Point = getPoint(pixels, row, col + 1)
        val bottom: Point = getPoint(pixels, row + 1, col)

        val rightToLeft = Geometry.vectorBetween(right, left)
        val topToBottom = Geometry.vectorBetween(top, bottom)
        val normal = rightToLeft.crossProduct(topToBottom).normalize()

        heightMapVertices[offset++] = normal.x
        heightMapVertices[offset++] = normal.y
        heightMapVertices[offset++] = normal.z
      }
    }
    return heightMapVertices
  }

  fun bindData(heightMapProgram: HeightmapShaderProgram) {
    vertexBuffer.setVertexAttribPointer(
      0,
      heightMapProgram.aPositionAttributeLocation,
      POSITION_COMPONENT_COUNT,
      STRIDE
    )
    vertexBuffer.setVertexAttribPointer(
      POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT,
      heightMapProgram.aNormalAttributeLocation,
      NORMAL_COMPONENT_COUNT,
      STRIDE
    )
  }

  private fun getPoint(pixels: IntArray, row: Int, col: Int): Point {
    val x = (col.toFloat() / (width - 1)) - 0.5f
    val z = (row.toFloat() / (height - 1)) - 0.5f

    val clampedRow = clamp(row, 0, width - 1)
    val clampedCol = clamp(col, 0, height - 1)

    val y = Color.red(pixels[(clampedRow * height) + clampedCol]) / 255f

    return Point(x, y, z)
  }

  private fun clamp(value: Int, min: Int, max: Int): Int {
    return max(min, min(max, value))
  }

  fun draw() {
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId)
    glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0)
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
  }
}