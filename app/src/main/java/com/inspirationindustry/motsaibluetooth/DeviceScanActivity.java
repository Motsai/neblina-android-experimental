package com.inspirationindustry.motsaibluetooth;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by scott on 2016-02-11.
 */
public class DeviceScanActivity extends ListActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_device_list);
//        setListAdapter(mLeDeviceListAdapter);

        //Team Treehouse Adapter
        String[] daysOfTheWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,daysOfTheWeek);
        setListAdapter(adapter);

    }




}

