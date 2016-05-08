package openGL2Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Triangle {

    private final Context mActivityContext;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int mProgram;
    private final int mProgram2;
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

    //TEXTURE variables
    /** Store our model data in a float buffer. */
//    private final FloatBuffer mCubeTextureCoordinates;
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;
    /** This is a handle to our texture data. */
    private int mTextureDataHandle;


    //Default Constructor
    public Triangle(Context activityContext){
        mActivityContext = activityContext;
        mProgram = GLES20.glCreateProgram();
        mProgram2 = GLES20.glCreateProgram();
    }

    public Triangle(Context activityContext,
                    float ax, float ay, float az,
                    float bx, float by, float bz,
                    float cx, float cy, float cz,
                    float color_r,  float color_g,  float color_b, float color_a) {

        mActivityContext = activityContext;
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

//        //TEXTURE coordinates
//        final float[] mCubeTextureCoordinates =
//                {
//                        // Front face
//                        0.0f, 0.0f,
//                        0.0f, 1.0f,
//                        1.0f, 0.0f,
//                        0.0f, 1.0f,
//                        1.0f, 1.0f,
//                        1.0f, 0.0f,
//                };

//        Triangle coords -> Into GPU
        //GPU uses C code, so this byte buffer is needed to tell the system to store the data
//        in a native format...
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

        //define Shaders
        final int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        final int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        //create program and attach the shaders
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vertexShader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);


        //
//        final int pointVertexShaderHandle = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
//        final int pointFragmentShaderHandle = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
////        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
////                new String[] {"a_Position"});
        mProgram2 = GLES20.glCreateProgram();
//        GLES20.glAttachShader(mProgram,vertexShader);
//        GLES20.glAttachShader(mProgram,fragmentShader);
//        GLES20.glLinkProgram(mProgram);
    }

    //We actually just want the coordinates for one triangle...
    public Triangle(Context context, float[] verticeArray, int[] indicesArray, float[] textureArray, float[] normalsArray){
        mActivityContext = context;
        mProgram = GLES20.glCreateProgram();
        mProgram2 = GLES20.glCreateProgram();
    }

    //This could be used to accept vertices, texture coordinates and a normal array
    public Triangle(Context context,
                    float ax, float ay, float az,
                    float bx, float by, float bz,
                    float cx, float cy, float cz,

                    float tax, float tay, float taz,
                    float tbx, float tby, float tbz,
                    float tcx, float tcy, float tcz,

                    float na, float nb, float nc){

        mActivityContext = context;
        mProgram = GLES20.glCreateProgram();
        mProgram2 = GLES20.glCreateProgram();
    }

    public void draw(float[] mvpMatrix){
        //Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        //PRE-PROCESS the variables that will be used
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

        //DRAW the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +

//                    Lighting Code
            "uniform mat4 u_MVMatrix;"   +  // A constant representing the combined model/view matrix.
            "uniform vec3 u_LightPos;"     // The position of the light in eye space.
//            + "attribute vec4 a_Position;"     // Per-vertex position information we will pass in.
            + "attribute vec4 a_Color;"     // Per-vertex color information we will pass in.
            + "attribute vec3 a_Normal;"     // Per-vertex normal information we will pass in.
            + "varying vec4 v_Color;" +

//            "attribute vec2 a_TexCoordinate;" + //texture mapping
//            "varying vec2 v_TexCoordinate;" +//texture mapping
                    "void main() {" +

                    //Lighting Code
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * vPosition);"
// Transform the normal's orientation into eye space.
                    + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));"
// Will be used for attenuation.
                    + "   float distance = length(u_LightPos - modelViewVertex);"
// Get a lighting direction vector from the light to the vertex.
                    + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);"
// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
// pointing in the same direction then it will get max illumination.
                    + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);"
// Attenuate the light based on distance.
                    + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));"
// Multiply the color by the illumination level. It will be interpolated across the triangle.
                    + "   v_Color = a_Color * diffuse;" +

                    //Primary Calculation
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
//            "uniform sampler2D u_Texture;" + //texture mapping
//            "varying vec2 v_TexCoordinate;" +  //texture mapping
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
//                    "diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));" +//texture mapping
//                    "diffuse = diffuse + 0.3;" + //texture mapping
//                    "gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate));" + //texture mapping
                    "}";


    final String pointVertexShader =
            "uniform mat4 u_MVPMatrix;      \n"
                    +	"attribute vec4 a_Position;     \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_Position = u_MVPMatrix   \n"
                    + "               * a_Position;   \n"
                    + "   gl_PointSize = 5.0;         \n"
                    + "}                              \n";

    final String pointFragmentShader =
            "precision mediump float;       \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_FragColor = vec4(1.0,    \n"
                    + "   1.0, 1.0, 1.0);             \n"
                    + "}                              \n";


    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];


        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }



}
