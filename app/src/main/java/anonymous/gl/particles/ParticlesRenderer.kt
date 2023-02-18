package anonymous.gl.particles

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_CULL_FACE
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_LEQUAL
import android.opengl.GLES20.GL_LESS
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDepthFunc
import android.opengl.GLES20.glDepthMask
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import anonymous.gl.R
import anonymous.gl.objects.Heightmap
import anonymous.gl.particles.objects.ParticleShooter
import anonymous.gl.particles.objects.ParticleSystem
import anonymous.gl.particles.programs.ParticleShaderProgram
import anonymous.gl.programs.HeightmapShaderProgram
import anonymous.gl.programs.SkyboxShaderProgram
import anonymous.gl.skybox.objects.Skybox
import anonymous.gl.utils.MatrixHelper
import anonymous.gl.utils.TextureHelper
import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Vector
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticlesRenderer(private val context: Context) : GLSurfaceView.Renderer {
  private val modelMatrix = FloatArray(16)
  private val viewMatrix = FloatArray(16)
  private val viewMatrixForSkybox = FloatArray(16)
  private val projectionMatrix = FloatArray(16)

  private val tempMatrix = FloatArray(16)
  private val modelViewProjectionMatrix = FloatArray(16)
  private lateinit var heightmapProgram: HeightmapShaderProgram
  private lateinit var heightmap: Heightmap

  private lateinit var skyboxProgram: SkyboxShaderProgram
  private lateinit var skybox: Skybox

  private lateinit var particleProgram: ParticleShaderProgram
  private lateinit var particleSystem: ParticleSystem
  private lateinit var redParticleShooter: ParticleShooter
  private lateinit var greenParticleShooter: ParticleShooter
  private lateinit var blueParticleShooter: ParticleShooter

  private var globalStartTime: Long = 0
  private var particleTexture = 0
  private var skyboxTexture = 0

  private var xRotation = 0f
  private var yRotation: Float = 0f


  fun handleTouchDrag(deltaX: Float, deltaY: Float) {
    xRotation += deltaX / 16f
    yRotation += deltaY / 16f
    if (yRotation < -90) {
      yRotation = -90f
    } else if (yRotation > 90) {
      yRotation = 90f
    }

    // Setup view matrix
    updateViewMatrices()
  }

  private fun updateViewMatrices() {
    Matrix.setIdentityM(viewMatrix, 0)
    Matrix.rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f)
    Matrix.rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f)
    System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.size)

    // We want the translation to apply to the regular view matrix, and not
    // the skybox.
    Matrix.translateM(viewMatrix, 0, 0f, -1.5f, -5f)
  }

  override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_CULL_FACE)
    heightmapProgram = HeightmapShaderProgram(context)
    heightmap = Heightmap(
      (context.resources
        .getDrawable(R.drawable.heightmap) as BitmapDrawable).bitmap
    )
    skyboxProgram = SkyboxShaderProgram(context)
    skybox = Skybox()
    particleProgram = ParticleShaderProgram(context)
    particleSystem = ParticleSystem(10000)
    globalStartTime = System.nanoTime()
    val particleDirection: Vector = Vector(0f, 0.5f, 0f)
    val angleVarianceInDegrees = 5f
    val speedVariance = 1f
    redParticleShooter = ParticleShooter(
      Point(-1f, 0f, 0f),
      particleDirection,
      Color.rgb(255, 50, 5),
      angleVarianceInDegrees,
      speedVariance
    )
    greenParticleShooter = ParticleShooter(
      Point(0f, 0f, 0f),
      particleDirection,
      Color.rgb(25, 255, 25),
      angleVarianceInDegrees,
      speedVariance
    )
    blueParticleShooter = ParticleShooter(
      Point(1f, 0f, 0f),
      particleDirection,
      Color.rgb(5, 50, 255),
      angleVarianceInDegrees,
      speedVariance
    )
    particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture)
    skyboxTexture = TextureHelper.loadCubeMap(
      context, intArrayOf(
        R.drawable.left, R.drawable.right,
        R.drawable.bottom, R.drawable.top,
        R.drawable.front, R.drawable.back
      )
    )
  }

  override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
    glViewport(0, 0, width, height)
    MatrixHelper.perspectiveM(
      projectionMatrix,
      45f,
      width.toFloat() / height.toFloat(),
      1f,
      100f
    )
    updateViewMatrices()
  }

  override fun onDrawFrame(glUnused: GL10?) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    drawHeightmap()
    drawSkybox()
    drawParticles()
  }

  private fun drawHeightmap() {
    Matrix.setIdentityM(modelMatrix, 0)
    Matrix.scaleM(modelMatrix, 0, 100f, 10f, 100f)
    updateMvpMatrix()
    heightmapProgram.useProgram()
    heightmapProgram.setUniforms(modelViewProjectionMatrix)
    heightmap.bindData(heightmapProgram)
    heightmap.draw()
  }

  private fun drawSkybox() {
    Matrix.setIdentityM(modelMatrix, 0)
    updateMvpMatrixForSkybox()
    glDepthFunc(GL_LEQUAL) // This avoids problems with the skybox itself getting clipped.
    skyboxProgram.useProgram()
    skyboxProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture)
    skybox.bindData(skyboxProgram)
    skybox.draw()
    glDepthFunc(GL_LESS)
  }

  private fun drawParticles() {
    val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f
    redParticleShooter.addParticles(particleSystem, currentTime, 1)
    greenParticleShooter.addParticles(particleSystem, currentTime, 1)
    blueParticleShooter.addParticles(particleSystem, currentTime, 1)
    Matrix.setIdentityM(modelMatrix, 0)
    updateMvpMatrix()
    glDepthMask(false)
    glEnable(GL_BLEND)
    glBlendFunc(GL_ONE, GL_ONE)
    particleProgram.useProgram()
    particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture)
    particleSystem.bindData(particleProgram)
    particleSystem.draw()
    glDisable(GL_BLEND)
    glDepthMask(true)
  }

  private fun updateMvpMatrix() {
    Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
  }

  private fun updateMvpMatrixForSkybox() {
    Matrix.multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
  }
}