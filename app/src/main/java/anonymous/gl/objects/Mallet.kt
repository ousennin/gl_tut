package anonymous.gl.objects

import anonymous.gl.data.VertexArray
import anonymous.gl.programs.ColorShaderProgram
import anonymous.gl.utils.geometry.ObjectBuilder
import anonymous.gl.utils.geometry.Point

class Mallet(
    val radius: Float,
    val height: Float,
    numPointsAroundMallet: Int,
) {
    private companion object {
        const val POSITION_COMPONENT_COUNT = 3;
    }

    private val vertexArray: VertexArray
    private val drawList: List<ObjectBuilder.DrawCommand>

    init {
        val generatedData = ObjectBuilder.createMallet(
            Point(0f, 0f, 0f),
            radius,
            height,
            numPointsAroundMallet,
        )
        vertexArray = VertexArray(generatedData.vertexData)
        drawList = generatedData.drawList
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT,
            0,
        )
    }

    fun draw() {
        for (drawCommand in drawList) {
            drawCommand.draw()
        }
    }
}