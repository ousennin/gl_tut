package anonymous.gl.utils

import android.content.Context
import androidx.annotation.RawRes
import java.nio.charset.Charset

object TextResourceReader {
    fun readTextFileFromResource(context: Context, @RawRes resourceId: Int): String {
        return try {
            context.resources.openRawResource(resourceId)
                .use { inputStream ->
                    inputStream
                        .readBytes()
                        .toString(Charset.defaultCharset())
                }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}