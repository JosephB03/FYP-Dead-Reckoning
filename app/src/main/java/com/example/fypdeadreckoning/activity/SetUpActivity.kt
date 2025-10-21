package com.example.fypdeadreckoning.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.example.fypdeadreckoning.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.fypdeadreckoning.helpers.extra.dataStore
import com.example.fypdeadreckoning.helpers.extra.CalibrationKeys

class SetUpActivity: AppCompatActivity() {
    private var TAG = "SetUpActivity"

    private var beginText: TextView? = null
    private var strideButton: Button? = null
    private var beginButton: Button? = null

    private val strideLengthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val strideLength = data.getDoubleExtra("stride_length", 0.0)
                val preferredSensitivity = data.getDoubleExtra("preferred_sensitivity", 0.0)

                Log.d(TAG, "Stride length: $strideLength")
                Log.d(TAG, "Preferred step sensitivity: $preferredSensitivity")

                saveStepCalibration(strideLength, preferredSensitivity)
            }
        }
    }

    private val magneticCompassLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val magBiasX = data.getDoubleExtra("mag_bias_x", 0.0)
                val magBiasY = data.getDoubleExtra("mag_bias_y", 0.0)
                val magBiasZ = data.getDoubleExtra("mag_bias_z", 0.0)

               Log.d(TAG, "Mag X: $magBiasX")
               Log.d(TAG, "Mag Y: $magBiasY")
               Log.d(TAG, "Mag Z: $magBiasZ")

                saveCompassCalibration(magBiasX, magBiasY, magBiasZ)
            }
        }
    }

    private val gyroscopeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val gyroBiasX = data.getDoubleExtra("gyro_bias_x", 0.0)
                val gyroBiasY = data.getDoubleExtra("gyro_bias_y", 0.0)
                val gyroBiasZ = data.getDoubleExtra("gyro_bias_z", 0.0)

                Log.d(TAG, "Gyro X: $gyroBiasX")
                Log.d(TAG, "Gyro Y: $gyroBiasY")
                Log.d(TAG, "Gyro Z: $gyroBiasZ")


                saveGyroscopeCalibration(gyroBiasX, gyroBiasY, gyroBiasZ)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        beginText = findViewById<View?>(R.id.beginText) as TextView
        strideButton = findViewById<View?>(R.id.strideButton) as Button
        beginButton = findViewById<View?>(R.id.beginButton) as Button

        strideButton!!.setOnClickListener {
            Log.d(TAG, "Stride calibration button pressed.")
            val intent = Intent(this@SetUpActivity, StrideLengthActivity::class.java)
            strideLengthLauncher.launch(intent)
        }

        beginButton!!.setOnClickListener {
            Log.d(TAG, "Begin button pressed.")

            lifecycleScope.launch {
                // Read all calibration values
                val preferences = dataStore.data.first()
                val strideLength = preferences[CalibrationKeys.STRIDE_LENGTH_KEY] ?: -1.0
                val sensitivity = preferences[CalibrationKeys.STEP_COUNTER_SENSITIVITY_KEY] ?: -1.0

                Log.d(TAG, "Retrieved stride length: $strideLength")

                // Check if all are calibrated
                if (strideLength == -1.0 || sensitivity == -1.0) {
                        Toast.makeText(this@SetUpActivity, "Please calibrate all sensors", Toast.LENGTH_SHORT).show()
                } else {
                    // All calibrated - proceed to main activity
                    val intent = Intent(this@SetUpActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun saveStepCalibration(strideLength: Double, sensitivity: Double) {
        lifecycleScope.launch {
            dataStore.edit { preferences ->
                preferences[CalibrationKeys.STRIDE_LENGTH_KEY] = strideLength
                preferences[CalibrationKeys.STEP_COUNTER_SENSITIVITY_KEY] = sensitivity
            }
            Log.d(TAG, "Stride length and sensitivity saved to DataStore")
        }
    }

    private fun saveCompassCalibration(magBiasX: Double, magBiasY: Double, magBiasZ: Double) {
        lifecycleScope.launch {
            dataStore.edit { preferences ->
                preferences[CalibrationKeys.MAG_X_KEY] = magBiasX
                preferences[CalibrationKeys.MAG_Y_KEY] = magBiasY
                preferences[CalibrationKeys.MAG_Z_KEY] = magBiasZ
            }
            Log.d(TAG, "Magnetic bias in X, Y, Z saved to DataStore")
        }
    }

    private fun saveGyroscopeCalibration(gyroBiasX: Double, gyroBiasY: Double, gyroBiasZ: Double) {
        lifecycleScope.launch {
            dataStore.edit { preferences ->
                preferences[CalibrationKeys.GYRO_X_KEY] = gyroBiasX
                preferences[CalibrationKeys.GYRO_Y_KEY] = gyroBiasY
                preferences[CalibrationKeys.GYRO_Z_KEY] = gyroBiasZ
            }
            Log.d(TAG, "Gyroscope bias in X, Y, Z saved to DataStore")
        }
    }

}