package com.game.rps.controll.network;

import android.bluetooth.BluetoothDevice;

import com.game.rps.model.choice.Choice;

import java.util.List;

public interface BluetoothController {

    /**
     * Returns a list of available opponents to join
     *
     * @return list of available opponents to join
     */
    List<BluetoothDevice> getAllDevices();

    /**
     * Establishes a connection to an opponent via Bluetooth
     *
     * @param device opponent's device
     */
    void connect(BluetoothDevice device);

    /**
     * Returns the current connection status
     *
     * @return true if player is connected to opponent, false - otherwise
     */
    boolean isConnected();

    /**
     * Sends the current player choice to the opponent
     *
     * @param choice Choice of the opponent
     */
    void sendChoiceToOpponent(Choice choice);

    /**
     * Resets current connection
     */
    void reset();
}
