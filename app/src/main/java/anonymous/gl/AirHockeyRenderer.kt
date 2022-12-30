package anonymous.gl

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import anonymous.gl.objects.Mallet
import anonymous.gl.objects.Table
import anonymous.gl.programs.ColorShaderProgram
import anonymous.gl.programs.TextureShaderProgram
import anonymous.gl.utils.MatrixHelper
import anonymous.gl.utils.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AirHockeyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private lateinit var table: Table
    private lateinit var mallet: Mallet

    private lateinit var textureShaderProgram: TextureShaderProgram
    private lateinit var colorShaderProgram: ColorShaderProgram

    private var texture: Int = 0
    private var texture2: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        table = Table()
        mallet = Mallet()

        textureShaderProgram = TextureShaderProgram(context)
        colorShaderProgram = ColorShaderProgram(context)

        texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface)
        texture2 = TextureHelper.loadTexture(context, R.drawable.texture2)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                / height.toFloat(), 1f, 10f
        )

        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, -2.5f)
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        textureShaderProgram.useProgram()
        textureShaderProgram.setUniforms(projectionMatrix, texture, texture2)
        table.bindData(textureShaderProgram)
        table.draw()

        colorShaderProgram.useProgram()
        colorShaderProgram.setUniforms(projectionMatrix)
        mallet.bindData(colorShaderProgram)
        mallet.draw()
    }
}