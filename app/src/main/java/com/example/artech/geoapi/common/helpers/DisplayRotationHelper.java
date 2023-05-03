
package com.example.artech.geoapi.common.helpers;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.ar.core.Session;



/*

  This code defines a Java class called DisplayRotationHelper that implements the DisplayListener interface.
  This class helps with handling the display rotation changes that can occur in Android devices, especially when using the camera.

  The class has several fields, including a boolean flag called viewportChanged that indicates whether a change
  in the surface dimensions has been recorded, and the width and height of the viewport. It also has a Display,
  a DisplayManager, and a CameraManager object, which are used to get information about the device display and camera.

  The constructor of the class initializes the DisplayManager and CameraManager objects and gets the default
  display using the WindowManager. The onResume() method registers the DisplayRotationHelper object as a listener
  for display changes, while the onPause() method unregisters it.

  The onSurfaceChanged() method records changes in the surface dimensions and sets the viewportChanged flag to true.
  This information is later used in the updateSessionIfNeeded() method, which updates the Session object's display
  geometry if a change in the display has occurred since the last update.

  The getCameraSensorRelativeViewportAspectRatio() method calculates the aspect ratio of the GL surface viewport while
  accounting for the display rotation relative to the device camera sensor orientation. It does this by first getting
  the camera sensor-to-display rotation and then computing the aspect ratio based on that.

  The getCameraSensorToDisplayRotation() method gets the rotation of the back-facing camera with respect to the display.
  It does this by retrieving the camera characteristics using the CameraManager, getting the sensor orientation from
  the characteristics, and then computing the camera sensor-to-display rotation using the current display orientation.

  The toDegrees() method converts the display rotation value from a Surface constant to degrees.

  Finally, the onDisplayChanged() method is called when the display changes, setting the viewportChanged flag to true.

 */



/**
 * Helper to track the display rotations. In particular, the 180 degree rotations are not notified
 * by the onSurfaceChanged() callback, and thus they require listening to the android display
 * events.
 */
public final class DisplayRotationHelper implements DisplayListener {
  private boolean viewportChanged;
  private int viewportWidth;
  private int viewportHeight;
  private final Display display;
  private final DisplayManager displayManager;
  private final CameraManager cameraManager;

  /**
   * Constructs the DisplayRotationHelper but does not register the listener yet.
   *
   * @param context the Android {@link Context}.
   */
  public DisplayRotationHelper(Context context) {
      displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
      cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
      WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      display = windowManager.getDefaultDisplay();
  }

  /** Registers the display listener. Should be called from . */
  public void onResume() {
      displayManager.registerDisplayListener(this, null);
  }

  /** Unregisters the display listener. Should be called from . */
  public void onPause() {
      displayManager.unregisterDisplayListener(this);
  }

  /**
   * Records a change in surface dimensions. This will be later used by {@link
   * #updateSessionIfNeeded(Session)}. Should be called from {@link
   * android.opengl.GLSurfaceView.Renderer
   * #onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)}.
   *
   * @param width the updated width of the surface.
   * @param height the updated height of the surface.
   */
  public void onSurfaceChanged(int width, int height) {
    viewportWidth = width;
    viewportHeight = height;
    viewportChanged = true;
  }

  /**
   * Updates the session display geometry if a change was posted either by {@link
   * #onSurfaceChanged(int, int)} call or by {@link #onDisplayChanged(int)} system callback. This
   * function should be called explicitly before each call to {@link Session#update()}. This
   * function will also clear the 'pending update' (viewportChanged) flag.
   *
   * @param session the {@link Session} object to update if display geometry changed.
   */
  public void updateSessionIfNeeded(Session session) {
//    if (display != null) {
      if (viewportChanged) {
        int displayRotation = display.getRotation();
        session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight);
        viewportChanged = false;
      }
//    }
  }

  /**
   *  Returns the aspect ratio of the GL surface viewport while accounting for the display rotation
   *  relative to the device camera sensor orientation.
   */
  public float getCameraSensorRelativeViewportAspectRatio(String cameraId) {
    float aspectRatio;
    int cameraSensorToDisplayRotation = getCameraSensorToDisplayRotation(cameraId);
    switch (cameraSensorToDisplayRotation) {
      case 90:
      case 270:
        aspectRatio = (float) viewportHeight / (float) viewportWidth;
        break;
      case 0:
      case 180:
        aspectRatio = (float) viewportWidth / (float) viewportHeight;
        break;
      default:
        throw new RuntimeException("Unhandled rotation: " + cameraSensorToDisplayRotation);
    }
    return aspectRatio;
  }

  /**
   * Returns the rotation of the back-facing camera with respect to the display. The value is one of
   * 0, 90, 180, 270.
   */
  public int getCameraSensorToDisplayRotation(String cameraId) {
    CameraCharacteristics characteristics;
    try {
      characteristics = cameraManager.getCameraCharacteristics(cameraId);
    } catch (CameraAccessException e) {
      throw new RuntimeException("Unable to determine display orientation", e);
    }

    // Camera sensor orientation.
    int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

    // Current display orientation.
    int displayOrientation = toDegrees(display.getRotation());

    // Make sure we return 0, 90, 180, or 270 degrees.
    return (sensorOrientation - displayOrientation + 360) % 360;
  }

  private int toDegrees(int rotation) {
    switch (rotation) {
      case Surface.ROTATION_0:
        return 0;
      case Surface.ROTATION_90:
        return 90;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_270:
        return 270;
      default:
        throw new RuntimeException("Unknown rotation " + rotation);
    }
  }

  @Override
  public void onDisplayAdded(int displayId) {}

  @Override
  public void onDisplayRemoved(int displayId) {}

  @Override
  public void onDisplayChanged(int displayId) {
    viewportChanged = true;
  }
}
