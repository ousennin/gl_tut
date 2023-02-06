package anonymous.gl.particles.objects

import android.graphics.Color
import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.glDrawArrays
import anonymous.gl.Constants.BYTES_PER_FLOAT
import anonymous.gl.data.VertexArray
import anonymous.gl.particles.programs.ParticleShaderProgram
import anonymous.gl.utils.geometry.Point
import anonymous.gl.utils.geometry.Vector

class ParticleSystem(private val maxParticlesCount: Int) {
    private companion object {
        const val POSITION_COMPONENT_COUNT = 3
        const val COLOR_COMPONENT_COUNT = 3
        const val VECTOR_COMPONENT_COUNT = 3
        const val PARTICLE_START_TIME_COMPONENT_COUNT = 1

        const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
            COLOR_COMPONENT_COUNT +
            VECTOR_COMPONENT_COUNT +
            PARTICLE_START_TIME_COMPONENT_COUNT

        const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;
    }

    private val particles: FloatArray = FloatArray(maxParticlesCount * TOTAL_COMPONENT_COUNT)
    private val vertexArray = VertexArray(particles)

    private var currentParticleCount: Int = 0
    private var nextParticle: Int = 0

    fun addParticle(position: Point, color: Int, direction: Vector, particleStartTime: Float) {
        val particleOffset = nextParticle * TOTAL_COMPONENT_COUNT

        var currentOffset = particleOffset
        nextParticle++

        if (currentParticleCount < maxParticlesCount) {
            currentParticleCount++
        }
        if (nextParticle == maxParticlesCount) {
            nextParticle = 0
        }

        particles[currentOffset++] = position.x
        particles[currentOffset++] = position.y
        particles[currentOffset++] = position.z

        particles[currentOffset++] = Color.red(color) / 255f
        particles[currentOffset++] = Color.green(color) / 255f
        particles[currentOffset++] = Color.blue(color) / 255f

        particles[currentOffset++] = direction.x
        particles[currentOffset++] = direction.y
        particles[currentOffset++] = direction.z

        particles[currentOffset] = particleStartTime
        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT)
    }

    fun bindData(particleShaderProgram: ParticleShaderProgram) {
        var dataOffset = 0
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleShaderProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        dataOffset += POSITION_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleShaderProgram.aColorLocation,
            COLOR_COMPONENT_COUNT,
            STRIDE
        )
        dataOffset += COLOR_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleShaderProgram.aDirectionVectorLocation,
            VECTOR_COMPONENT_COUNT,
            STRIDE,
        )
        dataOffset += VECTOR_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleShaderProgram.aParticleStartTimeLocation,
            PARTICLE_START_TIME_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw() {
        glDrawArrays(GL_POINTS, 0, currentParticleCount)
    }
}