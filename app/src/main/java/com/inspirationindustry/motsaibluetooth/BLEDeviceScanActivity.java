package com.inspirationindustry.motsaibluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.OnClick;

public class BLEDeviceScanActivity extends ListActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private List<String> mDeviceNameList;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 60000;
    private List<BluetoothDevice> mDeviceList;
    private BluetoothGatt mBluetoothGatt;

    //GATT CALLBACK VARIABLES
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    //0x2A19 is the battery life characteristic
    //Another option based on using AT+CHAR? gives -> 0xFFE1 for the HM-10 characteristic
    public final static UUID UUID_BLE_CHARACTERISTIC = UUID.nameUUIDFromBytes(hexStringToByteArray("0x2A19"));


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
                            Log.w("BLUETOOTH DEBUG", "You found something! Running LeScan Callback");

                            mLeDeviceListAdapter.add(device.getName().toString());
                            mLeDeviceListAdapter.notifyDataSetChanged();

                            mDeviceList.add(device);



                        }
                    });
                }
            };

    //GATT CALLBACK
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    Log.w("BLUETOOTH DEBUG", "You are in onConnectionStateChange");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery: " +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.w("BLUETOOTH DEBUG", "You are in onServicesDiscovered");
                    Log.w("BLUETOOTH DEBUG", gatt.getServices().toString());
                    //Here is what this returned:
//                    [android.bluetooth.BluetoothGattService@41f52190, android.bluetooth.BluetoothGattService@41f52770, android.bluetooth.BluetoothGattService@41f52d08]
//                    [android.bluetooth.BluetoothGattService@41f52190, android.bluetooth.BluetoothGattService@41f52770, android.bluetooth.BluetoothGattService@41f52d08]
//                    [android.bluetooth.BluetoothGattService@41f52190, android.bluetooth.BluetoothGattService@41f52770, android.bluetooth.BluetoothGattService@41f52d08]

                    //TODO
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.w("BLUETOOTH DEBUG", "You are in onCharacteristicRead");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are in short form of broadcastUpdate");
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are in onBroadcastUpdate");
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_BLE_CHARACTERISTIC.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }


    @OnClick(R.id.refreshButton)
    public void refreshActivity(View view){

        //start a new scan
        //should we stop the old scan first? It didn't seem to crash when I pressed it at least.
        scanLeDevice(true);
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l,v,position,id);

        BluetoothDevice device = mDeviceList.get(position);
        //Note: Our app is the GATT client
        mBluetoothGatt = device.connectGatt(getBaseContext(), false, mGattCallback);

        //Create Toast Message
        String clicked_device = device.getName();
        String message = "You clicked on " + clicked_device + " at position " + position;
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
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
