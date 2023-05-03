
package com.example.artech.geoapi.common.samplerender;

import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLU;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/*
  maybeThrowGLException(String reason, String api): This method throws a GLException if there is any OpenGL error.
  The method takes two parameters - reason and api - which are used to create an error message if an OpenGL error is detected.

  maybeLogGLError(int priority, String tag, String reason, String api): This method logs a message with the given
  logcat priority if there is any OpenGL error. The method takes four parameters - priority, tag, reason, and api -
  which are used to create a log message if an OpenGL error is detected.

  formatErrorMessage(String reason, String api, List<Integer> errorCodes): This method formats an error message with
  the given reason, api, and a list of OpenGL error codes.

  getGlErrors(): This method retrieves a list of OpenGL error codes. If there are no errors, it returns null. Otherwise,
  it returns a list of all the detected error codes. The method uses a while loop to continuously retrieve error codes
  until there are no more errors.

  The class is marked as final and the constructor is private, indicating that it cannot be instantiated or subclassed.
 */


/** Module for handling OpenGL errors. */
public class GLError {
  /** Throws a {@link GLException} if a GL error occurred. */
  public static void maybeThrowGLException(String reason, String api) {
    List<Integer> errorCodes = getGlErrors();
    if (errorCodes != null) {
      throw new GLException(errorCodes.get(0), formatErrorMessage(reason, api, errorCodes));
    }
  }

  /** Logs a message with the given logcat priority if a GL error occurred. */
  public static void maybeLogGLError(int priority, String tag, String reason, String api) {
    List<Integer> errorCodes = getGlErrors();
    if (errorCodes != null) {
      Log.println(priority, tag, formatErrorMessage(reason, api, errorCodes));
    }
  }

  private static String formatErrorMessage(String reason, String api, List<Integer> errorCodes) {
    StringBuilder builder = new StringBuilder(String.format("%s: %s: ", reason, api));
    Iterator<Integer> iterator = errorCodes.iterator();
    while (iterator.hasNext()) {
      int errorCode = iterator.next();
      builder.append(String.format("%s (%d)", GLU.gluErrorString(errorCode), errorCode));
      if (iterator.hasNext()) {
        builder.append(", ");
      }
    }
    return builder.toString();
  }

  private static List<Integer> getGlErrors() {
    int errorCode = GLES30.glGetError();
    // Shortcut for no errors
    if (errorCode == GLES30.GL_NO_ERROR) {
      return null;
    }
    List<Integer> errorCodes = new ArrayList<>();
    errorCodes.add(errorCode);
    while (true) {
      errorCode = GLES30.glGetError();
      if (errorCode == GLES30.GL_NO_ERROR) {
        break;
      }
      errorCodes.add(errorCode);
    }
    return errorCodes;
  }

  private GLError() {}
}
