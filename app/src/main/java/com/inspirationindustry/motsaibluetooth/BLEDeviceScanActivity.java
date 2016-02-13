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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class BLEDeviceScanActivity extends ListActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private List<String> mDeviceNameList;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 60000;
    private List<BluetoothDevice> mDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        activateBLE();
        Log.w("BLUETOOTH DEBUG", "BLE was activated");

        //Try without bluetooth device type but still lists
        mDeviceNameList = new ArrayList<String>();
        mDeviceList = new ArrayList<BluetoothDevice>();
        mLeDeviceListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDeviceNameList);
        setListAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);

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
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //stops scanning after a pre-defined period
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.w("BLUETOOTH DEBUG", "ending the scan!");
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            Log.w("BLUETOOTH DEBUG", "starting the scan!");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            Log.w("BLUETOOTH DEBUG", "ending the scan!");
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
                            Log.w("BLUETOOTH DEBUG", "YOU FOUND SOMETHING!");
                            mLeDeviceListAdapter.add(device.getName().toString());
                            mDeviceList.add(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @OnClick(R.id.refreshButton)
    public void refreshActivity(View view){

        //start a new scan
        //should we stop the old scan first? It didn't seem to crash when I pressed it at least.
        scanLeDevice(true);
    }


}





//ARRAY/LIST/ADAPTER EXPERIMENTS AND NOTES
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

//Here is a basic adapter that works
//        String[] daysOfTheWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,daysOfTheWeek);
//        setListAdapter(adapter);
//
// NOTES: Basically I used the treehouse method, except it needed a List<> instead of
// an array so that you can add devices on the fly as they are detected
