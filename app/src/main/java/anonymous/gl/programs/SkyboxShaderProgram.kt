package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import androidx.annotation.RawRes

class SkyboxShaderProgram(
    context: Context,
    @RawRes vertexShaderId: Int,
    @RawRes fragmentShaderId: Int,
) : ShaderProgram(context, vertexShaderId, fragmentShaderId) {
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)
    val aPositionLocation = glGetAttribLocation(program, A_POSITION)

    fun setUniforms(matrix: FloatArray, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId)
        glUniform1i(uTextureUnitLocation, 0)
    }
}