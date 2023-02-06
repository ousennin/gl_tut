package anonymous.gl.particles.objects

import android.opengl.Matrix
import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Vector
import kotlin.random.Random

class ParticleShooter(
    private val position: Point,
    direction: Vector,
    private val color: Int,
    private val angleVarianceInDegrees: Float,
    private val speedVariance: Float,
) {
    private val rotationMatrix = FloatArray(16)
    private val directionVector = FloatArray(4)
    private val resultVector = FloatArray(4)

    init {
        directionVector[0] = direction.x
        directionVector[1] = direction.y
        directionVector[2] = direction.z
    }

    fun addParticles(
        particleSystem: ParticleSystem,
        currentTime: Float,
        count: Int,
    ) {
        for (i in 0 until count) {
            Matrix.setRotateEulerM(
                rotationMatrix, 0,
                (Random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (Random.nextFloat() - 0.5f) * angleVarianceInDegrees,
                (Random.nextFloat() - 0.5f) * angleVarianceInDegrees,
            )
            Matrix.multiplyMV(
                resultVector, 0,
                rotationMatrix, 0,
                directionVector, 0,
            )
            val speedAdjustment = 1f + Random.nextFloat() * speedVariance
            val thisDirection = Vector(
                resultVector[0] * speedAdjustment,
                resultVector[1] * speedAdjustment,
                resultVector[2] * speedAdjustment,
            )
            particleSystem.addParticle(position, color, thisDirection, currentTime)
        }
    }
}