package com.akshar.wallpaperengine.wallpaper

import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.*

class LiveWallpaperEngineService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LiveShaderEngine()
    }

    inner class LiveShaderEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var time = 0f
        private var touchX = -1f
        private var touchY = -1f
        private var touchRipple = 0f

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var runtimeShader: Any? = null // RuntimeShader on API 33+

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) {
                    handler.postDelayed(this, 33L) // ~30 FPS for battery balance
                }
            }
        }

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val shaderSource = """
                        uniform float2 resolution;
                        uniform float time;
                        uniform float2 touch;
                        uniform float touchRipple;

                        half4 main(in float2 fragCoord) {
                            float2 uv = fragCoord.xy / resolution.xy;
                            float aspect = resolution.x / resolution.y;
                            uv.x *= aspect;

                            // Cosmic wave coordinates
                            float moveX = sin(time * 0.15 + uv.y * 2.0) * 0.15;
                            float moveY = cos(time * 0.12 + uv.x * 1.8) * 0.15;

                            float d = length(uv - float2(0.5 * aspect + moveX, 0.4 + moveY));
                            float glow = smoothstep(0.8, 0.0, d) * 0.4;

                            // Interactive ripple on user touch
                            if (touchRipple > 0.0) {
                                float2 touchUv = touch / resolution.xy;
                                touchUv.x *= aspect;
                                float touchDist = length(uv - touchUv);
                                float rippleWave = sin(touchDist * 20.0 - time * 2.0) * 0.5 + 0.5;
                                float rippleMask = smoothstep(0.5, 0.0, touchDist) * touchRipple;
                                glow += rippleWave * rippleMask * 0.5;
                            }

                            // Dark OLED violet palette
                            half4 bgColor = half4(0.02, 0.02, 0.05, 1.0);
                            half4 accentColor = half4(0.55, 0.20, 0.95, 1.0);
                            half4 cyanColor = half4(0.10, 0.80, 0.95, 1.0);

                            half4 mixedColor = mix(bgColor, accentColor, glow);
                            mixedColor = mix(mixedColor, cyanColor, sin(time * 0.05 + uv.x) * 0.1);

                            return mixedColor;
                        }
                    """.trimIndent()
                    runtimeShader = android.graphics.RuntimeShader(shaderSource)
                } catch (e: Exception) {
                    runtimeShader = null
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        override fun onTouchEvent(event: MotionEvent?) {
            if (event == null) return
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                touchX = event.x
                touchY = event.y
                touchRipple = 1.0f
            }
            super.onTouchEvent(event)
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    time += 0.05f
                    if (touchRipple > 0.01f) {
                        touchRipple *= 0.92f
                    }

                    val width = canvas.width.toFloat()
                    val height = canvas.height.toFloat()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
                        val shader = runtimeShader as android.graphics.RuntimeShader
                        shader.setFloatUniform("resolution", width, height)
                        shader.setFloatUniform("time", time)
                        shader.setFloatUniform("touch", touchX, touchY)
                        shader.setFloatUniform("touchRipple", touchRipple)
                        paint.shader = shader
                        canvas.drawRect(0f, 0f, width, height, paint)
                    } else {
                        // Fallback procedural canvas render
                        canvas.drawColor(Color.parseColor("#06060A"))
                        val cx = width * 0.5f + sin(time * 0.3f) * 60f
                        val cy = height * 0.4f + cos(time * 0.2f) * 60f

                        val radialGradient = RadialGradient(
                            cx, cy, max(width, height) * 0.6f,
                            intArrayOf(Color.parseColor("#5B21B6"), Color.parseColor("#1E1B4B"), Color.parseColor("#06060A")),
                            floatArrayOf(0f, 0.5f, 1f),
                            Shader.TileMode.CLAMP
                        )
                        paint.shader = radialGradient
                        canvas.drawRect(0f, 0f, width, height, paint)
                    }
                }
            } catch (e: Exception) {
                // Surface dropped
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        // Ignore unlock failures on teardown
                    }
                }
            }
        }
    }
}
