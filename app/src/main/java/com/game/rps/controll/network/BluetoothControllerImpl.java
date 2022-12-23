package com.game.rps.controll.network;

import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.game.rps.controll.game.GameControllerImpl;
import com.game.rps.controll.game.State;
import com.game.rps.model.choice.Choice;

import java.util.ArrayList;
import java.util.List;

public class BluetoothControllerImpl implements BluetoothController {
    private static final String TAG = "BluetoothController";
    private static BluetoothControllerImpl instance;
    private final ArrayList<BluetoothDevice> mBTDevices;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Context context;
    private BluetoothDevice device;
    private BluetoothConnectionService service;

    private BluetoothControllerImpl(Context context) {
        this.mBTDevices = new ArrayList<>();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
    }

    public static BluetoothController getController(Context context) {
        if (instance == null)
            instance = new BluetoothControllerImpl(context);
        return instance;
    }

    public static BluetoothController getController() {
        return instance;
    }

    @Override
    public List<BluetoothDevice> getAllDevices() {
        Log.d(TAG, "Getting all devices");
        return this.mBTDevices;
    }

    @Override
    public void connect(BluetoothDevice device) {
        Log.d(TAG, "Connect");
        if (ContextCompat.checkSelfPermission(this.context, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.context, new String[]{BLUETOOTH_SCAN}, 2);
        }
        instance.mBluetoothAdapter.cancelDiscovery();
        instance.device = device;
        instance.device.createBond();
        instance.service = new BluetoothConnectionService(this.context);
        GameControllerImpl.getController().switchState(State.CONNECTED);
        this.service.startClient(device, BluetoothConnectionService.MY_UUID);
    }

    @Override
    public boolean isConnected() {
        Log.d(TAG, "is connected");
        if (instance.service != null)
            return instance.service.write(MessageType.TEST, "test");
        return false;
    }

    @Override
    public void sendChoiceToOpponent(Choice choice) {
        Log.d(TAG, "send choice to opponent");
        if (instance.service != null)
            instance.service.write(MessageType.CHOICE, choice.name());
    }

    @Override
    public void reset() {
        if (this.service != null) {
            this.service.close();
            this.service = null;
        }
        GameControllerImpl.getController().switchState(com.game.rps.controll.game.State.NOT_CONNECTED);
    }

    public void setDevice(BluetoothDevice device) {
        Log.d(TAG, "set device");
        instance.device = device;
    }

}
