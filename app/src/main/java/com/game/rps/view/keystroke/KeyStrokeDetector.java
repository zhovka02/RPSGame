package com.game.rps.view.keystroke;

import android.view.KeyEvent;

import com.game.rps.controll.keystroke.KeyStrokeListener;

public class KeyStrokeDetector implements KeyEvent.Callback {


    private static KeyStrokeDetector INSTANCE;
    private KeyStrokeListener mListener;

    private boolean up;
    private boolean down;


    private KeyStrokeDetector() {
        this.up = false;
        this.down = false;
        this.mListener = null;
        Notificator notificator = new Notificator();
        notificator.start();
    }

    /**
     * Returns the keystroke detector instance.
     *
     * @return current keystroke detector instance
     */
    public static KeyStrokeDetector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeyStrokeDetector();
        }
        return INSTANCE;
    }

    /**
     * Registers a new listener for the keystroke  detector
     *
     * @param listener a new listener for the keystroke  detector
     */
    public void setOnKeyStrokeListener(KeyStrokeListener listener) {
        this.mListener = listener;
    }

    /**
     * Called when a key down event has occurred.
     *
     * @param keyCode The value in event.getKeyCode().
     * @param event   Description of the key event.
     * @return true
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        assert this.mListener != null;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            this.down = true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            this.up = true;
        }
        return true;
    }

    /**
     * Called when a key up event has occurred.
     *
     * @param keyCode  The value in event.getKeyCode().
     * @param keyEvent Description of the key event.
     * @return true
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && this.mListener != null) {
            this.down = false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && this.mListener != null) {
            this.up = false;
        }
        return true;
    }

    @Override
    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        return false;
    }


    @Override
    public boolean onKeyMultiple(int i, int i1, KeyEvent keyEvent) {
        return false;
    }

    /**
     * auxiliary class for handling key presses
     */
    private class Notificator extends Thread {
        @Override
        public void run() {
            while (true) {
                if (KeyStrokeDetector.this.up && KeyStrokeDetector.this.down)
                    KeyStrokeDetector.this.mListener.onKeyBoth();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (KeyStrokeDetector.this.up && KeyStrokeDetector.this.down)
                    KeyStrokeDetector.this.mListener.onKeyBoth();
                else if (KeyStrokeDetector.this.up)
                    KeyStrokeDetector.this.mListener.onKeyUp();
                else if (KeyStrokeDetector.this.down)
                    KeyStrokeDetector.this.mListener.onKeyDown();

            }
        }
    }

}
