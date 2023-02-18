package anonymous.gl.particles

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import anonymous.gl.R
import anonymous.gl.particles.objects.ParticleShooter
import anonymous.gl.particles.objects.ParticleSystem
import anonymous.gl.particles.programs.ParticleShaderProgram
import anonymous.gl.programs.SkyboxShaderProgram
import anonymous.gl.skybox.objects.Skybox
import anonymous.gl.utils.MatrixHelper
import anonymous.gl.utils.TextureHelper
import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Vector
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticlesRenderer(private val context: Context) : GLSurfaceView.Renderer {
    companion object {
        private const val angleVarianceInDegrees: Float = 5f
        private const val speedVariance = 1f
        private const val PARTICLES_COUNT = 1
    }

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)

    private lateinit var particleShaderProgram: ParticleShaderProgram
    private lateinit var particleSystem: ParticleSystem
    private lateinit var redParticleShooter: ParticleShooter
    private lateinit var greenParticleShooter: ParticleShooter
    private lateinit var blueParticleShooter: ParticleShooter

    private var texture: Int = 0

    private var globalStartTime: Long = 0L

    private lateinit var skyboxProgram: SkyboxShaderProgram
    private lateinit var skybox: Skybox
    private var skyboxTexture = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        particleShaderProgram = ParticleShaderProgram(context)
        particleSystem = ParticleSystem(10000)
        globalStartTime = System.nanoTime()

        val particleDirection = Vector(0f, 0.5f, 0f)
        redParticleShooter = ParticleShooter(
            Point(-1f, 0f, 0f),
            particleDirection,
            Color.rgb(255, 50, 5),
            angleVarianceInDegrees,
            speedVariance,
        )
        greenParticleShooter = ParticleShooter(
            Point(0f, 0f, 0f),
            particleDirection,
            Color.rgb(25, 255, 25),
            angleVarianceInDegrees,
            speedVariance,
        )
        blueParticleShooter = ParticleShooter(
            Point(1f, 0f, 0f),
            particleDirection,
            Color.rgb(5, 50, 255),
            angleVarianceInDegrees,
            speedVariance,
        )
        texture = TextureHelper.loadTexture(context, R.drawable.particle_texture)

        skyboxProgram = SkyboxShaderProgram(
            context,
            vertexShaderId = R.raw.skybox_vertex_shader,
            fragmentShaderId = R.raw.skybox_fragment_shader,
        )
        skybox = Skybox()
        skyboxTexture = TextureHelper.loadCubeMap(
            context,
            intArrayOf(
                R.drawable.left,
                R.drawable.right,
                R.drawable.bottom,
                R.drawable.top,
                R.drawable.front,
                R.drawable.back,
            )
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        MatrixHelper.perspectiveM(
            projectionMatrix,
            45f,
            width.toFloat() / height.toFloat(),
            1f,
            10f,
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        drawSkybox()
        drawParticles()
    }

    private fun drawSkybox() {
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        skyboxProgram.useProgram()
        skyboxProgram.setUniforms(viewProjectionMatrix, skyboxTexture)
        skybox.bindData(skyboxProgram)
        skybox.draw()
    }

    private fun drawParticles() {
        val currentTime: Float = (System.nanoTime() - globalStartTime) / 1000000000f

        redParticleShooter.addParticles(particleSystem, currentTime, PARTICLES_COUNT)
        greenParticleShooter.addParticles(particleSystem, currentTime, PARTICLES_COUNT)
        blueParticleShooter.addParticles(particleSystem, currentTime, PARTICLES_COUNT)

        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        particleShaderProgram.useProgram()
        particleShaderProgram.setUniforms(viewProjectionMatrix, currentTime, texture)
        particleSystem.bindData(particleShaderProgram)
        particleSystem.draw()

        glDisable(GL_BLEND)
    }
}