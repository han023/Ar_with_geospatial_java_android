
package com.example.artech.geoapi.common.samplerender;

import android.opengl.GLES30;
import java.io.Closeable;
import java.nio.IntBuffer;


/*

  This is a Java class called IndexBuffer which represents an OpenGL ES index buffer. It contains a
  single field buffer of type GpuBuffer, which represents the actual buffer stored on the GPU.

  The class provides two constructors, one of which takes a SampleRender object and an IntBuffer of indices.
  The other constructor is not visible outside the package and takes a GpuBuffer object as a parameter.
  The second constructor is used to create a new IndexBuffer instance from an existing buffer, which is
  useful when multiple index buffers need to share the same data.

  The class provides a set method to populate the buffer with new data. The set method takes an IntBuffer of
  indices and updates the GPU buffer with the contents of the buffer. If the size of the new data is larger
  than the capacity of the existing buffer, a new buffer is allocated automatically.

  The close method frees the GPU buffer. The getBufferId and getSize methods are package-private and used by
  other classes within the same package. getBufferId returns the OpenGL ES buffer ID associated with this IndexBuffer,
  and getSize returns the number of indices stored in the buffer.

*/


/**
 * A list of vertex indices stored GPU-side.
 *
 * <p>When constructing a {@link Mesh}, an {@link IndexBuffer} may be passed to describe the
 * ordering of vertices when drawing each primitive.
 *
 * @see <a
 *     href="https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glDrawElements.xhtml">glDrawElements</a>
 */
public class IndexBuffer implements Closeable {
  private final GpuBuffer buffer;

  /**
   * Construct an {@link IndexBuffer} populated with initial data.
   *
   * <p>The GPU buffer will be filled with the data in the <i>direct</i> buffer {@code entries},
   * starting from the beginning of the buffer (not the current cursor position). The cursor will be
   * left in an undefined position after this function returns.
   *
   * <p>The {@code entries} buffer may be null, in which case an empty buffer is constructed
   * instead.
   */
  public IndexBuffer(SampleRender render, IntBuffer entries) {
    buffer = new GpuBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, GpuBuffer.INT_SIZE, entries);
  }

  /**
   * Populate with new data.
   *
   * <p>The entire buffer is replaced by the contents of the <i>direct</i> buffer {@code entries}
   * starting from the beginning of the buffer, not the current cursor position. The cursor will be
   * left in an undefined position after this function returns.
   *
   * <p>The GPU buffer is reallocated automatically if necessary.
   *
   * <p>The {@code entries} buffer may be null, in which case the buffer will become empty.
   */
  public void set(IntBuffer entries) {
    buffer.set(entries);
  }

  @Override
  public void close() {
    buffer.free();
  }

  /* package-private */
  int getBufferId() {
    return buffer.getBufferId();
  }

  /* package-private */
  int getSize() {
    return buffer.getSize();
  }
}
