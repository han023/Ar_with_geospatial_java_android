
package com.example.artech.geoapi.common.samplerender;

import android.opengl.GLES30;
import android.util.Log;
import java.nio.Buffer;



/*

  This is a Java class called GpuBuffer which represents a GPU buffer object in OpenGL ES 3.0.
  The class is used to manage a buffer object that is stored in GPU memory and can be used to hold
  vertex data, index data, or other types of data that can be accessed by a shader program during rendering.

  The class has several private fields, including target which specifies the target of the buffer object
  (e.g., GLES30.GL_ARRAY_BUFFER for vertex data), numberOfBytesPerEntry which specifies the number of bytes
  per entry in the buffer, bufferId which stores the ID of the buffer object, size which stores the current
  size of the buffer in entries, and capacity which stores the current capacity of the buffer in entries.

  The class has a constructor that takes a target, number of bytes per entry, and a buffer of entries.
  The constructor generates a new buffer object and binds it to the target specified by the constructor.
  If a buffer of entries is provided, the constructor populates the buffer object with the entries using
  glBufferData(). The set() method can be used to update the contents of the buffer with a new buffer of
  entries using glBufferSubData() or glBufferData() depending on whether the new buffer is smaller or larger than the current buffer.

  The class also has a free() method that deletes the buffer object and frees up GPU memory. There are
  also several public getters for accessing the buffer ID and current size of the buffer.

*/



/* package-private */
class GpuBuffer {
  private static final String TAG = GpuBuffer.class.getSimpleName();

  // These values refer to the byte count of the corresponding Java datatypes.
  public static final int INT_SIZE = 4;
  public static final int FLOAT_SIZE = 4;

  private final int target;
  private final int numberOfBytesPerEntry;
  private final int[] bufferId = {0};
  private int size;
  private int capacity;

  public GpuBuffer(int target, int numberOfBytesPerEntry, Buffer entries) {
    if (entries != null) {
      if (!entries.isDirect()) {
        throw new IllegalArgumentException("If non-null, entries buffer must be a direct buffer");
      }
      // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
      // called with a size of 0, so avoid this case.
      if (entries.limit() == 0) {
        entries = null;
      }
    }

    this.target = target;
    this.numberOfBytesPerEntry = numberOfBytesPerEntry;
    if (entries == null) {
      this.size = 0;
      this.capacity = 0;
    } else {
      this.size = entries.limit();
      this.capacity = entries.limit();
    }

    try {
      // Clear VAO to prevent unintended state change.
      GLES30.glBindVertexArray(0);
      GLError.maybeThrowGLException("Failed to unbind vertex array", "glBindVertexArray");

      GLES30.glGenBuffers(1, bufferId, 0);
      GLError.maybeThrowGLException("Failed to generate buffers", "glGenBuffers");

      GLES30.glBindBuffer(target, bufferId[0]);
      GLError.maybeThrowGLException("Failed to bind buffer object", "glBindBuffer");

      if (entries != null) {
        entries.rewind();
        GLES30.glBufferData(
            target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW);
      }
      GLError.maybeThrowGLException("Failed to populate buffer object", "glBufferData");
    } catch (Throwable t) {
      free();
      throw t;
    }
  }

  public void set(Buffer entries) {
    // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
    // called with a size of 0, so avoid this case.
    if (entries == null || entries.limit() == 0) {
      size = 0;
      return;
    }
    if (!entries.isDirect()) {
      throw new IllegalArgumentException("If non-null, entries buffer must be a direct buffer");
    }
    GLES30.glBindBuffer(target, bufferId[0]);
    GLError.maybeThrowGLException("Failed to bind vertex buffer object", "glBindBuffer");

    entries.rewind();

    if (entries.limit() <= capacity) {
      GLES30.glBufferSubData(target, 0, entries.limit() * numberOfBytesPerEntry, entries);
      GLError.maybeThrowGLException("Failed to populate vertex buffer object", "glBufferSubData");
      size = entries.limit();
    } else {
      GLES30.glBufferData(
          target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW);
      GLError.maybeThrowGLException("Failed to populate vertex buffer object", "glBufferData");
      size = entries.limit();
      capacity = entries.limit();
    }
  }

  public void free() {
    if (bufferId[0] != 0) {
      GLES30.glDeleteBuffers(1, bufferId, 0);
      GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free buffer object", "glDeleteBuffers");
      bufferId[0] = 0;
    }
  }

  public int getBufferId() {
    return bufferId[0];
  }

  public int getSize() {
    return size;
  }
}
