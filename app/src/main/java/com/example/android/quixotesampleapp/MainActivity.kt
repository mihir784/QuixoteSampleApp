package com.example.android.quixotesampleapp

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.android.quixotesampleapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagnetometer: Sensor
    private lateinit var screenDisplay: Display
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private var rotation = true
    private var pitch: MutableLiveData<Float> = MutableLiveData(Float.NaN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpSensors()
        setOrientation()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            this,
            sensorMagnetometer,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun setUpSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        screenDisplay = wm.defaultDisplay
    }

    private fun setOrientation() {
        pitch.observe(this, Observer {
            if (it > -0.78f && it < 0.78f) {
                binding.btTopLeft.visibility = View.VISIBLE
                binding.btTopRight.visibility = View.VISIBLE
                binding.btBottomLeft.visibility = View.VISIBLE
                binding.btBottomRight.visibility = View.VISIBLE
                if (rotation) {
                    binding.ivArrow.rotation = binding.ivArrow.rotation + 1
                }
            } else {
                binding.btTopLeft.visibility = View.GONE
                binding.btTopRight.visibility = View.GONE
                binding.btBottomLeft.visibility = View.GONE
                binding.btBottomRight.visibility = View.GONE
                binding.ivArrow.rotation = 0f
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnClickListeners() {
        var topLeftClicked = false
        var topRightClicked = false
        var bottomLeftClicked = false
        var bottomRightClicked = false

        binding.btTopLeft.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (topLeftClicked) {
                        binding.ivArrow.rotation = 135f
                    } else {
                        binding.ivArrow.rotation = 315f
                    }
                    rotation = false
                }
                MotionEvent.ACTION_UP -> {
                    rotation = true
                    topLeftClicked = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        topLeftClicked = false
                    }, 500)
                }
            }
            return@setOnTouchListener true
        }

        binding.btTopRight.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (topRightClicked) {
                        binding.ivArrow.rotation = 225f
                    } else {
                        binding.ivArrow.rotation = 45f
                    }
                    rotation = false
                }
                MotionEvent.ACTION_UP -> {
                    rotation = true
                    topRightClicked = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        topRightClicked = false
                    }, 500)
                }
            }
            return@setOnTouchListener true
        }

        binding.btBottomRight.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (bottomRightClicked) {
                        binding.ivArrow.rotation = 315f
                    } else {
                        binding.ivArrow.rotation = 135f
                    }
                    rotation = false
                }
                MotionEvent.ACTION_UP -> {
                    rotation = true
                    bottomRightClicked = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        bottomRightClicked = false
                    }, 500)
                }
            }
            return@setOnTouchListener true
        }

        binding.btBottomLeft.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (bottomLeftClicked) {
                        binding.ivArrow.rotation = 45f
                    } else {
                        binding.ivArrow.rotation = 225f
                    }
                    rotation = false
                }
                MotionEvent.ACTION_UP -> {
                    rotation = true
                    bottomLeftClicked = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        bottomLeftClicked = false
                    }, 500)
                }
            }
            return@setOnTouchListener true
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {

        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values.clone()
            else -> return
        }
        val rotationOk = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        var rotationMatrixAdjusted = FloatArray(9)
        when (screenDisplay.rotation) {
            Surface.ROTATION_0 -> rotationMatrixAdjusted = rotationMatrix.clone()
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                rotationMatrixAdjusted
            )
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                rotationMatrixAdjusted
            )
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                rotationMatrixAdjusted
            )
        }

        val orientationValues = FloatArray(3)
        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrixAdjusted, orientationValues)
        }

        pitch.postValue(orientationValues[1])
    }

}