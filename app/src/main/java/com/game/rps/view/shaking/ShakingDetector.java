package com.game.rps.view.shaking;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.game.rps.controll.shaking.ShakingDetectedListener;

public class ShakingDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int IGNORE = 500;
    private static ShakingDetector INSTANCE;
    private ShakingDetectedListener mListener;
    private long mShakeTimestamp;

    private ShakingDetector() {
        this.mShakeTimestamp = 0;
        this.mListener = null;
    }

    /**
     * Returns the shaking detector instance.
     *
     * @return current shaking detector instance
     */
    public static ShakingDetector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShakingDetector();
        }
        return INSTANCE;
    }

    /**
     * Registers a new listener for the shaking detector
     *
     * @param listener a new listener for the shaking detector
     */
    public void setOnShakeListener(ShakingDetectedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (this.mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (this.mShakeTimestamp + IGNORE > now) {
                    return;
                }
                this.mShakeTimestamp = now;

                this.mListener.onShakingDetected(event);
            }
        }
    }
}
