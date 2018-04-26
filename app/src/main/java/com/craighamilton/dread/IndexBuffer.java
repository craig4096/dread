package com.craighamilton.dread;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class IndexBuffer {

	private ShortBuffer	mIndexBuffer;
	private int	mNumIndices;
	
	public IndexBuffer()
	{
	}
	
	public IndexBuffer(short[] indices)
	{
		setIndices(indices);
	}
	
	public void drawTriangles(GL10 gl)
	{
		gl.glDrawElements(GL10.GL_TRIANGLES, mNumIndices, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
	}
	
	public void setIndices(short[] indices)
	{
		mNumIndices = indices.length;
		// create the index buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(6 * 2);
		bb.order(ByteOrder.nativeOrder());
		mIndexBuffer = bb.asShortBuffer();
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
}
