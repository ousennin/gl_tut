package anonymous.gl.skybox.objects

import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glDrawElements
import anonymous.gl.data.VertexArray
import anonymous.gl.programs.SkyboxShaderProgram
import java.nio.ByteBuffer

class Skybox {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }

    private val vertexArray: VertexArray = VertexArray(
        floatArrayOf(
            -1f, 1f, 1f,
            1f, 1f, 1f,
            -1f, -1f, 1f,
            1f, -1f, 1f,
            -1f, 1f, -1f,
            1f, 1f, -1f,
            -1f, -1f, -1f,
            1f, -1f, -1f,
        )
    )
    private val indexArray: ByteBuffer = ByteBuffer.allocateDirect(36)
        .put(
            byteArrayOf(
                // Front
                1, 3, 0,
                0, 3, 2,
                // Back
                4, 6, 5,
                5, 6, 7,
                // Left
                0, 2, 4,
                4, 2, 6,
                // Right
                5, 7, 1,
                1, 7, 3,
                // Top
                5, 1, 4,
                4, 1, 0,
                // Bottom
                6, 2, 7,
                7, 2, 3
            )
        )
        .apply { position(0) }

    fun bindData(skyboxProgram: SkyboxShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            skyboxProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            0,
        )
    }

    fun draw() {
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indexArray)
    }
}