package anonymous.gl.objects

import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.glDrawArrays
import anonymous.gl.Constants.BYTES_PER_FLOAT
import anonymous.gl.data.VertexArray
import anonymous.gl.programs.ColorShaderProgram

class Mallet {
    private companion object {
        const val POSITION_COMPONENT_COUNT = 2
        const val COLOR_COMPONENT_COUNT = 3
        const val STRIDE: Int = ((POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)
            * BYTES_PER_FLOAT)
        val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, R, G, B
            0f, -0.4f, 0f, 0f, 1f,
            0f, 0.4f, 1f, 0f, 0f
        )
    }

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    fun bindData(colorShaderProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorShaderProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            colorShaderProgram.aColorLocation,
            COLOR_COMPONENT_COUNT,
            STRIDE,
        )
    }

    fun draw() {
        glDrawArrays(GL_POINTS, 0, 2)
    }
}