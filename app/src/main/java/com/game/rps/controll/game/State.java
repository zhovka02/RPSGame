package com.game.rps.controll.game;

/**
 * Represents possible game states.
 */
public enum State {
    /**
     * No connection to the opponent has been established
     */
    NOT_CONNECTED,
    /**
     * Connection to the opponent is established
     */
    CONNECTED,
    /**
     * Choice is correctly made by pressing keys and shaking
     */
    CHOSEN,
    /**
     * The current player won
     */
    WIN,
    /**
     * The current player has lost
     */
    LOSE,
    /**
     * A draw has taken place
     */
    DRAW
}
