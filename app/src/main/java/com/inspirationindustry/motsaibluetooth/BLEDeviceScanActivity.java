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
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    //NEBLINA CUSTOM UUIDs
    public UUID mUUID;
    public static final UUID NEB_SERVICE_UUID = UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_DATACHAR_UUID = UUID.fromString("0df9f022-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_CTRLCHAR_UUID = UUID.fromString("0df9f023-1532-11e5-8960-0002a5d5c51b");

    //0x2A19 is the battery life characteristic
    //Another option based on using AT+CHAR? gives -> 0xFFE1 for the HM-10 characteristic
//    public final static UUID UUID_BLE_CHARACTERISTIC = UUID.nameUUIDFromBytes(hexStringToByteArray("0x2A19"));


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

        //maybe we need to register this?
//        Intent i = new Intent("START_BROADCAST_RECEIVER");
//        i.setClass(this, mGattUpdateReceiver.getClass());

        scanLeDevice(true);

    }


    //When User Taps a List Item
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        BluetoothDevice device = mDeviceList.get(position);

        Log.i("BLUETOOTH DEBUG", "WHY DOES THIS NOT WORK!!!");

        //Note: Our app is the GATT client
        mBluetoothGatt = device.connectGatt(getBaseContext(), false, mGattCallback);
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        //Create Toast Message
        String clicked_device = device.getName();
        String message = "You clicked on " + clicked_device + " at position " + position;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
//                    [android.bluetooth.BluetoothGattService@41f52190,
//                      android.bluetooth.BluetoothGattService@41f52770,
//                      android.bluetooth.BluetoothGattService@41f52d08]

//                    BluetoothGattService ctrl_char_service = gatt.getService(NEB_CTRLCHAR_UUID);
//                    BluetoothGattService data_char_service = gatt.getService(NEB_DATACHAR_UUID);
                    BluetoothGattService service = gatt.getService(NEB_SERVICE_UUID);

//                    BluetoothGattCharacteristic ctrl_characteristic = service.getCharacteristic(NEB_CTRLCHAR_UUID);
//                    final byte[] bytes = ctrl_characteristic.getValue();
//                    Log.w("BLUETOOH DEBUG","CTRL: " + bytes.toString());

//
                    BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(NEB_DATACHAR_UUID);
//                   //This is the code that returned null
//                    byte[] bytes = data_characteristic.getValue();
//                    Log.w("BLUETOOTH_DEBUG", "DATA: " + bytes);


                    //Here is where we read the characteristic
                    gatt.readCharacteristic(data_characteristic);
                    Log.w("BLUETOOTH_DEBUG", "Data Characteristic Read Enabled");

                    //Alternatively we could follow the instructions here:
                    // http://stackoverflow.com/questions/25865587/android-4-3-bluetooth-ble-dont-called-oncharacteristicread


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
                    Log.w("BLUETOOTH DEBUG", "WOOHOO you read characteristic value = " + characteristic.getValue());


                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    Log.w("BLUETOOTH DEBUG", "You are in onCharacteristicChanged");
                    //TODO: Alternatively we could get periodic reads using the instructions below:
                    // http://stackoverflow.com/questions/25865587/android-4-3-bluetooth-ble-dont-called-oncharacteristicread
                }
            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are broadcasting: " + action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.w("BLUETOOTH DEBUG", "You are in LONG form of onBroadcastUpdate");

        //TEXTBOOK example -> Won't actually work for Neblina module
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if ("1".equals(characteristic.getUuid())) {//"1" is a nonesense value
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

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
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
                // Show all the supported services and characteristics on the
                // user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());//commenting out so it compiles
            } else if (BLEDeviceScanActivity.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//commenting out so it compiles
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_DATA_AVAILABLE");
            }
        }
    };


    @OnClick(R.id.refreshButton)
    public void refreshActivity(View view){

        //start a new scan
        //should we stop the old scan first? It didn't seem to crash when I pressed it at least.
        scanLeDevice(true);
    }

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


//
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }



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
