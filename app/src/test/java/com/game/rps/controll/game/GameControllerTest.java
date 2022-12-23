package com.game.rps.controll.game;

import android.hardware.SensorEvent;
import android.util.Log;
import android.view.KeyEvent;

import com.game.rps.model.choice.Choice;
import com.game.rps.view.ShakingDetectorTest;
import com.game.rps.view.keystroke.KeyStrokeDetector;
import com.game.rps.view.shaking.ShakingDetector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class GameControllerTest {

    private final KeyStrokeDetector keyStrokeDetector = KeyStrokeDetector.getInstance();

    private final ShakingDetector shakingDetector = ShakingDetector.getInstance();

    private final GameController controller = GameControllerImpl.getController();

    private State currState;

    private final GameStateListener listener = state -> GameControllerTest.this.currState = state;


    @Before
    public void setUp() {
        // We need it in order to mock Log.class
        PowerMockito.mockStatic(Log.class);
        this.controller.setListener(this.listener);
    }

    @After
    public void tearUp() {
        this.controller.restart();
        this.currState = State.NOT_CONNECTED;
    }

    @Test
    public void testOfThreeConditions() {
        // The first condition is shaking
        SensorEvent sensorEvent = ShakingDetectorTest.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.shakingDetector.onSensorChanged(sensorEvent);

        // The second condition is to press the key
        this.keyStrokeDetector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        this.keyStrokeDetector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
        // The third condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);

        // Let's see if we're ready for the round
        Assert.assertTrue(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyTwoConditions1() {
        // The first condition is shaking
        SensorEvent sensorEvent = ShakingDetectorTest.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.shakingDetector.onSensorChanged(sensorEvent);

        // The second condition is to press the key
        this.keyStrokeDetector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        this.keyStrokeDetector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyTwoConditions2() {
        // The first condition is shaking
        SensorEvent sensorEvent = ShakingDetectorTest.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.shakingDetector.onSensorChanged(sensorEvent);

        // The second condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyTwoConditions3() {
        // The first condition is to press the key
        this.keyStrokeDetector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        this.keyStrokeDetector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));

        // The second condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyOneCondition1() {
        // The first condition is shaking
        SensorEvent sensorEvent = ShakingDetectorTest.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.shakingDetector.onSensorChanged(sensorEvent);

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyOneCondition2() {
        // The first condition is to press the key
        this.keyStrokeDetector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        this.keyStrokeDetector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfOnlyOneCondition3() {
        // The first condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);

        // Let's see if we're ready for the round
        Assert.assertFalse(this.controller.isReadyToPerformRound());
    }

    @Test
    public void testOfStateConnected() {
        // The first condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);

        // Let's see if the listener has been notified
        Assert.assertEquals(State.CONNECTED, this.currState);
    }

    @Test
    public void testOfStateChosen() {
        // The first condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);
        // The second condition is shaking
        SensorEvent sensorEvent = ShakingDetectorTest.createSensorEvent(new float[]{-34.171677f, -12.624118f, -12.745705f});
        this.shakingDetector.onSensorChanged(sensorEvent);

        // The third condition is to press the key
        this.keyStrokeDetector.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        // let's wait a little bit for the detector to process the press
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
        this.keyStrokeDetector.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
        // Let's see if the listener has been notified
        Assert.assertEquals(State.CHOSEN, this.currState);
    }

    @Test
    public void testOfStateNotConnected() {
        // The first condition is to be connected to opponent
        this.controller.switchState(State.CONNECTED);
        // But suddenly the connection is broken
        this.controller.switchState(State.NOT_CONNECTED);

        // Let's see if the listener has been notified
        Assert.assertEquals(State.NOT_CONNECTED, this.currState);
    }

    @Test
    public void testOfStateWin() {
        // To be ready for performing of round
        this.testOfThreeConditions();
        // Let's perform it
        this.controller.performRound(Choice.ROCK);
        // Let's see if the listener has been notified
        Assert.assertEquals(State.WIN, this.currState);
    }

    @Test
    public void testOfStateLose() {
        // To be ready for performing of round
        this.testOfThreeConditions();
        // Let's perform it
        this.controller.performRound(Choice.SCISSORS);
        // Let's see if the listener has been notified
        Assert.assertEquals(State.LOSE, this.currState);
    }

    @Test
    public void testOfStateDraw() {
        // To be ready for performing of round
        this.testOfThreeConditions();
        // Let's perform it
        this.controller.performRound(Choice.PAPER);
        // Let's see if the listener has been notified
        Assert.assertEquals(State.DRAW, this.currState);
    }
}
