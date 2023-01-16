package anonymous.gl.programs

import android.content.Context
import android.opengl.GLES20.glUseProgram
import androidx.annotation.RawRes
import anonymous.gl.utils.ShaderHelper
import anonymous.gl.utils.TextResourceReader

open class ShaderProgram(
    context: Context,
    @RawRes vertexShaderResourceId: Int,
    @RawRes fragmentShaderResourceId: Int,
) {
    protected companion object {
        const val U_MATRIX = "u_Matrix"
        const val U_TEXTURE_UNIT = "u_TextureUnit"
        const val U_TEXTURE_UNIT_2 = "u_TextureUnit2"

        const val A_POSITION = "a_Position"
        const val A_COLOR = "a_Color"
        const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

        const val U_COLOR = "u_Color"
    }

    protected val program: Int = ShaderHelper.buildProgram(
        TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
        TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId),
    )

    fun useProgram() {
        glUseProgram(program)
    }
}