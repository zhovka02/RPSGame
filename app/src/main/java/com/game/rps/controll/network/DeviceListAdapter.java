package com.game.rps.controll.network;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.game.rps.R;

import java.util.List;


public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private final LayoutInflater layoutInflater;
    private final List<BluetoothDevice> mDevices;
    private final int viewResourceId;
    private final Context context;

    public DeviceListAdapter(Context context, int tvResourceId, List<BluetoothDevice> devices) {
        super(context, tvResourceId, devices);
        this.mDevices = devices;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.viewResourceId = tvResourceId;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = this.layoutInflater.inflate(this.viewResourceId, null);

        BluetoothDevice device = this.mDevices.get(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) this.context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 4);
            }
            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }
        }

        return convertView;
    }

}
