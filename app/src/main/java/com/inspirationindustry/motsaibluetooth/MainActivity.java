package com.inspirationindustry.motsaibluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Refresh button shortcut
//    @OnClick(R.id.refreshButton)
//    public void refreshButton(View view){
//        scanLeDevice(true);
//    }


    //Butterknife shortcut to calling an intent instead of creating an onClick listener.
    @OnClick(R.id.nextButton)
    public void startDailyActivity(View view){

        //What I actually want
//        Intent intent = new Intent(this,DeviceScanActivity.class);
//       startActivity(intent);

        //This definitely works
        Intent intent = new Intent(this,TestActivity2.class);
        startActivity(intent);
    }
}
