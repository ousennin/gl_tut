package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform3fv
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUniformMatrix4fv
import anonymous.gl.R

class HeightmapShaderProgram(
  context: Context
) : ShaderProgram(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader) {
  val aPositionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)
  val aNormalAttributeLocation: Int = glGetAttribLocation(program, A_NORMAL)
  private val uVectorToLightLocation: Int = glGetUniformLocation(program, U_VECTOR_TO_LIGHT)
  private val uMVMatrixLocation: Int = glGetUniformLocation(program, U_MV_MATRIX)
  private val uITMVMatrixLocation = glGetUniformLocation(program, U_IT_MV_MATRIX)
  private val uMVPMatrixLocation = glGetUniformLocation(program, U_MVP_MATRIX)
  private val uPointLightPositionsLocation = glGetUniformLocation(program, U_POINT_LIGHT_POSITIONS)
  private val uPointLightColorsLocation = glGetUniformLocation(program, U_POINT_LIGHT_COLORS)


  fun setUniforms(
    mvMatrix: FloatArray,
    it_mvMatrix: FloatArray,
    mvpMatrix: FloatArray,
    vectorToDirectionalLight: FloatArray,
    pointLightPositions: FloatArray,
    pointLightColors: FloatArray
  ) {
    glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0)
    glUniformMatrix4fv(uITMVMatrixLocation, 1, false, it_mvMatrix, 0)
    glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)

    glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0)
    glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0)
    glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0)
  }
}