package com.craighamilton.dread;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
//import android.util.Log;

/**
 * Class representing an opengl texture
 * @author craig
 *
 */
public class Texture {

	
	public enum WrapMode
	{
		Repeat,
		ClampToEdge
	}
	
	private int mTextureId = -1;
	private final String mAssetURL;
	private final WrapMode mWrapMode;
	private final Bitmap mBitmap;
	
	private Texture(String assetURL, WrapMode wrapMode, Bitmap bmp)
	{
		mAssetURL = assetURL;
		mWrapMode = wrapMode;
		mBitmap = bmp;
	}
	
	private Texture()
	{
		mAssetURL = "";
		mWrapMode = WrapMode.Repeat;
		mBitmap = null;
	}
	
	public static Texture load(Context context, String assetURL, WrapMode wrapMode)
	{
		// attempt to open the Bitmap Image
		InputStream is = null;
		try
		{
			is = context.getAssets().open(assetURL);
		}
		catch(IOException e)
		{
			return new Texture();
		}
		
		Bitmap bmp = BitmapFactory.decodeStream(is);
		if(bmp != null)
		{
			return new Texture(assetURL, wrapMode, bmp);
		}
		return new Texture();
	}
	
	public int getWidth()
	{
		return (mBitmap != null) ? mBitmap.getWidth() : 0;
	}
	
	public int getHeight()
	{
		return (mBitmap != null) ? mBitmap.getHeight() : 0;
	}
	
	/**
	 * Loads the GL texture
	 * @param gl handle to OpenGL context
	 * @param context handle to android context
	 * @param url asset filename
	 * @return the Texture object, null if unsuccessful
	 */
	public void loadGL(GL10 gl, Context context)
	{
		// attempt to open the Bitmap Image
		if(mBitmap != null)
		{
			Bitmap bmp = mBitmap;
			// Scale the bitmap if it is not a power of two
			boolean scaleBitmap = false;
			int dstWidth = Utility.nextPowerOfTwo(bmp.getWidth());
			int dstHeight = Utility.nextPowerOfTwo(bmp.getHeight());
			
			if(dstHeight != bmp.getHeight() || dstWidth != bmp.getWidth())
			{
				scaleBitmap = true;
				// scale the bitmap to the next power of two
				bmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true);
			}
			
			int[] texture = { 0 };
			gl.glGenTextures(1, IntBuffer.wrap(texture));
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
			
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			switch(mWrapMode)
			{
				case ClampToEdge:
				{
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
				}
				break;
				case Repeat:
				{
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
				}
				break;
			}
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
	
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	
			if(scaleBitmap)
			{
				bmp.recycle();
			}
			
			mTextureId = texture[0];
		}
		else
		{
			mTextureId = -1;
		}
	}
	
	public static Texture create(Bitmap bitmap, WrapMode wrapMode)
	{
		return new Texture("created", wrapMode, bitmap);
	}

	/**
	 * 
	 * @return raw opengl handle id
	 */
	public int getGLTextureID()
	{
		return mTextureId;
	}
	
	public void cleanUp(GL10 gl)
	{
		int[] id = { mTextureId };
		gl.glDeleteTextures(1, id, 0);
		//Log.d("Acorn", "Deleting texture: " + id);
	}
	
	
	public String getAssetURL()
	{
		return mAssetURL;
	}
	
	public WrapMode getWrapMode()
	{
		return mWrapMode;
	}
}
