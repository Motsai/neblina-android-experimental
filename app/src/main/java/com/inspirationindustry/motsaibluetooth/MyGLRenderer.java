package com.inspirationindustry.motsaibluetooth;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by scott on 2016-02-22.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private final float[] mMVPMatrix = new float[16];
    private final float[]  mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    public void onSurfaceCreated(GL10 unused, EGLConfig config){
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        mTriangle = new Triangle();
    }

    public void onDrawFrame(GL10 unused){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //Set the camera position (View Matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Tutorial ROTATION matrix
        float[] scratch = new float[16];
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int)time);
        Matrix.setRotateM(mRotationMatrix,0,angle,0,0,-1.0f);

        

        //Combine rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch,0,mMVPMatrix,0,mRotationMatrix,0);
        //Draw shape
        mTriangle.draw(scratch);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height){
        GLES20.glViewport(0, 0, width, height);

        //Projection Code
        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix,0,-ratio,ratio,-1,1,3,7);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


}
