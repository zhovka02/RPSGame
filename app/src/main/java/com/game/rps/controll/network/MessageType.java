package com.game.rps.controll.network;

/**
 * represents possible types of messages
 */
enum MessageType {
    /**
     * the player makes a choice
     */
    CHOICE,
    /**
     * the player tells his name
     */
    NAME,
    /**
     * player checks opponent's availability
     */
    TEST
}
