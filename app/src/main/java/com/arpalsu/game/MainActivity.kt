package com.arpalsu.game

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast

class MainActivity : Activity(), SensorEventListener {
    private lateinit var wv: WebView
    private var sm: SensorManager? = null
    private var rot: Sensor? = null
    private val mat = FloatArray(9)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        wv = WebView(this)
        wv.settings.javaScriptEnabled = true
        wv.settings.mediaPlaybackRequiresUserGesture = false
        wv.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
        }
        setContentView(wv)
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        rot = sm?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rot == null) Toast.makeText(this, "HP ini tidak punya sensor rotasi", Toast.LENGTH_LONG).show()
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) load()
        else requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
    }

    private fun load() {
        val html = assets.open("index.html").bufferedReader().readText()
        wv.loadDataWithBaseURL("https://ar.local/", html, "text/html", "utf-8", null)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, r: IntArray) {
        load()
    }

    override fun onResume() {
        super.onResume()
        rot?.let { sm?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sm?.unregisterListener(this)
    }

    override fun onSensorChanged(e: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(mat, e.values)
        wv.evaluateJavascript("setRot(" + mat.joinToString(",") + ")", null)
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}
