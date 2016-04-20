package com.inspirationindustry.motsaibluetooth;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import openGLClasses.MyGLSurfaceView;

public class VisualizationActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);



    }
}



