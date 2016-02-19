package com.inspirationindustry.motsaibluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BLEDeviceScanActivity extends ListActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private List<String> mDeviceNameList;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 60000;
    private List<BluetoothDevice> mDeviceList;
    private BluetoothGatt mBluetoothGatt;
    private boolean mConnected = false;

    //GATT CALLBACK VARIABLES
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.inspirationindustry.motsaibluetooth.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.inspirationindustry.motsaibluetooth.EXTRA_DATA";
    private final static String TAG = BLEDeviceScanActivity.class.getSimpleName();
    private int mConnectionState = STATE_DISCONNECTED;

    //NEBLINA CUSTOM UUIDs
    public static final UUID NEB_SERVICE_UUID = UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_DATACHAR_UUID = UUID.fromString("0df9f022-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_CTRLCHAR_UUID = UUID.fromString("0df9f023-1532-11e5-8960-0002a5d5c51b");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_scan_activity);
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


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        BluetoothDevice device = mDeviceList.get(position);

        //Note: Our app is the GATT client
        mBluetoothGatt = device.connectGatt(getBaseContext(), false, mGattCallback);

        //Create Toast Message
        String clicked_device = device.getName();
        Toast.makeText(this, "Connecting to " + clicked_device, Toast.LENGTH_LONG).show();
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
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w("BLUETOOTH DEBUG", "You found something! Running LeScan Callback " + scanRecord.toString());
                                if(device.getName()!=null) {
                                    if(!mDeviceList.contains(device)) {
                                        mLeDeviceListAdapter.add(device.getName().toString()+ "" + device.getAddress());
                                        mLeDeviceListAdapter.notifyDataSetChanged();
                                        mDeviceList.add(device);
                                    }
                                }
                            }
                    });
                }
            };


    //THE 3 CALLBACKS
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                //CALLED WHEN CONNECTION STATE CHANGES
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.w(TAG, "Connected to Gatt server and Starting discovery: " +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                //CALLED WHEN NEW SERVICES ARE DISCOVERED
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    BluetoothGattService service = gatt.getService(NEB_SERVICE_UUID);
                    BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(NEB_DATACHAR_UUID);

                    //Here is where we read the characteristic
                    gatt.readCharacteristic(data_characteristic);
                    Log.w("BLUETOOTH_DEBUG", "Data Characteristic Read Enabled");

                    //Broadcast the discovery of BLE services
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                //CALLED WHEN CHARACTERISTICS ARE READ
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.w("BLUETOOTH DEBUG", "WOOHOO you read characteristic value = " + characteristic.getValue());


                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                //CALLED WHEN SUBSCRIBED AND A NEW CHARACTERISTIC ARRIVES
                //TODO: Create subscription service
                @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    Log.w("BLUETOOTH DEBUG", "You are in onCharacteristicChanged");
                    //TODO: Alternatively we could get periodic reads using the instructions below:
                    // http://stackoverflow.com/questions/25865587/android-4-3-bluetooth-ble-dont-called-oncharacteristicread
                }
            };

    //BROADCAST WITHOUT CHARACTERISTIC
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are broadcasting: " + action);
        sendBroadcast(intent);
    }

    //BROADCAST WITH CHARACTERISTIC
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are in LONG form of onBroadcastUpdate");

        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            Log.w("BLUETOOTH DEBUG", "Hex (length=" + data.length + "): " + stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                    stringBuilder.toString());
        }

        //Unwrap Data Based on Motsai's Neblina Protocol
        if (data.length == 20) {
            //Plus 1 is to remind me that the end of the range is non-inclusive
            final byte[] header = Arrays.copyOfRange(data, 0, 3 + 1); //Bytes 0-3 are the header
            final byte[] timestamp = Arrays.copyOfRange(data, 4, 7 + 1); //Bytes 4-7 are the timestamp
            final byte[] q0 = Arrays.copyOfRange(data, 8, 9 + 1); // Bytes 8-9 are Q0 value
            final byte[] q1 = Arrays.copyOfRange(data, 10, 11 + 1); // Bytes 10-11 are Q1 value
            final byte[] q2 = Arrays.copyOfRange(data, 12, 13 + 1); // Bytes 12-13 are Q2 value
            final byte[] q3 = Arrays.copyOfRange(data, 14, 15 + 1); // Bytes 12-15 are Q3 value
            final byte[] reserved = Arrays.copyOfRange(data, 16, 19 + 1); // Bytes 16-19 are reserved

            //Convert to big endian
            float Q0 = normalizedQ(q0);
            float Q1 = normalizedQ(q1);
            float Q2 = normalizedQ(q2);
            float Q3 = normalizedQ(q3);

            Log.w("BLUETOOTH DEBUG", "Q0: " + Q0);
            Log.w("BLUETOOTH DEBUG", "Q1: " + Q1);
            Log.w("BLUETOOTH DEBUG", "Q2: " + Q2);
            Log.w("BLUETOOTH DEBUG", "Q3: " + Q3);

            byte b = q0[0];
            q0[0] = q0[1];
            q0[1] = b;
            int val = ((q0[0]&0xff)<<8)|(q0[1]&0xff);
            Log.w("BLUETOOTH DEBUG", "Q0 value int:" + val);
            float normalized_q0 = (float) val / 32768;
            Log.w("BLUETOOTH DEBUG", "Q0 value normalized:" + normalized_q0);

            sendBroadcast(intent);
        }
    }

    private float normalizedQ(byte[] q) {
        if(q.length==2){
            int val = ((q[1]&0xff)<<8)|(q[0]&0xff); //concatenate the byte array into an int
            float normalized = (float) val / 32768; //normalize by dividing by 2^15
            if (normalized > 1.0) normalized = normalized-2;
            return normalized;
        }else return -1;
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    //TODO: Once we parse the data, we should handle it's display here... this will have to be integrated with the openGL library later on
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.w("BLUETOOTH DEBUG", "You are in BroadcastReceiver's onReceive: " + action);
            if (BLEDeviceScanActivity.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_CONNECTED");
                // updateConnectionState(R.string.connected); //commenting out so it compiles
                invalidateOptionsMenu();
            } else if (BLEDeviceScanActivity.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_DISCONNECTED");
//                updateConnectionState(R.string.disconnected);//commenting out so it compiles
                invalidateOptionsMenu();
//                clearUI();//commenting out so it compiles
            } else if (BLEDeviceScanActivity.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_SERVICES_DISCOVERED");
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());//commenting out so it compiles
            } else if (BLEDeviceScanActivity.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//commenting out so it compiles
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_DATA_AVAILABLE");
            }
        }
    };


// READING BLE ATTRIBUTES SAMPLE CODE
//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return;
//        String uuid = null;
//        String unknownServiceString = getResources().
//                getString(R.string.unknown_service);
//        String unknownCharaString = getResources().
//                getString(R.string.unknown_characteristic);
//        ArrayList<HashMap<String, String>> gattServiceData =
//                new ArrayList<HashMap<String, String>>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
//                = new ArrayList<ArrayList<HashMap<String, String>>>();
//        mGattCharacteristics =
//                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//        // Loops through available GATT Services.
//        for (BluetoothGattService gattService : gattServices) {
//            HashMap<String, String> currentServiceData =
//                    new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(
//                    LIST_NAME, SampleGattAttributes.
//                            lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                    new ArrayList<HashMap<String, String>>();
//            List<BluetoothGattCharacteristic> gattCharacteristics =
//                    gattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> charas =
//                    new ArrayList<BluetoothGattCharacteristic>();
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic :
//                    gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData =
//                        new HashMap<String, String>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(
//                        LIST_NAME, SampleGattAttributes.lookup(uuid,
//                                unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
//            }
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//
//    }


}