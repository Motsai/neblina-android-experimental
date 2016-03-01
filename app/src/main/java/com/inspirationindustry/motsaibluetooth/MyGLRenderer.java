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

    private Triangle mTriangle1;
    private Triangle mTriangle2;
    private Triangle mTriangle3;
    private Triangle mTriangle4;
    private Triangle mTriangle5;
    private Triangle mTriangle6;
    private Triangle mTriangle7;
    private Triangle mTriangle8;
    private Triangle mTriangle9;
    private Triangle mTriangle10;
    private Triangle mTriangle11;
    private Triangle mTriangle12;

//    private Square mSquare;
    private final float[] mMVPMatrix = new float[16];
    private final float[]  mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    public void onSurfaceCreated(GL10 unused, EGLConfig config){
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //Front Face - Android Green
        mTriangle1 = new Triangle(
                -0.5f,   0.5f,   0.5f,
                -0.5f,   0.5f,  -0.5f,
                0.5f,    0.5f,  0.5f,
                0.63671875f, 0.76953125f, 0.22265625f, 1.0f
        );

        mTriangle2 = new Triangle(
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.63671875f, 0.76953125f, 0.22265625f, 1.0f
        );

        //Bottom Face - Light Blue
        mTriangle3 = new Triangle(
                -0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.3333f, 0.3333f, 1.0f, 1.0f
        );

        mTriangle4 = new Triangle(
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.3333f, 0.3333f, 1.0f, 1.0f
        );

        //Left Face - Gold
        mTriangle5 = new Triangle(
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f,
                1.0f, 0.6666f, 0.0f, 1.0f
        );

        mTriangle6 = new Triangle(
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                1.0f, 0.6666f, 0.0f, 1.0f
        );

        //Right Face - Red
        mTriangle7 = new Triangle(
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0.6666f, 0.0f, 0.0f, 1.0f
        );

        mTriangle8 = new Triangle(
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.6666f, 0.0f, 0.0f, 1.0f
        );

        //Back Face - Dark Blue
        mTriangle9 = new Triangle(
                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.0f, 0.0f, 0.6666f, 1.0f
        );

        mTriangle10 = new Triangle(
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.0f, 0.0f, 0.6666f, 1.0f
        );

        //Top Face - Teal
        mTriangle11 = new Triangle(
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.0f, 0.6666f, 0.6666f, 1.0f
        );

        mTriangle12 = new Triangle(
                -0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.0f, 0.6666f, 0.6666f, 1.0f
        );



        //static declaration for original triangle
//    {   // in counterclockwise order:
//            0.0f,  0.622008459f, 0.0f, // top
//            -0.5f, -0.311004243f, 0.0f, // bottom left
//            0.5f, -0.311004243f, 0.1f  // bottom right
//    };
//        mSquare = new Square();
    }

    public void onDrawFrame(GL10 unused){
        float[] scratch = new float[16];

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //Set the camera position (View Matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
//        mSquare.draw(mMVPMatrix);

        //Tutorial ROTATION matrix
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int)time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 1.0f, 0);



//        float q1 = BLEDeviceScanActivity.latest_Q0;
//        float q2 = BLEDeviceScanActivity.latest_Q1;
//        float q3 = BLEDeviceScanActivity.latest_Q2;
//        float q4 = BLEDeviceScanActivity.latest_Q3;

//        Matrix.setRotateM(mRotationMatrix,0,q1,q2,q3,q4);

//
//        Math.atan2();
//
//        Matrix.setRotateM(mRotationMatrix, 0);


//        float qMatrix[] = {
//                2*q1*q1 - 1 + 2*q2*q2,   2*(q2*q3 + q1*q4),       2* (q2*q4 - q1*q3),      0,
//                2*(q2*q3 - q1*q4),       -1 + 2*q1*q1 + 2*q3*q3,  2*(q3*q4 + q1*q2),       0,
//                2*(q2*q4 + q1*q3),       2*(q3*q4 - q1*q2),       2*q1*q1 - 1 + 2*q4*q4,   0,
//                0,                       0,                       0,                       1
//        } ;

        //Combine rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        //Draw shape
        //TODO: Draw a cube made out of 12 Triangles
        mTriangle1.draw(scratch);
        mTriangle2.draw(scratch);
        mTriangle3.draw(scratch);
        mTriangle4.draw(scratch);
        mTriangle5.draw(scratch);
        mTriangle6.draw(scratch);
        mTriangle7.draw(scratch);
        mTriangle8.draw(scratch);
        mTriangle9.draw(scratch);
        mTriangle10.draw(scratch);
        mTriangle11.draw(scratch);
        mTriangle12.draw(scratch);


        //played around with this for a while...  it only drew half the square (i.e. the first triangle)

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
