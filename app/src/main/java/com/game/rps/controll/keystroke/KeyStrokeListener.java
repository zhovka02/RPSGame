package com.game.rps.controll.keystroke;

public interface KeyStrokeListener {
    /**
     * Called when a volume key down event has occurred.
     */
    void onKeyDown();
    /**
     * Called when a volume key up event has occurred.
     */
    void onKeyUp();
    /**
     * Called when a volume key both (up and down) event has occurred.
     */
    void onKeyBoth();
}
