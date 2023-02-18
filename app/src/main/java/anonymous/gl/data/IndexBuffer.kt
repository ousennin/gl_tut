package anonymous.gl.data

import android.opengl.GLES20
import anonymous.gl.Constants
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class IndexBuffer(
    indexData: ShortArray
) {
    val bufferId: Int

    init {
        val buffers = IntArray(1)
        GLES20.glGenBuffers(buffers.size, buffers, 0)
        if (buffers[0] == 0) {
            error("Could not create a new vertex buffer object")
        }
        bufferId = buffers[0]

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0])

        val indexArray: ShortBuffer = ByteBuffer
            .allocateDirect(indexData.size * Constants.BYTES_PER_SHORT)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indexData)
        indexArray.position(0)

        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indexArray.capacity() * Constants.BYTES_PER_SHORT,
            indexArray,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}