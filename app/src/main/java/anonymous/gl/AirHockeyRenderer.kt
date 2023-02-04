package anonymous.gl

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix.invertM
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.setLookAtM
import android.opengl.Matrix.translateM
import anonymous.gl.objects.Mallet
import anonymous.gl.objects.Puck
import anonymous.gl.objects.Table
import anonymous.gl.programs.ColorShaderProgram
import anonymous.gl.programs.TextureShaderProgram
import anonymous.gl.utils.MatrixHelper
import anonymous.gl.utils.TextureHelper
import anonymous.gl.utils.geometry.Geometry
import anonymous.gl.utils.geometry.Plane
import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Ray
import anonymous.gl.utils.geometry.Sphere
import anonymous.gl.utils.geometry.Vector
import java.lang.Float.max
import java.lang.Float.min
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AirHockeyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)

    private val leftBound = -0.5f
    private val rightBound = 0.5f
    private val farBound = -0.8f
    private val nearBound = 0.5f

    private lateinit var table: Table
    private lateinit var mallet: Mallet
    private lateinit var puck: Puck

    private lateinit var textureShaderProgram: TextureShaderProgram
    private lateinit var colorShaderProgram: ColorShaderProgram

    private var texture: Int = 0
    private var texture2: Int = 0

    private var isMalletPressed = false
    private lateinit var blueMalletPosition: Point
    private lateinit var previousBlueMalletPosition: Point
    private lateinit var puckPosition: Point
    private lateinit var puckVector: Vector

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        table = Table()
        mallet = Mallet(0.08f, 0.15f, 32)
        puck = Puck(0.06f, 0.02f, 32)

        textureShaderProgram = TextureShaderProgram(context)
        colorShaderProgram = ColorShaderProgram(context)

        texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface)
        texture2 = TextureHelper.loadTexture(context, R.drawable.texture2)
        blueMalletPosition = Point(0f, mallet.height / 2f, 0.4f)
        previousBlueMalletPosition = blueMalletPosition
        puckPosition = Point(0f, puck.height / 2f, 0f)
        puckVector = Vector(0f, 0f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                / height.toFloat(), 1f, 10f
        )
        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        puckPosition = puckPosition.translate(puckVector)
        if (puckPosition.x < leftBound + puck.radius
            || puckPosition.x > rightBound - puck.radius
        ) {
            puckVector = Vector(-puckVector.x, puckVector.y, puckVector.z)
            puckVector = puckVector.scale(0.9f)
        }
        if (puckPosition.z < farBound + puck.radius ||
            puckPosition.z > nearBound - puck.radius
        ) {
            puckVector = Vector(puckVector.x, puckVector.y, -puckVector.z)
            puckVector = puckVector.scale(0.9f)
        }
        puckPosition = Point(
            clamp(puckPosition.x, leftBound + puck.radius, rightBound - puck.radius),
            puckPosition.y,
            clamp(puckPosition.z, farBound + puck.radius, nearBound - puck.radius)
        )

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)

        positionTableInScene()
        textureShaderProgram.useProgram()
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, texture, texture2)
        table.bindData(textureShaderProgram)
        table.draw()

        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorShaderProgram.useProgram()
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet.bindData(colorShaderProgram)
        mallet.draw()

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z)
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f)
        mallet.draw()

        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z)
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f)
        puck.bindData(colorShaderProgram)
        puck.draw()
    }

    private fun positionTableInScene() {
        setIdentityM(modelMatrix, 0)
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }

    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, x, y, z)
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val malletBoundingSphere = Sphere(
            Point(
                blueMalletPosition.x,
                blueMalletPosition.y,
                blueMalletPosition.z,
            ),
            mallet.height / 2f,
        )
        isMalletPressed = Geometry.intersects(malletBoundingSphere, ray)
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        if (isMalletPressed) {
            val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            val plane = Plane(
                Point(0f, 0f, 0f),
                Vector(0f, 1f, 0f)
            )
            val touchedPoint: Point = Geometry.intersectionPoint(ray, plane)
            blueMalletPosition = Point(
                clamp(
                    touchedPoint.x,
                    leftBound + mallet.radius,
                    rightBound - mallet.radius
                ),
                mallet.height / 2f,
                clamp(
                    touchedPoint.z,
                    0f + mallet.radius,
                    nearBound - mallet.radius,
                )
            )
            val distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length
            if (distance < (puck.radius + mallet.radius)) {
                puckVector = Geometry.vectorBetween(
                    previousBlueMalletPosition,
                    blueMalletPosition,
                )
            }
        }
    }

    private fun convertNormalized2DPointToRay(normalizedX: Float, normalizedY: Float): Ray {
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0)
        multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0)

        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        val nearPointRay = Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay = Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        return Ray(
            nearPointRay,
            Geometry.vectorBetween(nearPointRay, farPointRay)
        )
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return min(max, max(value, min))
    }
}