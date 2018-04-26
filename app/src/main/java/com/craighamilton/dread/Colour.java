package com.craighamilton.dread;

public class Colour {

	public float mRed;
	public float mGreen;
	public float mBlue;
	public float mAlpha;
	
	public Colour()
	{
		mAlpha = 1.0f;
		mRed = 1.0f;
		mGreen = 1.0f;
		mBlue = 1.0f;
	}
	
	public Colour(int argb)
	{
		mAlpha = ((argb >> 24) & 0xFF) / 255.0f;
		mRed = ((argb >> 16) & 0xFF) / 255.0f;
		mGreen = ((argb >> 8) & 0xFF) / 255.0f;
		mBlue = ((argb >> 0) & 0xFF) / 255.0f;
	}
	
	public Colour(float red, float green, float blue)
	{
		mAlpha = 1.0f;
		mRed = red;
		mGreen = green;
		mBlue = blue;
	}
	
	public Colour(float red, float green, float blue, float alpha)
	{
		mAlpha = alpha;
		mRed = red;
		mGreen = green;
		mBlue = blue;
	}
}
