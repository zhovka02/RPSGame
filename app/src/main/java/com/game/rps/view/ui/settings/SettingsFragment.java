package com.game.rps.view.ui.settings;

import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.game.rps.MainActivity;
import com.game.rps.R;
import com.game.rps.controll.network.BluetoothController;
import com.game.rps.controll.network.BluetoothControllerImpl;
import com.game.rps.controll.network.DeviceListAdapter;
import com.game.rps.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    public DeviceListAdapter mDeviceListAdapter;
    private FragmentSettingsBinding binding;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mBroadcastReceiver2;
    private ListView lvNewDevices;
    private BluetoothController bluetoothController;
    private final BroadcastReceiver broadcastReceiverForListing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(SettingsFragment.this.requireActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsFragment.this.requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                }
                if (device.getName() != null) {
                    if (!SettingsFragment.this.bluetoothController.getAllDevices().contains(device))
                        SettingsFragment.this.bluetoothController.getAllDevices().add(device);
                    SettingsFragment.this.lvNewDevices.setAdapter(SettingsFragment.this.mDeviceListAdapter);
                }

            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        this.binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBroadcastReceiver = ((MainActivity) this.requireActivity()).getBroadcastReceiverForOnOffBt();
        this.mBroadcastReceiver2 = ((MainActivity) this.requireActivity()).getBroadcastReceiverForDiscoverable();
        ((MainActivity) this.requireActivity()).setBroadcastReceiver3(this.broadcastReceiverForListing);

        Button btnOnOff = root.findViewById(R.id.btnOnOff);
        Button discOnOff = root.findViewById(R.id.discOnOff);
        Button discover = root.findViewById(R.id.btnFindUnpairedDevices);
        Button reset = root.findViewById(R.id.reset);
        this.lvNewDevices = root.findViewById(R.id.lvNewDevices);

        this.bluetoothController = BluetoothControllerImpl.getController(this.requireActivity());

        btnOnOff.setOnClickListener(view -> this.enableDisableBT());
        discOnOff.setOnClickListener(view -> this.enableDisableDiscoverable());
        discover.setOnClickListener(view -> this.discover());
        reset.setOnClickListener(view -> this.reset());
        this.mDeviceListAdapter = new DeviceListAdapter(this.requireActivity(), R.layout.device_adapter_view, this.bluetoothController.getAllDevices());

        this.lvNewDevices.setOnItemClickListener((adapterView, view, i, l) -> this.pair(i));
        return root;
    }

    private void reset() {
        this.bluetoothController.reset();
        this.mDeviceListAdapter.clear();
    }

    public void enableDisableDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        this.startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.requireActivity().registerReceiver(this.mBroadcastReceiver2, intentFilter);
    }

    private void enableDisableBT() {
        if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivity(enableBT);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.requireActivity().registerReceiver(this.mBroadcastReceiver, BTIntent);
        }
        if (this.mBluetoothAdapter.isEnabled()) {
            if (ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
            this.mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.requireActivity().registerReceiver(this.mBroadcastReceiver, BTIntent);
        }
    }

    public void discover() {
        if (ContextCompat.checkSelfPermission(this.requireActivity(), BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        }
        if (this.mBluetoothAdapter.isDiscovering()) {
            this.mBluetoothAdapter.cancelDiscovery();
            this.mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.requireActivity().registerReceiver(this.broadcastReceiverForListing, discoverDevicesIntent);
        }
        if (!this.mBluetoothAdapter.isDiscovering()) {
            this.mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.requireActivity().registerReceiver(this.broadcastReceiverForListing, discoverDevicesIntent);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    public void pair(int i) {
        this.bluetoothController.reset();
        BluetoothDevice device = this.bluetoothController.getAllDevices().get(i);
        this.bluetoothController.connect(device);
    }

}