
package com.example.artech.geoapi.common.samplerender;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.ar.core.Anchor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/*

  This is a snippet of Java code for rendering 3D objects using OpenGL. It defines a class called
  SampleRender which implements rendering logic and provides methods to draw meshes and clear the
  framebuffer. It also defines an interface called Renderer for rendering callbacks.

  The SampleRender constructor takes a GLSurfaceView, a Renderer implementation, and an AssetManager
  as arguments. The GLSurfaceView is used to set up the OpenGL context and the renderer callbacks.
  The Renderer implementation is used to receive the rendering callbacks. The AssetManager is used to load Android resources.

  The SampleRender class provides two draw methods to draw meshes with a specified shader. One method
  draws to the default framebuffer, while the other draws to a specified Framebuffer. It also provides
  a clear method to clear the given framebuffer.

  The SampleRender class provides a method called renderCompassAtAnchor to render a virtual compass
  at an ARCore Anchor. The method takes several matrices, a Shader, a Mesh, and a Framebuffer as arguments.
  It uses the matrices to calculate the model/view/projection matrices, sets up the Shader and Mesh, and
  draws the mesh to the specified Framebuffer.

  Overall, this code is a basic framework for rendering 3D objects using OpenGL, with support for loading
  Android resources and rendering ARCore objects. However, there are many details missing from this code,
  such as the implementation of the Shader and Mesh classes, and the implementation of the rendering callbacks
  in the Renderer interface.

 */


/** A SampleRender context. */
public class SampleRender {
  private static final String TAG = SampleRender.class.getSimpleName();

  private final AssetManager assetManager;

  private int viewportWidth = 1;
  private int viewportHeight = 1;

  /**
   * Constructs a SampleRender object and instantiates GLSurfaceView parameters.
   *
   * @param glSurfaceView Android GLSurfaceView
   * @param renderer Renderer implementation to receive callbacks
   * @param assetManager AssetManager for loading Android resources
   */
  public SampleRender(GLSurfaceView glSurfaceView, Renderer renderer, AssetManager assetManager) {
    this.assetManager = assetManager;
    glSurfaceView.setPreserveEGLContextOnPause(true);
    glSurfaceView.setEGLContextClientVersion(3);
    glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    glSurfaceView.setRenderer(
        new GLSurfaceView.Renderer() {
          @Override
          public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glEnable(GLES30.GL_BLEND);
            GLError.maybeThrowGLException("Failed to enable blending", "glEnable");
            renderer.onSurfaceCreated(SampleRender.this);
          }

          @Override
          public void onSurfaceChanged(GL10 gl, int w, int h) {
            viewportWidth = w;
            viewportHeight = h;
            renderer.onSurfaceChanged(SampleRender.this, w, h);
          }

          @Override
          public void onDrawFrame(GL10 gl) {
            clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f);
            renderer.onDrawFrame(SampleRender.this);
          }
        });
    glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    glSurfaceView.setWillNotDraw(false);
  }

  /** Draw a {@link Mesh} with the specified {@link Shader}. */
  public void draw(Mesh mesh, Shader shader) {
    draw(mesh, shader, /*framebuffer=*/ null);
  }

  /**
   * Draw a {@link Mesh} with the specified {@link Shader} to the given {@link Framebuffer}.
   *
   * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is used.
   */
  public void draw(Mesh mesh, Shader shader, Framebuffer framebuffer) {
    useFramebuffer(framebuffer);
    shader.lowLevelUse();
    mesh.lowLevelDraw();
  }

  /**
   * Clear the given framebuffer.
   *
   * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is
   * cleared.
   */
  public void clear(Framebuffer framebuffer, float r, float g, float b, float a) {
    useFramebuffer(framebuffer);
    GLES30.glClearColor(r, g, b, a);
    GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
    GLES30.glDepthMask(true);
    GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
  }

  /** Interface to be implemented for rendering callbacks. */
  public static interface Renderer {
    /**
     * Called by {@link SampleRender} when the GL render surface is created.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceCreated}.
     */
    public void onSurfaceCreated(SampleRender render);

    /**
     * Called by {@link SampleRender} when the GL render surface dimensions are changed.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceChanged}.
     */
    public void onSurfaceChanged(SampleRender render, int width, int height);

    /**
     * Called by {@link SampleRender} when a GL frame is to be rendered.
     *
     * <p>See {@link GLSurfaceView.Renderer#onDrawFrame}.
     */
    public void onDrawFrame(SampleRender render);
  }

  /* package-private */
  AssetManager getAssets() {
    return assetManager;
  }

  private void useFramebuffer(Framebuffer framebuffer) {
    int framebufferId;
    int viewportWidth;
    int viewportHeight;
    if (framebuffer == null) {
      framebufferId = 0;
      viewportWidth = this.viewportWidth;
      viewportHeight = this.viewportHeight;
    } else {
      framebufferId = framebuffer.getFramebufferId();
      viewportWidth = framebuffer.getWidth();
      viewportHeight = framebuffer.getHeight();
    }
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
    GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
    GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
    GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
  }

  public void renderCompassAtAnchor(Anchor anchor,float[] viewMatrix,float[] modelMatrix,float[] projectionMatrix,
                                    float[] modelViewMatrix,float[] modelViewProjectionMatrix,
                                    Shader virtualObjectShader,Mesh virtualObjectMesh,Framebuffer virtualSceneFramebuffer) {
    // Get the current pose of the Anchor in world space. The Anchor pose is updated
    // during calls to session.update() as ARCore refines its estimate of the world.
    anchor.getPose().toMatrix(modelMatrix, 0);
    // Calculate model/view/projection matrices
    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

    // Update shader properties and draw
    virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
    draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
  }

}
