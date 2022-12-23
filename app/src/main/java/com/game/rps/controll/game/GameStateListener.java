package com.game.rps.controll.game;

public interface GameStateListener {
    /**
     * Invoked when the state of the game (controller) changes and processes the request correctly.
     * @param state New state of the game (controller)
     */
    void onGameStateChanged(State state);
}
