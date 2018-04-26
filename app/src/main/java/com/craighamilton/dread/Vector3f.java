package com.craighamilton.dread;

public class Vector3f {
	
	public float mX, mY, mZ;
	
	public Vector3f()
	{
		mX = mY = mZ = 0.0f;
	}
	
	public Vector3f(float x, float y, float z)
	{
		mX = x;
		mY = y;
		mZ = z;
	}
	
	public void add(Vector3f other)
	{
		mX += other.mX;
		mY += other.mY;
		mZ += other.mZ;
	}
	
	public void add(float x, float y, float z)
	{
		mX += x;
		mY += y;
		mZ += z;
	}
	
	public void set(float x, float y, float z)
	{
		mX = x;
		mY = y;
		mZ = z;
	}
	
	public void set(Vector3f other)
	{
		mX = other.mX;
		mY = other.mY;
		mZ = other.mZ;
	}
	
	public Vector3f clone()
	{
		return new Vector3f(mX, mY, mZ);
	}
	
	public float dot(Vector3f other)
	{
		return (mX * other.mX) + (mY * other.mY) + (mZ * other.mZ);
	}
	
	public float length()
	{
		return (float)Math.sqrt((mX * mX) + (mY * mY) + (mZ * mZ));
	}
	
	public float lengthSqrd()
	{
		return (mX * mX) + (mY * mY) + (mZ * mZ);
	}
	
	public void sub(Vector3f other)
	{
		mX -= other.mX;
		mY -= other.mY;
		mZ -= other.mZ;
	}
	
	public void sub(float x, float y, float z)
	{
		mX -= x;
		mY -= y;
		mZ -= z;
	}
	
	public void normalize()
	{
		float len = this.length();
		if(len > 0.0f)
		{
			float recip = 1.0f / len;
			mX *= recip;
			mY *= recip;
			mZ *= recip;
		}
	}
	
	public void cross(Vector3f a, Vector3f b)
	{
		mX = (a.mY * b.mZ) - (b.mY * a.mZ);
		mY = (a.mZ * b.mX) - (b.mZ * a.mX);
		mZ = (a.mX * b.mY) - (b.mX * a.mY);
	}
	
	public void scale(float scale)
	{
		mX *= scale;
		mY *= scale;
		mZ *= scale;
	}
}
