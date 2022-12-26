package anonymous.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LessonOneRenderer : GLSurfaceView.Renderer {
    private companion object {
        private const val BYTES_PER_FLOAT = 4

        /** How many elements per vertex.  */
        private const val STRIDE_BYTES: Int = 7 * BYTES_PER_FLOAT

        /** Size of the position data in elements.  */
        private const val POSITION_DATA_SIZE = 3

        /** Offset of the color data.  */
        private const val COLOR_OFFSET = 3

        /** Size of the color data in elements.  */
        private const val COLOR_DATA_SIZE = 4
    }

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val modelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val viewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport.  */
    private val projectionMatrix = FloatArray(16)

    /** Allocate storage for the final combined matrix. This will be passed into the shader program.  */
    private val mvpMatrix = FloatArray(16)

    /** This will be used to pass in the transformation matrix. */
    private var mvpMatrixHandle: Int = 0

    /** This will be used to pass in model position information.  */
    private var positionHandle = 0

    /** This will be used to pass in model color information.  */
    private var colorHandle = 0

    /** Offset of the position data.  */
    private var positionOffset = 0


    private var triangleOneVertices: FloatBuffer
    private var triangleTwoVertices: FloatBuffer
    private var triangleThreeVertices: FloatBuffer
    

    init {
        // This triangle is red, green, and blue.
        val triangleOneVerticesData = floatArrayOf(
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559017f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        )

        // This triangle is yellow, cyan, and magenta.
        val triangleTwoVerticesData = floatArrayOf(
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            0.0f, 0.559017f, 0.0f,
            1.0f, 0.0f, 1.0f, 1.0f
        )

        // This triangle is white, gray, and black.
        val triangleThreeVerticesData = floatArrayOf(
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f,

            0.0f, 0.559017f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        )
        
        triangleOneVertices =
            ByteBuffer.allocateDirect(triangleOneVerticesData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleTwoVertices =
            ByteBuffer.allocateDirect(triangleTwoVerticesData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleThreeVertices =
            ByteBuffer.allocateDirect(triangleThreeVerticesData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()

        triangleOneVertices.put(triangleOneVerticesData).position(0)
        triangleTwoVertices.put(triangleTwoVerticesData).position(0)
        triangleThreeVertices.put(triangleThreeVerticesData).position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f

        // We are looking toward the distance
        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)

        val vertexShader: String =
            "uniform mat4 u_MVPMatrix;      "  +      // A constant representing the combined model/view/projection matrix.

        "attribute vec4 a_Position;     " +       // Per-vertex position information we will pass in.
                "attribute vec4 a_Color;        " +       // Per-vertex color information we will pass in.

                "varying vec4 v_Color;          " +       // This will be passed into the fragment shader.

                "void main()                    " +       // The entry point for our vertex shader.
                "{                              " +
        "   v_Color = a_Color;          " +       // Pass the color through to the fragment shader.
                // It will be interpolated across the triangle.
                "   gl_Position = u_MVPMatrix   " +   // gl_Position is a special variable used to store the final position.
                "               * a_Position;   " +    // Multiply the vertex by the matrix to get the final point in
                "}                              "    // normalized screen coordinates.

        val fragmentShader =
            "precision mediump float;       " +        // Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    "varying vec4 v_Color;          " +        // This is the color from the vertex shader interpolated across the
                    // triangle per fragment.
                    "void main()                    " +        // The entry point for our fragment shader.
                    "{                              " +
                    "   gl_FragColor = v_Color;     " +        // Pass the color directly through the pipeline.
                    "}                              "

        // Load in the vertex shader.
        val vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader)

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle)
                throw RuntimeException("vertex shader compilation failed")
            }
        }
        if (vertexShaderHandle == 0) {
            throw RuntimeException("Error creating vertex shader.")
        }

        // Load in the fragment shader shader.
        var fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        if (fragmentShaderHandle != 0) {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader)
            GLES20.glCompileShader(fragmentShaderHandle)
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle)
                fragmentShaderHandle = 0
            }
        }
        if (fragmentShaderHandle == 0) {
            throw RuntimeException("Error creating fragment shader.")
        }

        var programHandle = GLES20.glCreateProgram()
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle)
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position")
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color")
            GLES20.glLinkProgram(programHandle)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
            if (programHandle == 0) {
                throw RuntimeException("Error creating program")
            }

            // Set program handles. These will later be used to pass in values to the program.
            mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
            positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
            colorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

            GLES20.glUseProgram(programHandle)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10_000L
        val angleInDegrees = (360f / 10_000f) * time

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(triangleOneVertices)

        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(triangleTwoVertices)

        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(triangleThreeVertices)
    }

    /**
     * Draws a triangle from the given vertex data.
     *
     * @param triangleBuffer The buffer containing the vertex data.
     */
    private fun drawTriangle(triangleBuffer: FloatBuffer) {
        triangleBuffer.position(positionOffset)
        GLES20.glVertexAttribPointer(
            positionHandle,
            POSITION_DATA_SIZE,
            GLES20.GL_FLOAT,
            false,
            STRIDE_BYTES,
            triangleBuffer,
        )
        GLES20.glEnableVertexAttribArray(positionHandle)

        triangleBuffer.position(COLOR_OFFSET)
        GLES20.glVertexAttribPointer(
            colorHandle,
            COLOR_DATA_SIZE,
            GLES20.GL_FLOAT,
            false,
            STRIDE_BYTES,
            triangleBuffer
        )
        GLES20.glEnableVertexAttribArray(colorHandle)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }
}