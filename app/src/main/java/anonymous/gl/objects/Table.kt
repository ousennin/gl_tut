package anonymous.gl.objects

import android.opengl.GLES20.GL_TRIANGLE_FAN
import android.opengl.GLES20.glDrawArrays
import anonymous.gl.Constants
import anonymous.gl.data.VertexArray
import anonymous.gl.programs.TextureShaderProgram

class Table {
    private companion object {
        const val POSITION_COMPONENT_COUNT = 2
        const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        const val STRIDE: Int =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT

        private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
            // Triangle Fan
            0f, 0f, 0.5f, 0.5f,
            -0.5f, -0.8f, 0f, 0.9f,
            0.5f, -0.8f, 1f, 0.9f,
            0.5f, 0.8f, 1f, 0.1f,
            -0.5f, 0.8f, 0f, 0.1f,
            -0.5f, -0.8f, 0f, 0.9f
        )
    }

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    fun bindData(textureProgram: TextureShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            textureProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE);
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.aTextureCoordinatesLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE);
    }

    fun draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)
    }
}