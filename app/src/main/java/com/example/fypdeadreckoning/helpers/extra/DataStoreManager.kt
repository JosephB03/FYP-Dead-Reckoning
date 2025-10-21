package com.example.fypdeadreckoning.helpers.extra

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Extension property - accessible from any Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calibration_settings")

// Define all calibration keys in one place
object CalibrationKeys {
    // Step calibration
    val STRIDE_LENGTH_KEY = doublePreferencesKey("stride_length")
    val STEP_COUNTER_SENSITIVITY_KEY = doublePreferencesKey("step_counter_sensitivity")

    // Magnetometer calibration
    val MAG_X_KEY = doublePreferencesKey("mag_bias_x")
    val MAG_Y_KEY = doublePreferencesKey("mag_bias_y")
    val MAG_Z_KEY = doublePreferencesKey("mag_bias_z")

    // Gyroscope calibration
    val GYRO_X_KEY = doublePreferencesKey("gyro_bias_x")
    val GYRO_Y_KEY = doublePreferencesKey("gyro_bias_y")
    val GYRO_Z_KEY = doublePreferencesKey("gyro_bias_z")
}