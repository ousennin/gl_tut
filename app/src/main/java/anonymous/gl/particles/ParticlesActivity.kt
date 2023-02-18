package anonymous.gl.particles

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity

class ParticlesActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: ParticlesRenderer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val supportsEs2 = activityManager.deviceConfigurationInfo.reqGlEsVersion >= 0x20000
        if (supportsEs2) {
            glSurfaceView.setEGLContextClientVersion(2)
            renderer = ParticlesRenderer(this)
            glSurfaceView.setRenderer(renderer)
        } else {
            return
        }
        glSurfaceView.setOnTouchListener(object : OnTouchListener {
            private var previousX: Float = 0f
            private var previousY: Float = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return when {
                    event != null -> when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            previousX = event.x
                            previousY = event.y
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.x - previousX
                            val deltaY = event.y - previousY
                            previousX = event.x
                            previousY = event.y
                            glSurfaceView.queueEvent {
                                renderer.handleTouchDrag(deltaX, deltaY)
                            }
                            true
                        }
                        else -> false
                    }
                    else -> false
                }
            }

        })
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