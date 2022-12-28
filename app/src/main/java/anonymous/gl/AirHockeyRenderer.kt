package anonymous.gl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
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
        const val COLOR_COMPONENT_COUNT = 3
        const val BYTES_PER_FLOAT = 4
        const val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT

        const val A_COLOR = "a_Color"
        const val U_MATRIX = "u_Matrix"
        const val A_POSITION = "a_Position"
    }

    private val tableVerticesWithTriangles = floatArrayOf(
        // Order of coordinates: X, Y, R, G, B
        // Triangle Fan
        0f, 0f, 1f, 1f, 1f,
        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
        0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
        0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
        -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
        -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
        // Line 1
        -0.5f, 0f, 1f, 0f, 0f,
        0.5f, 0f, 1f, 0f, 0f,
        // Mallets
        0f, -0.4f, 0f, 0f, 1f,
        0f, 0.4f, 1f, 0f, 0f
    )

    private val projectionMatrix = FloatArray(16)

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

    private var aColorLocation: Int = 0
    private var aPositionLocation: Int = 0
    private var uMatrixLocation: Int = 0


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        vertexShaderSource =
            TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader)
        fragmentShaderSource =
            TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader)
        vertexShaderId = ShaderHelper.compileVertexShader(vertexShaderSource)
        fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShaderSource)
        program = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId)
        ShaderHelper.isProgramValid(program)
        glUseProgram(program)

        aColorLocation = glGetAttribLocation(program, A_COLOR)
        aPositionLocation = glGetAttribLocation(program, A_POSITION)
        vertexData.position(0)
        glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)

        vertexData.position(POSITION_COMPONENT_COUNT)
        glVertexAttribPointer(
            aColorLocation,
            COLOR_COMPONENT_COUNT,
            GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        glEnableVertexAttribArray(aColorLocation)

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspectRatio = if (width > height) {
            width.toFloat() / height
        } else {
            height.toFloat() / width
        }
        if (width > height) {
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)

        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)

        glDrawArrays(GL_LINES, 6, 2)

        glDrawArrays(GL_POINTS, 8, 1)

        glDrawArrays(GL_POINTS, 9, 1)
    }
}