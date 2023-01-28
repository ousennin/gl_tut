package anonymous.gl

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: AirHockeyRenderer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val supportsEs2 = activityManager.deviceConfigurationInfo.reqGlEsVersion >= 0x20000
        if (supportsEs2) {
            glSurfaceView.setEGLContextClientVersion(2)
            renderer = AirHockeyRenderer(this)
            glSurfaceView.setRenderer(renderer)
        } else {
            return
        }
        glSurfaceView.setOnTouchListener { v, event ->
            val normalizedX = (event.x / v.width) * 2 - 1
            val normalizedY = -((event.y / v.height) * 2 - 1)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    renderer.handleTouchPress(normalizedX, normalizedY)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    renderer.handleTouchDrag(normalizedX, normalizedY)
                    true
                }
                else -> false
            }
        }
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}