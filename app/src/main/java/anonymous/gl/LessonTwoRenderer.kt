//package anonymous.gl
//
//import android.opengl.GLES20
//import android.opengl.GLSurfaceView
//import android.opengl.Matrix
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.FloatBuffer
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//
//class LessonTwoRenderer : GLSurfaceView.Renderer {
//    private companion object {
//        const val BYTES_PER_FLOAT = 4
//        const val POSITION_DATA_SIZE = 3
//        const val COLOR_DATA_SIZE = 4
//        const val NORMAL_DATA_SIZE = 4
//    }
//
//    private val modelMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val mvpMatrix = FloatArray(16)
//
//    private val lightModelMatrix = FloatArray(16)
//
//    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
//     * we multiply this by our transformation matrices.  */
//    private val lightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
//
//    /** Used to hold the current position of the light in world space (after transformation via model matrix).  */
//    private val lightPosInWorldSpace = FloatArray(4)
//
//    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)  */
//    private val lightPosInEyeSpace = FloatArray(4)
//
//    private val cubePositions: FloatBuffer
//    private val cubeColors: FloatBuffer
//    private val cubeNormals: FloatBuffer
//
//    private var mvpMatrixHandle = 0
//    private var mvMatrixHandle = 0
//    private var lightPosHandle = 0
//    private var positionHandle = 0
//    private var colorHandle = 0
//    private var normalHandle = 0
//
//    private var perVertexProgramHandle = 0
//    private var pointProgramHandle = 0
//
//    init {
//        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
//        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
//        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
//        // usually represent the backside of an object and aren't visible anyways.
//
//        // Front face
//        val cubePositionData = floatArrayOf(
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//
//            // Right face
//            1.0f, 1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, -1.0f, -1.0f,
//            1.0f, 1.0f, -1.0f,
//
//            // Back face
//            1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, -1.0f,
//
//            // Left face
//            -1.0f, 1.0f, -1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, -1.0f, 1.0f,
//            -1.0f, 1.0f, 1.0f,
//
//            // Top face
//            -1.0f, 1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, -1.0f,
//
//            // Bottom face
//            1.0f, -1.0f, -1.0f,
//            1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//            1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//        )
//
//        // R, G, B, A
//        val cubeColorData = floatArrayOf(
//            // Front face (red)
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//
//            // Right face (green)
//            0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 1.0f, 0.0f, 1.0f,
//
//            // Back face (blue)
//            0.0f, 0.0f, 1.0f, 1.0f,
//            0.0f, 0.0f, 1.0f, 1.0f,
//            0.0f, 0.0f, 1.0f, 1.0f,
//            0.0f, 0.0f, 1.0f, 1.0f,
//            0.0f, 0.0f, 1.0f, 1.0f,
//            0.0f, 0.0f, 1.0f, 1.0f,
//
//            // Left face (yellow)
//            1.0f, 1.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f,
//
//            // Top face (cyan)
//            0.0f, 1.0f, 1.0f, 1.0f,
//            0.0f, 1.0f, 1.0f, 1.0f,
//            0.0f, 1.0f, 1.0f, 1.0f,
//            0.0f, 1.0f, 1.0f, 1.0f,
//            0.0f, 1.0f, 1.0f, 1.0f,
//            0.0f, 1.0f, 1.0f, 1.0f,
//
//            // Bottom face (magenta)
//            1.0f, 0.0f, 1.0f, 1.0f,
//            1.0f, 0.0f, 1.0f, 1.0f,
//            1.0f, 0.0f, 1.0f, 1.0f,
//            1.0f, 0.0f, 1.0f, 1.0f,
//            1.0f, 0.0f, 1.0f, 1.0f,
//            1.0f, 0.0f, 1.0f, 1.0f
//        )
//
//        // X, Y, Z
//        // The normal is used in light calculations and is a vector which points
//        // orthogonal to the plane of the surface. For a cube model, the normals
//        // should be orthogonal to the points of each face.
//        val cubeNormalData = floatArrayOf(
//            // Front face
//            0.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 1.0f,
//
//            // Right face
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 0.0f,
//            1.0f, 0.0f, 0.0f,
//
//            // Back face
//            0.0f, 0.0f, -1.0f,
//            0.0f, 0.0f, -1.0f,
//            0.0f, 0.0f, -1.0f,
//            0.0f, 0.0f, -1.0f,
//            0.0f, 0.0f, -1.0f,
//            0.0f, 0.0f, -1.0f,
//
//            // Left face
//            -1.0f, 0.0f, 0.0f,
//            -1.0f, 0.0f, 0.0f,
//            -1.0f, 0.0f, 0.0f,
//            -1.0f, 0.0f, 0.0f,
//            -1.0f, 0.0f, 0.0f,
//            -1.0f, 0.0f, 0.0f,
//
//            // Top face
//            0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f,
//
//            // Bottom face
//            0.0f, -1.0f, 0.0f,
//            0.0f, -1.0f, 0.0f,
//            0.0f, -1.0f, 0.0f,
//            0.0f, -1.0f, 0.0f,
//            0.0f, -1.0f, 0.0f,
//            0.0f, -1.0f, 0.0f
//        )
//
//        cubePositions = ByteBuffer.allocateDirect(cubePositionData.size * BYTES_PER_FLOAT)
//            .order(ByteOrder.nativeOrder()).asFloatBuffer()
//        cubePositions.put(cubePositionData).position(0)
//        cubeColors = ByteBuffer.allocateDirect(cubePositionData.size * BYTES_PER_FLOAT)
//            .order(ByteOrder.nativeOrder()).asFloatBuffer()
//        cubeColors.put(cubePositionData).position(0)
//        cubeNormals = ByteBuffer.allocateDirect(cubePositionData.size * BYTES_PER_FLOAT)
//            .order(ByteOrder.nativeOrder()).asFloatBuffer()
//        cubeNormals.put(cubePositionData).position(0)
//    }
//
//    private fun getVertexShader(): String {
//        // TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
//        return """
//                uniform mat4 u_MVPMatrix;
//                uniform mat4 u_MVMatrix;
//                uniform vec3 u_LightPos;
//                attribute vec4 a_Position;
//                attribute vec4 a_Color;
//                attribute vec3 a_Normal;
//                varying vec4 v_Color;
//                void main()
//                {
//                   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
//                   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
//                   float distance = length(u_LightPos - modelViewVertex);
//                   vec3 lightVector = normalize(u_LightPos - modelViewVertex);
//                   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
//                   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
//                   v_Color = a_Color * diffuse;
//                   gl_Position = u_MVPMatrix * a_Position;
//                }
//                """
//    }
//
//    private fun getFragmentShader(): String? {
//        return """
//            precision mediump float;
//            varying vec4 v_Color;
//            void main()
//            {
//               gl_FragColor = v_Color;
//            }
//            """
//    }
//
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        // Set the background clear color to black.
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//
//        // Use culling to remove back faces.
//        GLES20.glEnable(GLES20.GL_CULL_FACE)
//
//        // Enable depth testing
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//
//        // Position the eye in front of the origin.
//        val eyeX = 0.0f;
//        val eyeY = 0.0f;
//        val eyeZ = -0.5f;
//
//        // We are looking toward the distance
//        val lookX = 0.0f;
//        val lookY = 0.0f;
//        val lookZ = -5.0f;
//
//        // Set our up vector. This is where our head would be pointing were we holding the camera.
//        val upX = 0.0f;
//        val upY = 1.0f;
//        val upZ = 0.0f;
//
//        // Set the view matrix. This matrix can be said to represent the camera position.
//        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
//        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
//        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
//
//        val vertexShader = getVertexShader()
//        val fragmentShader = getFragmentShader()
//
//        val vertexShaderHandle: Int = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
//        val fragmentShaderHandle: Int = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
//
//        perVertexProgramHandle = createAndLinkProgram(
//            vertexShaderHandle,
//            fragmentShaderHandle,
//            arrayOf("a_Position", "a_Color", "a_Normal")
//        )
//    }
//
//    /**
//     * Helper function to compile and link a program.
//     *
//     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
//     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
//     * @param attributes Attributes that need to be bound to the program.
//     * @return An OpenGL handle to the program.
//     */
//    private fun createAndLinkProgram(vertexShaderHandle: Int, fragmentShaderHandle: Int, attributes: Array<String>): Int {
//
//    }
//
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        // Set the OpenGL viewport to the same size as the surface.
//        // Set the OpenGL viewport to the same size as the surface.
//        GLES20.glViewport(0, 0, width, height)
//
//        // Create a new perspective projection matrix. The height will stay the same
//        // while the width will vary as per aspect ratio.
//
//        // Create a new perspective projection matrix. The height will stay the same
//        // while the width will vary as per aspect ratio.
//        val ratio = width.toFloat() / height
//        val left = -ratio
//        val right = ratio
//        val bottom = -1.0f
//        val top = 1.0f
//        val near = 1.0f
//        val far = 10.0f
//
//        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
//    }
//
//    override fun onDrawFrame(gl: GL10?) {
//        TODO("Not yet implemented")
//    }
//}