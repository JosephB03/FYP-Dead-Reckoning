package com.example.fypdeadreckoning.activity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fypdeadreckoning.R
import com.example.fypdeadreckoning.helpers.extra.ExtraFunctions
import com.example.fypdeadreckoning.helpers.steps.DynamicStepCounter
import com.example.fypdeadreckoning.helpers.dialog.StepCalibrationDialogFragment
import com.example.fypdeadreckoning.interfaces.OnPreferredStepCounterListener
import java.lang.String
import java.util.Locale
import kotlin.Array
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.arrayOf
import kotlin.arrayOfNulls
import kotlin.text.format

class StrideLengthActivity : AppCompatActivity(), SensorEventListener, OnPreferredStepCounterListener {
    private var TAG = "StrideLengthActivity"

    private var textAndroidSteps: TextView? = null
    private var inputDistance: EditText? = null
    private var textLinearAcceleration: TextView? = null

    private var buttonStartCalibration: Button? = null
    private var buttonStopCalibration: Button? = null
    private var buttonSetStrideLength: Button? = null

    private var linearAcceleration: Sensor? = null
    private var androidStepCounter: Sensor? = null
    private var sensorManager: SensorManager? = null

    private lateinit var dynamicStepCounters: Array<DynamicStepCounter>

    // Step counter variables
    private var androidStepCount = 0
    private var androidStepCountTotal = 0
    private var androidStepCountInitial = 0
    private var stepCount = 0
    private var preferredStepCounterIndex = 0

    private var wasRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stride_calibration)

        dynamicStepCounters = arrayOf(
            DynamicStepCounter(),
            DynamicStepCounter(),
            DynamicStepCounter(),
            DynamicStepCounter(),
            DynamicStepCounter()
        )

        var sensitivity = 0.5
        for (i in dynamicStepCounters.indices) {
            dynamicStepCounters[i] = DynamicStepCounter(sensitivity)
            sensitivity += 0.05
        }

        //defining views
        textAndroidSteps = findViewById(R.id.textCalibrateSteps)
        inputDistance = findViewById(R.id.inputDistance)
        textLinearAcceleration = findViewById(R.id.calibrateLinearAcceleration)

        buttonStartCalibration = findViewById(R.id.startButton)
        buttonStopCalibration = findViewById(R.id.stopButton)
        buttonSetStrideLength = findViewById(R.id.setStrideButton)

        //defining sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        linearAcceleration = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        androidStepCounter = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        //activate sensors when start button is pressed
        buttonStartCalibration!!.setOnClickListener {
            sensorManager!!.registerListener(
                this@StrideLengthActivity,
                linearAcceleration,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            sensorManager!!.registerListener(
                this@StrideLengthActivity,
                androidStepCounter,
                SensorManager.SENSOR_DELAY_FASTEST
            )

            buttonStartCalibration!!.isEnabled = false
            buttonSetStrideLength!!.isEnabled = false
            buttonStopCalibration!!.isEnabled = true

            wasRunning = true
        }

        //deactivate sensors when stop button is pressed and open step_counters dialog
        buttonStopCalibration!!.setOnClickListener {
            sensorManager!!.unregisterListener(this@StrideLengthActivity)

            val stepCounts = arrayOfNulls<String>(dynamicStepCounters.size)
            for (i in stepCounts.indices) stepCounts[i] = String.format(
                Locale.UK,
                "Sensitivity: %.2f :: Step Count: %d",
                dynamicStepCounters[i].sensitivity,
                dynamicStepCounters[i].stepCount
            ) as String?


            //creating dialog, setting the stepCounts list, and setting a handler
            val stepCalibrationDialogFragment = StepCalibrationDialogFragment()
            stepCalibrationDialogFragment.setOnPreferredStepCounterListener(this@StrideLengthActivity)
            stepCalibrationDialogFragment.setStepList(stepCounts)
            stepCalibrationDialogFragment.show(supportFragmentManager, "step_counters")

            buttonStartCalibration!!.isEnabled = true
            buttonSetStrideLength!!.isEnabled = true
            buttonStopCalibration!!.isEnabled = false

            wasRunning = false
        }

        //when the button is pressed, determine the strideLength by dividing stepsTaken
        //by distanceTraveled, and stored stride length in StepCountActivity
        // TODO: Come up with a default stride length
        // TODO: Add clear functionality
        buttonSetStrideLength!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val strideLength: Double
                if (stepCount != 0) {
                    strideLength = inputDistance!!.text.toString().toInt()
                        .toDouble() / stepCount
                    Log.d(TAG, "Steps taken:: $stepCount")
                    Log.d(TAG, "Stride length: $strideLength")
                } else {
                    Toast.makeText(application, "Take a few steps first!", Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                val strideLengthStr = kotlin.String.format(Locale.UK, "%.2f", strideLength)
                Toast.makeText(
                    applicationContext,
                    "Stride length set: $strideLengthStr m/step",
                    Toast.LENGTH_SHORT
                ).show()

                //returns the stride_length and preferred_step_counter info to the calling activity
                val myIntent = intent
                myIntent.putExtra("stride_length", strideLength)
                myIntent.putExtra(
                    "preferred_step_counter",
                    dynamicStepCounters[preferredStepCounterIndex].sensitivity
                )
                setResult(RESULT_OK, myIntent)
                finish()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        sensorManager!!.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()

        if (wasRunning) {
            sensorManager!!.registerListener(
                this@StrideLengthActivity,
                linearAcceleration,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            sensorManager!!.registerListener(
                this@StrideLengthActivity,
                androidStepCounter,
                SensorManager.SENSOR_DELAY_FASTEST
            )

            buttonStartCalibration!!.isEnabled = false
            buttonSetStrideLength!!.isEnabled = false
            buttonStopCalibration!!.isEnabled = true
        } else {
            buttonStartCalibration!!.isEnabled = true
            buttonSetStrideLength!!.isEnabled = true
            buttonStopCalibration!!.isEnabled = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (androidStepCountInitial == 0) {
                // This is the total count since reboot. Save it as the starting point.
                androidStepCountInitial = event.values[0].toInt()
            }
            androidStepCountTotal = event.values[0].toInt()
            androidStepCount = androidStepCountTotal - androidStepCountInitial
            textAndroidSteps!!.text = androidStepCount.toString()
        } else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val norm: Float = ExtraFunctions.calcNorm(
                event.values[0] +
                        event.values[1] +
                        event.values[2]
            )

            val linearAcceleration = event.values[2].toString()
            textLinearAcceleration!!.text = if (linearAcceleration.length <= 5) linearAcceleration else linearAcceleration.substring(0, 5)

            for (dynamicStepCounter in dynamicStepCounters) dynamicStepCounter.findStep(norm)
        }
    }

    override fun onPreferredStepCounter(preferredStepCounterIndex: Int) {
        this.preferredStepCounterIndex = preferredStepCounterIndex
        this.stepCount = dynamicStepCounters[preferredStepCounterIndex].stepCount
        this.textAndroidSteps!!.text = this.stepCount.toString()
    }
}