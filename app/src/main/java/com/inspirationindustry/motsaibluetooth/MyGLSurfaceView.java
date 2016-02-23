package com.inspirationindustry.motsaibluetooth;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by scott on 2016-02-22.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context){
        super(context);
        setEGLContextClientVersion(2); //So says stack overflow article
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);
    }
}
