package com.example.sensorapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Sensor App - Monitor device sensors
 *
 * Features:
 * - Accelerometer (X, Y, Z axes)
 * - Light Sensor (Lux readings)
 * - Proximity Sensor (Distance)
 * - Real-time updates
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // UI Components
    private TextView accelXValue, accelYValue, accelZValue;
    private TextView lightValue, lightStatus;
    private TextView proximityValue, proximityStatus;
    private TextView sensorInfo;

    // Sensor Manager
    private SensorManager sensorManager;

    // Sensors
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        initializeViews();

        // Setup sensors
        setupSensors();

        // Display sensor info
        displaySensorInfo();
    }

    /**
     * Find all UI components
     */
    private void initializeViews() {
        accelXValue = findViewById(R.id.accelXValue);
        accelYValue = findViewById(R.id.accelYValue);
        accelZValue = findViewById(R.id.accelZValue);
        lightValue = findViewById(R.id.lightValue);
        lightStatus = findViewById(R.id.lightStatus);
        proximityValue = findViewById(R.id.proximityValue);
        proximityStatus = findViewById(R.id.proximityStatus);
        sensorInfo = findViewById(R.id.sensorInfo);
    }

    /**
     * Setup all sensors
     */
    private void setupSensors() {
        // Get SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            Toast.makeText(this, "Sensor Manager not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get individual sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Register listeners
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Display available sensor information
     */
    private void displaySensorInfo() {
        StringBuilder info = new StringBuilder();

        // Accelerometer info
        if (accelerometer != null) {
            info.append("✓ Accelerometer: ").append(accelerometer.getName()).append("\n");
        } else {
            info.append("✗ Accelerometer: NOT AVAILABLE\n");
        }

        // Light Sensor info
        if (lightSensor != null) {
            info.append("✓ Light Sensor: ").append(lightSensor.getName()).append("\n");
        } else {
            info.append("✗ Light Sensor: NOT AVAILABLE\n");
        }

        // Proximity Sensor info
        if (proximitySensor != null) {
            info.append("✓ Proximity Sensor: ").append(proximitySensor.getName()).append("\n");
        } else {
            info.append("✗ Proximity Sensor: NOT AVAILABLE\n");
        }

        info.append("\nUpdate Rate: ").append(formatDelay(SensorManager.SENSOR_DELAY_NORMAL));

        sensorInfo.setText(info.toString());
    }

    /**
     * Called when sensor value changes
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                handleAccelerometer(event);
                break;

            case Sensor.TYPE_LIGHT:
                handleLightSensor(event);
                break;

            case Sensor.TYPE_PROXIMITY:
                handleProximitySensor(event);
                break;
        }
    }

    /**
     * Handle accelerometer data
     *
     * @param event SensorEvent with X, Y, Z acceleration values
     */
    private void handleAccelerometer(SensorEvent event) {
        // event.values[0] = X axis
        // event.values[1] = Y axis
        // event.values[2] = Z axis

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Update UI (format to 2 decimal places)
        accelXValue.setText(String.format("%.2f m/s²", x));
        accelYValue.setText(String.format("%.2f m/s²", y));
        accelZValue.setText(String.format("%.2f m/s²", z));

        // Calculate magnitude (total acceleration)
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        // Optional: Detect shake
        if (magnitude > 20) {
            // Strong movement detected
            // You can add logic here (e.g., shake detection)
        }
    }

    /**
     * Handle light sensor data
     *
     * @param event SensorEvent with lux value
     */
    private void handleLightSensor(SensorEvent event) {
        // event.values[0] = illuminance in lux
        float lux = event.values[0];

        // Update UI
        lightValue.setText(String.format("%.2f lux", lux));

        // Determine light status
        String status;
        if (lux < 10) {
            status = "🌑 Very Dark";
        } else if (lux < 100) {
            status = "🌙 Dark";
        } else if (lux < 1000) {
            status = "🌤️ Normal";
        } else if (lux < 10000) {
            status = "☀️ Bright";
        } else {
            status = "☀️☀️ Very Bright";
        }

        lightStatus.setText("Status: " + status);
    }

    /**
     * Handle proximity sensor data
     *
     * @param event SensorEvent with distance value
     */
    private void handleProximitySensor(SensorEvent event) {
        // event.values[0] = distance in cm (usually 0 = close, max = far)
        float distance = event.values[0];

        // Update UI
        proximityValue.setText(String.format("%.2f cm", distance));

        // Determine proximity status
        String status;
        if (distance < 5) {
            status = "🔴 VERY CLOSE";
        } else if (distance < 10) {
            status = "🟠 CLOSE";
        } else {
            status = "🟢 FAR";
        }

        proximityStatus.setText("Status: " + status);
    }

    /**
     * Called when sensor accuracy changes
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
        // accuracy values: SensorManager.SENSOR_STATUS_UNRELIABLE (0),
        //                  SensorManager.SENSOR_STATUS_ACCURACY_LOW (1),
        //                  SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM (2),
        //                  SensorManager.SENSOR_STATUS_ACCURACY_HIGH (3)
    }

    /**
     * Convert delay constant to readable format
     */
    private String formatDelay(int delay) {
        switch (delay) {
            case SensorManager.SENSOR_DELAY_FASTEST:
                return "Fastest (0ms)";
            case SensorManager.SENSOR_DELAY_GAME:
                return "Game (20ms)";
            case SensorManager.SENSOR_DELAY_UI:
                return "UI (60ms)";
            case SensorManager.SENSOR_DELAY_NORMAL:
                return "Normal (200ms)";
            default:
                return "Unknown";
        }
    }

    /**
     * Cleanup when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister sensor listeners to save battery
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Cleanup when activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to save power
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Resume listening when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Re-register listeners
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null && sensorManager != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}