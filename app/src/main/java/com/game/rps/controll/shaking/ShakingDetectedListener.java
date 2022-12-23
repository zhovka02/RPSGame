package com.game.rps.controll.shaking;

import android.hardware.SensorEvent;

public interface ShakingDetectedListener {
    /**
     * Invoked when a shaking event occurs with the phone.
     * @param event sensor event
     */
    void onShakingDetected (SensorEvent event);

}
