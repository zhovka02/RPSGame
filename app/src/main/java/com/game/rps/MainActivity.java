package com.game.rps;

import static android.Manifest.permission.BLUETOOTH_CONNECT;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.game.rps.controll.game.GameControllerImpl;
import com.game.rps.controll.network.BluetoothControllerImpl;
import com.game.rps.databinding.ActivityMainBinding;
import com.game.rps.model.database.RPSDatabaseImpl;
import com.game.rps.view.keystroke.KeyStrokeDetector;
import com.game.rps.view.shaking.ShakingDetector;
import com.game.rps.view.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver mBroadcastReceiverForOnOffBT = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(SettingsFragment.class.getName(), "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(SettingsFragment.class.getName(), "BroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(SettingsFragment.class.getName(), "BroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(SettingsFragment.class.getName(), "BroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiverForDiscoverable = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(SettingsFragment.class.getName(), "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(SettingsFragment.class.getName(), "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(SettingsFragment.class.getName(), "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(SettingsFragment.class.getName(), "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(SettingsFragment.class.getName(), "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    private KeyStrokeDetector detector;
    private BluetoothControllerImpl bluetoothController;
    private final BroadcastReceiver mBroadcastReceiverForPairing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 22);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(SettingsFragment.class.getName(), "BroadcastReceiver: BOND_BONDED.");
                    MainActivity.this.bluetoothController.setDevice(mDevice);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(SettingsFragment.class.getName(), "BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(SettingsFragment.class.getName(), "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver3;

    protected void setBarStyles() {
        this.getWindow().setStatusBarColor(this.getResources().getColor(R.color.color_status_bar, this.getTheme()));

        ActionBar bar = this.getSupportActionBar();
        Drawable d = this.getResources().getDrawable(R.drawable.navigation_background);
        if (bar != null) {
            bar.setBackgroundDrawable(d);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.game.rps.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(this.getLayoutInflater());
        this.setContentView(binding.getRoot());

        this.setBarStyles();

        this.findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_settings, R.id.navigation_game, R.id.navigation_info)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.name_of_game);
        }
        this.bluetoothController = (BluetoothControllerImpl) BluetoothControllerImpl.getController(this);
        GameControllerImpl.getController();
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(ShakingDetector.getInstance(),
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        RPSDatabaseImpl.getInstance(this);
        this.detector = KeyStrokeDetector.getInstance();
        if (ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, this.getRequiredPermissions(), 21);
        }
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = this.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    @Override
    protected void onDestroy() {
        try {
            this.unregisterReceiver(this.mBroadcastReceiverForOnOffBT);
            this.unregisterReceiver(this.mBroadcastReceiverForDiscoverable);
            this.unregisterReceiver(this.mBroadcastReceiver3);
            this.unregisterReceiver(this.mBroadcastReceiverForPairing);
        }
        catch (IllegalArgumentException ignored){}
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.detector.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.detector.onKeyUp(keyCode, event);
    }

    public BroadcastReceiver getBroadcastReceiverForOnOffBt() {
        return this.mBroadcastReceiverForOnOffBT;
    }

    public BroadcastReceiver getBroadcastReceiverForDiscoverable() {
        return this.mBroadcastReceiverForDiscoverable;
    }

    public void setBroadcastReceiver3(BroadcastReceiver mBroadcastReceiver3) {
        this.mBroadcastReceiver3 = mBroadcastReceiver3;
    }
}