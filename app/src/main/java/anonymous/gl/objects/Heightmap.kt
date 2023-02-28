package anonymous.gl.objects

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glDrawElements
import anonymous.gl.data.IndexBuffer
import anonymous.gl.data.VertexBuffer
import anonymous.gl.programs.HeightmapShaderProgram

class Heightmap(bitmap: Bitmap) {
    companion object {
        const val POSITION_COMPONENT_COUNT = 3
    }

    private val width: Int = bitmap.width
    private val height: Int = bitmap.height
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
        val heightMapVertices = FloatArray(width * height * POSITION_COMPONENT_COUNT)
        var offset: Int = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                val xPosition = (col.toFloat() / (width - 1)) - 0.5f
                val yPosition = Color.red(pixels[(row * height) + col]) / 255f
                val zPosition = (row.toFloat() / (height - 1)) - 0.5f
                heightMapVertices[offset++] = xPosition
                heightMapVertices[offset++] = yPosition
                heightMapVertices[offset++] = zPosition
            }
        }
        return heightMapVertices
    }

    fun bindData(heightMapProgram: HeightmapShaderProgram) {
        vertexBuffer.setVertexAttribPointer(
            0,
            heightMapProgram.aPositionAttributeLocation,
            POSITION_COMPONENT_COUNT,
            0
        )
    }

    fun draw() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId)
        glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}