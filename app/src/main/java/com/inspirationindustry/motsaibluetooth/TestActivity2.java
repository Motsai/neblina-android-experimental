package com.inspirationindustry.motsaibluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TestActivity2 extends ListActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler; //careful there might be another Handler
//    private List<BluetoothDevice> deviceList;
//    private ArrayAdapter<BluetoothDevice> mLeDeviceListAdapter;
    private List<String> deviceList;
    private ArrayAdapter<String> mLeDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        activateBLE();
        Log.w("BLUETOOTH DEBUG", "ACTIVATE BLE COMPLETE!");
        //SETUP THE ADAPTER WITH BLUETOOTHDEVICE type
//        deviceList = new ArrayList<BluetoothDevice>();
//        mLeDeviceListAdapter = new ArrayAdapter<BluetoothDevice>(
//                this, android.R.layout.simple_list_item_1,deviceList); //We need to implement this
//        setListAdapter(mLeDeviceListAdapter);

        //Try without bluetooth device type but still lists
        deviceList = new ArrayList<String>();
        mLeDeviceListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,deviceList);
        mLeDeviceListAdapter.add("Device1");
        mLeDeviceListAdapter.add("Device2");
        setListAdapter(mLeDeviceListAdapter);

        //scanLeDevice(true);

        //Team Treehouse Basic Adapter that works
//        String[] listStrings = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,listStrings);
//        setListAdapter(adapter);
    }

    public void activateBLE() {

        //This should pass
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish(); //optional kill switch
        }

        //Get the Bluetooth Adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        //Enable Bluetooth if required
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }}

        private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if(enable){
            //stops scanning after a pre-defined period
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w("BLUETOOTH DEBUG","YOU FOUND SOMETHING!");
                            mLeDeviceListAdapter.add(device.getName());
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}



//ARRAY EXPERIMENTS AND NOTES
//1. Using ArrayLists + an ArrayAdapter
//List<String> where = new ArrayList<String>();
//where.add( "item1");


//2. HASH MAP STRATEGY REQUIRES A CUSTOM VIEW
//        String[] from = new String[] {"mainItem","rssi"};
//        int[] to = new int[] {R.id.deviceList}
//        ListAdapter mListAdapter = new SimpleAdapter(this,mDeviceList,foundDevices);


//3. Simple Array Adapter Version
//        String[] values = new String[] { "Android List View",
//                "Adapter implementation",
//                "Simple List View In Android",
//                "Create List View Android",
//                "Android Example",
//                "List View Source Code",
//                "List View Array Adapter",
//                "Android Example List View"
//        };
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,values);
//        mDeviceList.setAdapter(adapter);


//4. Team Treehouse Notes:
//There is a special activity class called List Activity
//ListActivity class needs your ListView ID to be "@android:id/list"
//ListActivity has a special Empty View with ID "@android:id/empty
