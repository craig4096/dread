package com.craighamilton.dread;

public class VectorAnimation
{
	private Vector3f mStart = new Vector3f(0,0,0);
	private Vector3f mEnd = new Vector3f(0,0,0);
	private float mCurrentTime = 0.0f;
	private float mAnimationTime = 0.0f;
	
	public VectorAnimation()
	{
	}
	
	public VectorAnimation(Vector3f start, Vector3f end, float animationTime)
	{
		mStart = start.clone();
		mEnd = end.clone();
		mAnimationTime = animationTime;
	}
	
	public void setStart(Vector3f start)
	{
		mStart.set(start);
	}
	
	public void setEnd(Vector3f end)
	{
		mEnd.set(end);
	}
	
	public void advance(float timeStep)
	{
		mCurrentTime += timeStep;
		if(mCurrentTime >= mAnimationTime)
		{
			mCurrentTime = mAnimationTime;
		}
		if(mCurrentTime <= 0.0f)
		{
			mCurrentTime = 0.0f;
		}
	}
	
	public boolean atBeginning()
	{
		return mCurrentTime <= 0.0f;
	}
	
	public boolean atEnd()
	{
		return mCurrentTime >= mAnimationTime;
	}
	
	public void setAnimationTime(float time)
	{
		mAnimationTime = time;
	}
	
	public void goToStart()
	{
		mCurrentTime = 0.0f;
	}
	
	public void goToEnd()
	{
		mCurrentTime = mAnimationTime;
	}
	
	public Vector3f getCurrentPosition()
	{
		float lerp = mCurrentTime / mAnimationTime;
		return new Vector3f(
				mStart.mX + (mEnd.mX - mStart.mX) * lerp,
				mStart.mY + (mEnd.mY - mStart.mY) * lerp,
				mStart.mZ + (mEnd.mZ - mStart.mZ) * lerp);
	}
	
	public void setCurrentTime(float time)
	{
		mCurrentTime = time;
	}
}
