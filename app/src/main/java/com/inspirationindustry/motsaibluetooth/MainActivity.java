package com.inspirationindustry.motsaibluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//This acts only as a splash screen while the BLE activity loads
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, BLEDeviceScanActivity.class);
        startActivity(intent);
        finish(); //Close once we end the splash screen
    }

}
