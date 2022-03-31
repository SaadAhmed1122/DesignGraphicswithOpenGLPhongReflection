package com.bennyplo.designgraphicswithopenglphongreflection;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Sphere {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+"uniform mat4 uMVPMatrix;varying vec4 vColor;" +
                    "attribute vec3 aVertexNormal;"+//attribute variable for normal vectors
                    "attribute vec4 aVertexColor;"+//attribute variable for vertex colors
                    "uniform vec3 uLightSourceLocation;"+//location of the light source (for diffuse and specular light)
                    "uniform vec3 uAmbientColor;"+//uniform variable for Ambient color
                    "varying vec3 vAmbientColor;"+
                    "uniform vec4 uDiffuseColor;" +//color of the diffuse light
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +//diffuse light intensity
                    "uniform vec3 uAttenuation;"+//light attenuation
                    "uniform vec4 uSpecularColor;"+
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; "+
                    "uniform float uMaterialShininess;"+
                    //----------
                    "void main() {"+
                    "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                    "vec4 mvPosition=uMVPMatrix*vec4(aVertexPosition,1.0);"+
                    "vec3 lightDirection=normalize(uLightSourceLocation-mvPosition.xyz);" +
                    "vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);"+
                    "vAmbientColor=uAmbientColor;"+
                    "vDiffuseColor=uDiffuseColor;" +
                    "vSpecularColor=uSpecularColor; "+
                    "vec3 eyeDirection=normalize(-mvPosition.xyz);" +
                    "vec3 reflectionDirection=reflect(-lightDirection,transformedNormal);" +
                    "vec3 vertexToLightSource = mvPosition.xyz-uLightSourceLocation;"+
                    "float diff_light_dist = length(vertexToLightSource);"+
                    "float attenuation = 1.0 / (uAttenuation.x"+
                    "                           + uAttenuation.y * diff_light_dist" +
                    "                           + uAttenuation.z * diff_light_dist * diff_light_dist);"+
                    "vDiffuseLightWeighting =attenuation*max(dot(transformedNormal,lightDirection),0.0);"+
                    "vSpecularLightWeighting=attenuation*pow(max(dot(reflectionDirection,eyeDirection), 0.0), uMaterialShininess);" +
                    "vColor=aVertexColor;"+
                    "}";
    private final String fragmentShaderCode = "precision lowp float;varying vec4 vColor; "+
            "varying vec3 vAmbientColor;"+
            "varying vec4 vDiffuseColor;" +
            "varying float vDiffuseLightWeighting;" +
            "varying vec4 vSpecularColor;" +
            "varying float vSpecularLightWeighting; "+
            "void main() {" +
            "vec4 diffuseColor=vDiffuseLightWeighting*vDiffuseColor;" +
            "vec4 specularColor=vSpecularLightWeighting*vSpecularColor;"+
            "gl_FragColor=vec4(vColor.xyz*vAmbientColor,1)+specularColor+diffuseColor;"+
            "}";
    private final FloatBuffer vertexBuffer,colorBuffer,normalBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mNormalHandle,mColorHandle;
    //--------
    private int diffuseColorHandle;
    private int mMVPMatrixHandle;
    private int lightLocationHandle,uAmbientColorHandle;
    private int specularColorHandle;
    private int materialShininessHandle;
    private int attenuateHandle;
    //--------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3,COLOR_PER_VERTEX=4;
    //---------
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;
    static float SphereVertex[];
    static float SphereColor[];
    static int SphereIndex[];
    static float SphereNormal[];
    static float lightlocation[]=new float[3];
    static float attenuation[]=new float[3];//light attenuation
    static float diffusecolor[]=new float[4];//diffuse light colour
    static float specularcolor[]=new float[4];//specular highlight colour
    static float MaterialShininess=10f;//material shiness
    //--------

    private  void createShpere(float radius,int nolatitude,int nolongitude) {
        float vertices[]=new float[65535];
        float normal[]=new float[65535];
        int pindex[]=new int[65535];
        float pcolor[]=new float[65535];
        int vertexindex=0;
        int normindex=0;
        int colorindex=0;
        int indx=0;
        float dist=0f;
        for (int row=0;row<=nolatitude;row++){
            double theta=row*Math.PI/nolatitude;
            double sinTheta=Math.sin(theta);
            double cosTheta=Math.cos(theta);
            for (int col=0;col<=nolongitude;col++)
            {
                double phi=col*2*Math.PI/nolongitude;
                double sinPhi=Math.sin(phi);
                double cosPhi=Math.cos(phi);
                double x=cosPhi*sinTheta;
                double y=cosTheta;
                double z=sinPhi*sinTheta;
                normal[normindex++]=(float)x;
                normal[normindex++]=(float)y;
                normal[normindex++]=(float)z;
                vertices[vertexindex++]=(float)(radius*x);
                vertices[vertexindex++]=(float)(radius*y)+dist;
                vertices[vertexindex++]=(float)(radius*z);
                pcolor[colorindex++] = 1f;
                pcolor[colorindex++] = 0;//Math.abs(tcolor);
                pcolor[colorindex++] = 0f;
                pcolor[colorindex++] = 1f;
                //--------
                float u=(col/(float)nolongitude);
                float v=(row/(float)nolatitude);
            }
        }
        for (int row=0;row<nolatitude;row++)
        {
            for (int col=0;col<nolongitude;col++)
            {
                int first=(row*(nolongitude+1))+col;
                int second=first+nolongitude+1;
                pindex[indx++]=first;
                pindex[indx++]=second;
                pindex[indx++]=first+1;
                pindex[indx++]=second;
                pindex[indx++]=second+1;
                pindex[indx++]=first+1;
            }
        }

        SphereVertex= Arrays.copyOf(vertices,vertexindex);
        SphereIndex=Arrays.copyOf(pindex,indx);
        SphereNormal=Arrays.copyOf(normal,normindex);
        SphereColor=Arrays.copyOf(pcolor,colorindex);
    }

    public Sphere(){

        createShpere(2,30,30);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length*4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer=cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        ByteBuffer nb = ByteBuffer.allocateDirect(SphereNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb.order(ByteOrder.nativeOrder());
        normalBuffer=nb.asFloatBuffer();
        normalBuffer.put(SphereNormal);
        normalBuffer.position(0);
        ///============
        lightlocation[0]=2f;
        lightlocation[1]=1f;
        lightlocation[2]=2f;
        specularcolor[0]=1;
        specularcolor[1]=1;
        specularcolor[2]=1;
        specularcolor[3]=1;
        //////////////////////
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        MyRenderer.checkGlError("glVertexAttribPointer");
        mColorHandle=GLES32.glGetAttribLocation(mProgram,"aVertexColor");
        GLES32.glEnableVertexAttribArray(mColorHandle);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);

        mNormalHandle=GLES32.glGetAttribLocation(mProgram,"aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // MyRenderer.checkGlError("glVertexAttribPointer");
        // get handle to shape's transformation matrix
        //nMatrixHandle=GLES32.glGetUniformLocation(mProgram, "uNMatrix");
        lightLocationHandle=GLES32.glGetUniformLocation(mProgram, "uLightSourceLocation");
        diffuseColorHandle=GLES32.glGetUniformLocation(mProgram,"uDiffuseColor");
        diffusecolor[0]=1;diffusecolor[1]=1;diffusecolor[2]=1;diffusecolor[3]=1;
        attenuateHandle=GLES32.glGetUniformLocation(mProgram,"uAttenuation");
        attenuation[0]=1;attenuation[1]=0.14f;attenuation[2]=0.07f;
        uAmbientColorHandle=GLES32.glGetUniformLocation(mProgram,"uAmbientColor");
        // MyRenderer.checkGlError("uAmbientColor");
        specularColorHandle=GLES32.glGetUniformLocation(mProgram,"uSpecularColor");
        materialShininessHandle=GLES32.glGetUniformLocation(mProgram,"uMaterialShininess");
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        //MyRenderer.checkGlError("glGetUniformLocation-mMVPMatrixHandle");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(lightLocationHandle,1,lightlocation,0);
        GLES32.glUniform4fv(diffuseColorHandle,1,diffusecolor,0);
        GLES32.glUniform3fv(attenuateHandle,1,attenuation,0);
        GLES32.glUniform3f(uAmbientColorHandle,0.6f,0.6f,0.6f);
        GLES32.glUniform4fv(specularColorHandle,1,specularcolor,0);
        GLES32.glUniform1f(materialShininessHandle,MaterialShininess);
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // Draw the sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
    }

    public void setLightLocation(float px,float py,float pz)
    {
        lightlocation[0]=px;
        lightlocation[1]=py;
        lightlocation[2]=pz;
    }
}
