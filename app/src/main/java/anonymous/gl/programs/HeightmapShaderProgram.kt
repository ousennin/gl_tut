package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import anonymous.gl.R

class HeightmapShaderProgram(
  context: Context
) : ShaderProgram(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader) {
  val aPositionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)
  private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)

  fun setUniforms(matrix: FloatArray) {
    GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
  }
}