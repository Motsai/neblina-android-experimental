package openGL2Classes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Triangle {

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int mProgram;
    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float[] triangleCoords = {
            0,0,0,
            0,0,0,
            0,0,0
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {0,0,0,0};
    public Triangle(float ax, float ay, float az,
                    float bx, float by, float bz,
                    float cx, float cy, float cz,
                    float color_r,  float color_g,  float color_b, float color_a) {

        color[0] = color_r;
        color[1] = color_g;
        color[2] = color_b;
        color[3] = color_a;

        triangleCoords[0] = ax;
        triangleCoords[1] = ay;
        triangleCoords[2] = az;
        triangleCoords[3] = bx;
        triangleCoords[4] = by;
        triangleCoords[5] = bz;
        triangleCoords[6] = cx;
        triangleCoords[7] = cy;
        triangleCoords[8] = cz;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);

        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram,vertexShader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix){
        //Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        //get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride,vertexBuffer);

        //Get a handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");

        //Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle,1,color,0);

        //get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,"uMVPMatrix");

        // Pass the projection an view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle,1,false,mvpMatrix,0);

        //Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
}
