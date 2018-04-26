package com.craighamilton.dread;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class AttributeBuffer {
	
	protected FloatBuffer mBuffer;
	
	public AttributeBuffer(float[] data)
	{
		// create the uv buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
		bb.order(ByteOrder.nativeOrder());
		mBuffer = bb.asFloatBuffer();
		mBuffer.put(data);
		mBuffer.position(0);
	}
	
	// update the data contained within this buffer
	public void update(float[] data)
	{
		mBuffer.put(data);
		mBuffer.position(0);
	}
	
	/**
	 * 
	 * @param gl
	 */
	public void bindVertex3(GL10 gl)
	{
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBuffer);
	}
	
	/**
	 * 
	 * @param gl
	 */
	public void bindTexCoords2D(GL10 gl)
	{
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mBuffer);
	}
}
