package com.game.rps.controll.network;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.game.rps.controll.game.GameControllerImpl;
import com.game.rps.model.choice.Choice;
import com.game.rps.model.database.RPSDatabaseImpl;
import com.game.rps.model.player.PlayerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


class BluetoothConnectionService {
    public static final UUID MY_UUID = UUID.fromString("f368971d-7291-43c4-af4d-ad00c620161c");
    private static final String TAG = "BluetoothConnectionService";
    private static final String appName = "RPSGame";
    private final BluetoothAdapter adapter;
    Context context;
    ProgressDialog mProgressDialog;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice device;
    private UUID deviceUUID;
    private ConnectedThread connectedThread;

    public BluetoothConnectionService(Context context) {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.start();
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.acceptThread == null) {
            this.acceptThread = new AcceptThread();
            this.acceptThread.start();
        }
    }

    public synchronized void close() {
        Log.d(TAG, "restart");
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.acceptThread != null) {
            this.acceptThread.cancel();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     *
     * @param device device to connect
     * @param uuid   Universally Unique Identifier of application
     */
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");
        this.mProgressDialog = ProgressDialog.show(this.context, "Connecting Bluetooth", "Please Wait...", true);
        this.connectThread = new ConnectThread(device, uuid);
        this.connectThread.start();
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");
        this.connectedThread = new ConnectedThread(mmSocket);
        this.connectedThread.start();
    }

    /**
     * Writes to the connected device
     *
     * @param type    type of th message
     * @param message message to send
     * @return if write was successful - true, false - otherwise
     */
    public boolean write(MessageType type, String message) {
        Log.d(TAG, "write: Write Called.");
        return this.connectedThread.write(type, message);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(BluetoothConnectionService.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) BluetoothConnectionService.this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 4);
                }
                tmp = BluetoothConnectionService.this.adapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;
            try {
                Log.d(TAG, "run: RFCOM server socket start.....");
                socket = this.mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            if (socket != null) {
                BluetoothConnectionService.this.connected(socket);
            }
            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            BluetoothConnectionService.this.device = device;
            BluetoothConnectionService.this.deviceUUID = uuid;
            if (ActivityCompat.checkSelfPermission(BluetoothConnectionService.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) BluetoothConnectionService.this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 54);
            }
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");
            if (ActivityCompat.checkSelfPermission(BluetoothConnectionService.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) BluetoothConnectionService.this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 54);
            }
            try {

                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID);
                tmp = BluetoothConnectionService.this.device.createRfcommSocketToServiceRecord(BluetoothConnectionService.this.deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }
            this.mmSocket = tmp;
            BluetoothConnectionService.this.adapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                try {
                    this.mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID);
            }

            BluetoothConnectionService.this.connected(this.mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    /**
     * This Thread is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private final DataInputStream dataInputStream;
        private final DataOutputStream dataOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                BluetoothConnectionService.this.mProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            try {
                tmpIn = this.mmSocket.getInputStream();
                tmpOut = this.mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
            this.dataInputStream = new DataInputStream(this.mmInStream);
            this.dataOutputStream = new DataOutputStream(this.mmOutStream);
            this.write(MessageType.NAME, RPSDatabaseImpl.getInstance(BluetoothConnectionService.this.context).getPlayer().getName());
        }

        public void run() {
            int type;
            while (true) {
                try {
                    type = this.dataInputStream.readInt();
                    final String message = this.dataInputStream.readUTF();
                    final MessageType messageType;
                    switch (type) {
                        case 0:
                            messageType = MessageType.CHOICE;
                            break;
                        case 1:
                            messageType = MessageType.NAME;
                            break;
                        case 2:
                            messageType = MessageType.TEST;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + type);
                    }
                    Thread parseThread = new Thread(() -> this.parseRead(messageType, message));
                    Log.d(TAG, "InputStream: " + message + " " + type);
                    parseThread.start();
                    Log.d(TAG, "parse Stream occured");
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        public boolean write(MessageType messageType, String message) {
            Log.d(TAG, "write: Writing to outputstream: " + message);
            try {
                this.dataOutputStream.writeInt(messageType.ordinal());
                this.dataOutputStream.writeUTF(message);

            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());

                return false;
            }
            return true;
        }

        public void cancel() {
            try {
                if (GameControllerImpl.getController().getState() != com.game.rps.controll.game.State.NOT_CONNECTED) {
                    GameControllerImpl.getController().switchState(com.game.rps.controll.game.State.NOT_CONNECTED);
                }
                this.mmSocket.close();
            } catch (IOException ignored) {}
        }

        private void parseRead(MessageType type, String message) {
            switch (type) {

                case CHOICE:
                    while (true) {
                        if (GameControllerImpl.getController().getState() == com.game.rps.controll.game.State.CHOSEN) {
                            Choice opponentChoice = Choice.valueOf(message);
                            System.out.println(Thread.currentThread().getName());
                            GameControllerImpl.getController().performRound(opponentChoice);
                            break;
                        }
                        synchronized (this) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case NAME:
                    GameControllerImpl.getController().setOpponent(new PlayerImpl(message, 0, 0, 0));
                    ((Activity) BluetoothConnectionService.this.context).runOnUiThread(() -> Toast.makeText(BluetoothConnectionService.this.context, "You are connected!", Toast.LENGTH_SHORT).show());
                case TEST:
                    break;
            }
        }
    }

}
