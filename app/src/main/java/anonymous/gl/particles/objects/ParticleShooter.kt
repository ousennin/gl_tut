package anonymous.gl.particles.objects

import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Vector

class ParticleShooter(
    private val position: Point,
    private val direction: Vector,
    private val color: Int,
) {
    fun addParticles(
        particleSystem: ParticleSystem,
        currentTime: Float,
        count: Int,
    ) {
        for (i in 0 until count) {
            particleSystem.addParticle(position, color, direction, currentTime)
        }
    }
}