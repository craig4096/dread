package com.craighamilton.dread;

import java.util.Vector;

public class AnimationHelper {

	public interface Listener
	{
		public void animationFinished(String animationName);
	}
	
	private SpriteSheet mSpriteSheet;
	private int mStartFrame;
	private int mEndFrame;
	private float mCurrentFrame;
	private boolean mLooping;
	private Listener mListener = null;
	private String mCurrentAnimationName = "";
	
	public AnimationHelper(SpriteSheet spriteSheet)
	{
		mSpriteSheet = spriteSheet;
		mStartFrame = 0;
		mCurrentFrame = 0.0f;
		mEndFrame = spriteSheet.getFrameCount();
		mLooping = true;
	}
	
	private void setAnimation(SpriteSheet.Animation anim)
	{
		mStartFrame = anim.getStartFrame();
		mEndFrame = anim.getEndFrame();
		mCurrentAnimationName = anim.getName();
		if(mEndFrame > mSpriteSheet.getFrameCount())
		{
			// TODO: throw exception, malformed animation frame data
		}
		mCurrentFrame = (float)mStartFrame;
	}
	
	public boolean playAnimation(String name)
	{
		SpriteSheet.Animation anim = mSpriteSheet.findAnimation(name);
		if(anim != null)
		{
			setAnimation(anim);
			return true;
		}
		return false;
	}
	
	public boolean playRandomAnimationContainingName(String name)
	{
		Vector<SpriteSheet.Animation> anims = mSpriteSheet.findAllAnimationsContaining(name);
		if(anims.size() > 0)
		{
			int index = (int)(Math.random() * (double)anims.size());
			if(index == anims.size())
			{
				index = 0;
			}
			setAnimation(anims.get(index));
			return true;
		}
		return false;
	}
	
	public void setLooping(boolean looping)
	{
		mLooping = looping;
	}
	
	public void update(float dt)
	{
		int lastFrame = (int)Math.floor(mCurrentFrame);
		// update animation
		mCurrentFrame += mSpriteSheet.getFramesPerSecond() * dt;
		if(mCurrentFrame >= mEndFrame)
		{
			if(mLooping)
			{
				mCurrentFrame = mStartFrame;
			}
			else
			{
				if(mListener != null)
				{
					mListener.animationFinished(mCurrentAnimationName);
				}
				mCurrentFrame = mEndFrame-1;
			}
		}
		
		int nextFrame = (int)Math.floor(mCurrentFrame);
		mHaveTexCoordsChanged = (lastFrame != nextFrame);
	}
	
	private boolean mHaveTexCoordsChanged = true;
	public boolean haveTexCoordsChanged()
	{
		return mHaveTexCoordsChanged;
	}
	
	public float[] getCurrentTexCoords()
	{
		return mSpriteSheet.getUVs((int)Math.floor(mCurrentFrame));
	}
	
	public void setListener(Listener listener)
	{
		mListener = listener;
	}
}
