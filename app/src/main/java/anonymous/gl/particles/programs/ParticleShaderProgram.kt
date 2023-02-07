package anonymous.gl.particles.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import anonymous.gl.R
import anonymous.gl.programs.ShaderProgram

class ParticleShaderProgram(context: Context) : ShaderProgram(
    context,
    R.raw.particle_vertex_shader,
    R.raw.particle_fragment_shader,
) {
    val aPositionLocation: Int = glGetAttribLocation(program, A_POSITION)
    val aColorLocation: Int = glGetAttribLocation(program, A_COLOR)
    val aDirectionVectorLocation: Int = glGetAttribLocation(program, A_DIRECTION_VECTOR)
    val aParticleStartTimeLocation: Int = glGetAttribLocation(program, A_PARTICLE_START_TIME)
    val uTextureLocation: Int = glGetUniformLocation(program, U_TEXTURE_UNIT)

    private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)
    private val uTimeLocation: Int = glGetUniformLocation(program, U_TIME)

    fun setUniforms(matrix: FloatArray, elapsedTime: Float, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform1f(uTimeLocation, elapsedTime)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(uTextureLocation, 0)
    }
}