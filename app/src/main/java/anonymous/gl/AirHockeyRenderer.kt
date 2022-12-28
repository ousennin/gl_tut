package anonymous.gl

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_LINES
import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import anonymous.gl.utils.ShaderHelper
import anonymous.gl.utils.TextResourceReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private companion object {
        const val POSITION_COMPONENT_COUNT = 2
        const val BYTES_PER_FLOAT = 4

        const val U_COLOR = "u_Color"
        const val A_POSITION = "a_Position"
    }

    private val tableVerticesWithTriangles = floatArrayOf(
        // Triangle 1
        -0.5f, -0.5f,
        0.5f, 0.5f,
        -0.5f, 0.5f,

        // Triangle 2
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0.5f, 0.5f,

        // Line 1
        -0.5f, 0f,
        0.5f, 0f,

        // Mallets
        0f, -0.25f,
        0f, 0.25f,
    )

    private val vertexData: FloatBuffer =
        ByteBuffer.allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(tableVerticesWithTriangles) }

    private var vertexShaderSource: String = ""
    private var fragmentShaderSource: String = ""

    private var vertexShaderId: Int = 0
    private var fragmentShaderId: Int = 0

    private var program: Int = 0

    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader)
        fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader)
        vertexShaderId = ShaderHelper.compileVertexShader(vertexShaderSource)
        fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShaderSource)
        program = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId)
        ShaderHelper.isProgramValid(program)
        glUseProgram(program)

        uColorLocation = glGetUniformLocation(program, U_COLOR)
        aPositionLocation = glGetAttribLocation(program, A_POSITION)
        vertexData.position(0)
        glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GL_FLOAT,
            false,
            0,
            vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        glUniform4f(uColorLocation, 1f, 1f, 1f, 1f)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glUniform4f(uColorLocation, 1f,  0f, 0f, 1f)
        glDrawArrays(GL_LINES, 6, 2)

        glUniform4f(uColorLocation, 0f, 0f, 1f, 1f)
        glDrawArrays(GL_POINTS, 8,  1)

        glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)
        glDrawArrays(GL_POINTS, 9, 1)
    }
}