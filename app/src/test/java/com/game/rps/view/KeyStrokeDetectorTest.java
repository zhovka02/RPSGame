package com.game.rps.view;

import android.view.KeyEvent;

import com.game.rps.controll.keystroke.KeyStrokeListener;
import com.game.rps.view.keystroke.KeyStrokeDetector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KeyStrokeDetectorTest {
    private final KeyStrokeDetector detector = KeyStrokeDetector.getInstance();

    private final KeyStrokeListener listener = new KeyStrokeListener() {
        @Override
        public void onKeyDown() {
            KeyStrokeDetectorTest.this.volumeDown = true;
            KeyStrokeDetectorTest.this.volumeUpDown = false;
            KeyStrokeDetectorTest.this.volumeUp = false;
        }

        @Override
        public void onKeyUp() {
            KeyStrokeDetectorTest.this.volumeDown = false;
            KeyStrokeDetectorTest.this.volumeUpDown = false;
            KeyStrokeDetectorTest.this.volumeUp = true;
        }

        @Override
        public void onKeyBoth() {
            KeyStrokeDetectorTest.this.volumeDown = false;
            KeyStrokeDetectorTest.this.volumeUpDown = true;
            KeyStrokeDetectorTest.this.volumeUp = false;
        }
    };

    private boolean volumeDown;

    private boolean volumeUp;

    private boolean volumeUpDown;

    @Before
    public void setUP(){
        this.volumeDown = false;
        this.volumeUp = false;
        this.volumeUpDown = false;
        this.detector.setOnKeyStrokeListener(this.listener);
    }

    @Test
    public void testVolumeDown(){
        // emulation of just pressing the volume key down
        this.detector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}
        // emulation of just releasing the volume key down
        this.detector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
        // Check the press processing
        Assert.assertTrue(this.volumeDown);
        Assert.assertFalse(this.volumeUp);
        Assert.assertFalse(this.volumeUpDown);
        // let's wait a little bit for the detector to process the release
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    @Test
    public void testVolumeUp(){
        // emulation of just pressing the volume key up
        this.detector.onKeyDown(KeyEvent.KEYCODE_VOLUME_UP,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}
        // emulation of just releasing the volume key up
        this.detector.onKeyUp(KeyEvent.KEYCODE_VOLUME_UP,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP));
        // Check the press processing
        Assert.assertFalse(this.volumeDown);
        Assert.assertTrue(this.volumeUp);
        Assert.assertFalse(this.volumeUpDown);
        // let's wait a little bit for the detector to process the release
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    @Test
    public void testVolumeBoth(){
        // emulation of just pressing the volume key up
        this.detector.onKeyDown(KeyEvent.KEYCODE_VOLUME_UP,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
        // emulation of just pressing the volume key down
        this.detector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}
        // emulation of just releasing the volume key up
        this.detector.onKeyUp(KeyEvent.KEYCODE_VOLUME_UP,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP));
        // emulation of just releasing the volume key down
        this.detector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
        // Check the press processing
        Assert.assertFalse(this.volumeDown);
        Assert.assertFalse(this.volumeUp);
        Assert.assertTrue(this.volumeUpDown);
        // let's wait a little bit for the detector to process the release
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }
}
