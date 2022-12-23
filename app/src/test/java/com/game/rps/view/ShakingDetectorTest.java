package com.game.rps.view;

import android.hardware.SensorEvent;

import com.game.rps.controll.shaking.ShakingDetectedListener;
import com.game.rps.view.shaking.ShakingDetector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class ShakingDetectorTest {
    private final ShakingDetector detector = ShakingDetector.getInstance();

    private final ShakingDetectedListener listener = event -> this.shakingDetected = true;

    boolean shakingDetected;

    @Before
    public void setUP(){
        this.shakingDetected = false;
        this.detector.setOnShakeListener(this.listener);
    }

    public static SensorEvent createSensorEvent(float[] values){
        // let emulate the phone shaking
        SensorEvent sensorEvent = Mockito.mock(SensorEvent.class);

        try {
            Field valuesField = SensorEvent.class.getField("values");
            valuesField.setAccessible(true);
            try {
                valuesField.set(sensorEvent, values);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return sensorEvent;
    }

    @Test
    public void testShakingOne() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{33.108765f, 16.720251f, 9.176687f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testShakingTwo() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testShakingThree() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{-42.578167f, -16.329145f, -5.8412395f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testShakingFour() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{-45.377575f, -17.294277f, -0.80301523f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testShakingFive() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{-29.844797f, -16.7893f, 8.046922f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testShakingSix() throws InterruptedException {
        // let emulate the phone shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{73.45789f, 30.338337f, 1.3146274f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertTrue(this.shakingDetected);
    }

    @Test
    public void testNoShakingOne() throws InterruptedException {
        // let emulate the absence of shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{-23.494904f, -0.48023856f, 4.4422493f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertFalse(this.shakingDetected);
    }

    @Test
    public void testNoShakingTwo() throws InterruptedException {
        // let emulate the absence of shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{7.9917116f, 10.132884f, 1.2946981f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertFalse(this.shakingDetected);
    }

    @Test
    public void testNoShakingThree() throws InterruptedException {
        // let emulate the absence of shaking
        SensorEvent sensorEvent = this.createSensorEvent(new float[]{0.5684601f, 7.9749036f, -2.4323268f});
        this.detector.onSensorChanged(sensorEvent);
        // let the detector process the shaking
        Thread.sleep(500);
        // let check the result of processing
        Assert.assertFalse(this.shakingDetected);
    }
}
