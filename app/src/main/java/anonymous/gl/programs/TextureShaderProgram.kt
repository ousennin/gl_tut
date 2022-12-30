package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import anonymous.gl.R

class TextureShaderProgram(context: Context) :
    ShaderProgram(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader) {
    private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation: Int = glGetUniformLocation(program, U_TEXTURE_UNIT)
    private val uTextureUnit2Location: Int = glGetUniformLocation(program, U_TEXTURE_UNIT_2)
    val aPositionLocation: Int = glGetAttribLocation(program, A_POSITION)
    val aTextureCoordinatesLocation: Int =
        glGetAttribLocation(program, A_TEXTURE_COORDINATES)

    fun setUniforms(matrix: FloatArray, textureId: Int, texture2Id: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        glActiveTexture(GL_TEXTURE0 + 0)
        glBindTexture(GL_TEXTURE_2D, textureId)

        glActiveTexture(GL_TEXTURE0 + 1)
        glBindTexture(GL_TEXTURE_2D, texture2Id)

        glUniform1i(uTextureUnitLocation, 0)
        glUniform1i(uTextureUnit2Location, 1)
    }
}