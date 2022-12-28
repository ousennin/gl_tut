package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import anonymous.gl.R

class ColorShaderProgram(context: Context) :
    ShaderProgram(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader) {
    private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)
    val aPositionLocation = glGetAttribLocation(program, A_POSITION)
    val aColorLocation = glGetAttribLocation(program, A_COLOR)

    fun setUniforms(matrix: FloatArray) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}