package openGL2Classes;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.inspirationindustry.motsaibluetooth.BLEDeviceScanActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by scott on 2016-02-22.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Context mActivityContext = null;


    public MyGLRenderer(final Context activityContext) {
        super();
        mActivityContext = activityContext;
    }

    private Triangle[] triangles = new Triangle[1];
    private OBJLoader loader = new OBJLoader(mActivityContext);

    //The 12 Rectangles of our cube
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

    private final float[] mMVPMatrix = new float[16];
    private final float[]  mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    public void onSurfaceCreated(GL10 unused, EGLConfig config){

        //Background Color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //These three lines make the closest shapes appear on top (as is logical)
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LESS);
//        GLES20.glEnable(GLES20.GL_CULL_FACE); //Removes back facing triangles -> take off when using transparent objects
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

//        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
//                new String[] {"a_Position",  "a_Color", "a_Normal", "a_TexCoordinate"});
//
//        mTextureDataHandle = Triangle.loadTexture(this, R.mipmap.square_texture);

        //TODO: Modify the triangle code to accept normals
        //TODO: Load objects from .obj file using the OBJLoader
        triangles = loader.loadObjModel("stall");
        //TODO: For each face in the OBJ, draw a Triangle, assign it a default white color to begin with
        //TODO: Load textures into code
        //TODO: Apply textures
        //Define the 12 triangles of our cube
        //Front Face - Android Green
        mTriangle1 = new Triangle(mActivityContext,
                -0.5f,   0.5f,   0.5f,
                -0.5f,   0.5f,  -0.5f,
                0.5f,    0.5f,  0.5f,
                0.63671875f, 0.76953125f, 0.22265625f, 1.0f
        );

        mTriangle2 = new Triangle(mActivityContext,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.63671875f, 0.76953125f, 0.22265625f, 1.0f
        );

        //Bottom Face - Light Blue
        mTriangle3 = new Triangle(mActivityContext,
                -0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.3333f, 0.3333f, 1.0f, 1.0f
        );

        mTriangle4 = new Triangle(mActivityContext,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.3333f, 0.3333f, 1.0f, 1.0f
        );

        //Left Face - Gold
        mTriangle5 = new Triangle(mActivityContext,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f,
                1.0f, 0.6666f, 0.0f, 1.0f
        );

        mTriangle6 = new Triangle(mActivityContext,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                1.0f, 0.6666f, 0.0f, 1.0f
        );

        //Right Face - Red
        mTriangle7 = new Triangle(mActivityContext,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0.6666f, 0.0f, 0.0f, 1.0f
        );

        mTriangle8 = new Triangle(mActivityContext,
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.6666f, 0.0f, 0.0f, 1.0f
        );

        //Back Face - Dark Blue
        mTriangle9 = new Triangle(mActivityContext,
                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.0f, 0.0f, 0.6666f, 1.0f
        );

        mTriangle10 = new Triangle(mActivityContext,
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.0f, 0.0f, 0.6666f, 1.0f
        );

        //Top Face - Teal
        mTriangle11 = new Triangle(mActivityContext,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.0f, 1.0f, 0.6666f, 1.0f
        );


        mTriangle12 = new Triangle(mActivityContext,
                  0.0f, -0.5f, 0.5f,
                  0.25f,-0.25f,0.5f,
                  -0.25f,-0.25f,0.5f,
                0.0f, 0.6666f, 0.6666f, 1.0f
        );
    }

    public void onDrawFrame(GL10 unused){
        float[] scratch = new float[16];

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //Position the EYE
        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 5.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = -1.0f;
        final float upZ = 0.0f;

        //Set the camera position (View Matrix)
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Tutorial ROTATION matrix
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int)time);

        float q1 = BLEDeviceScanActivity.latest_Q0;
        float q2 = BLEDeviceScanActivity.latest_Q1;
        float q3 = BLEDeviceScanActivity.latest_Q2;
        float q4 = BLEDeviceScanActivity.latest_Q3;

        //Equation from Omid's paper with conversions to make the math work
        double pi_d = Math.PI;
        float pi_f = (float)pi_d;

        double q1_double = q1;
        double theta_double = 2*Math.acos(q1_double);
        float theta = (float)theta_double*180/pi_f;

        double q2_double = q2;
        double rx_double = -1 * q2_double / Math.sin(theta_double/2);
        float rx = (float)rx_double;

        double q3_double = q3;
        double ry_double = -1 * q3_double / Math.sin(theta_double/2);
        float ry = (float)ry_double;

        double q4_double = q4;
        double rz_double = -1 * q4_double / Math.sin(theta_double/2);
        float rz = (float)rz_double;

        //Rotate the matrix
        Matrix.setRotateM(mRotationMatrix, 0, theta, rx, -ry, rz);

        //Combine rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        //Draw shape
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
