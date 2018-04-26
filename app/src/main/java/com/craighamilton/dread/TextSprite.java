package com.craighamilton.dread;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextSprite extends Sprite {
	
	private String mText;
	private int mSizeX = 0;
	private int mSizeY = 0;
	private int mTextHeightPixels = 0;

	public TextSprite(Renderer renderer, String text, int textHeightPixels)
	{
		super();
		mTextHeightPixels = textHeightPixels;
		setText(renderer, text);
	}
	
	
	public void setText(Renderer renderer, String text)
	{
		mText = text;
		
		if(mText.length() == 0)
		{
			mText = " ";
		}
		
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setTextSize(mTextHeightPixels);
		p.setColor(0xffffffff);
		p.setTextAlign(Paint.Align.LEFT);

		float xpos = getPosition().mX - (mSizeX / 2);
		float ypos = getPosition().mY - (mSizeY / 2);
		
		// calculate the dimensions of the text
		Rect bounds = new Rect();
		p.getTextBounds(text, 0, text.length(), bounds);
		mSizeX = bounds.width();
		mSizeY = mTextHeightPixels;
		
		setRectXYWH(xpos, ypos, mSizeX, mSizeY);
	
		// construct the bitmap
		Bitmap bmp = Bitmap.createBitmap(mSizeX, mSizeY, Bitmap.Config.ARGB_8888);

		// now draw the text to texture
		Canvas c = new Canvas(bmp);
		c.drawARGB(0, 0, 0, 0);
		Paint.FontMetrics metrics = p.getFontMetrics();
		c.drawText(text, 0, mSizeY - metrics.descent, p);
		
		if(mTexture != null)
		{
			renderer.releaseTexture(mTexture);
			mTexture = null;
		}
		// Create the texture from the bitmap
		mTexture = renderer.createTexture(bmp, Texture.WrapMode.ClampToEdge);
		bmp.recycle();
	}
	
	public void setTextPosition(int x, int y)
	{
		this.setPosition(x + (mSizeX / 2), y + (mSizeY / 2), 0);
	}
	
	
	public void cleanUp(GL10 gl)
	{
		mTexture.cleanUp(gl);
	}
}
