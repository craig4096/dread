package com.craighamilton.dread;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class SpriteSheet {

	private final int mColumns;
	private final int mRows;
	private final float mFramesPerSecond;
	private final Texture mTexture;
	private final float mXSpacing;
	private final float mYSpacing;
	// Used to avoid texture bleeding due to linear sampling
	// see SpriteSheet.getUVs()
	private final float mOnePixelX;
	private final float mOnePixelY;
	
	
	public static class Animation
	{
		private String mName;
		private int mStartFrame;
		private int mEndFrame;
		
		private Animation(JSONObject json) throws JSONException
		{
			mName = json.getString("name");
			mStartFrame = json.getInt("start");
			mEndFrame = json.getInt("end");
		}
		
		public int getStartFrame()
		{
			return mStartFrame;
		}
		
		public int getEndFrame()
		{
			return mEndFrame;
		}
		
		public String getName()
		{
			return mName;
		}
	}
	private Vector<Animation> mAnimations = new Vector<Animation>();
	
	/**
	 * Loads a sprite sheet given the directory name
	 */
	public SpriteSheet(Context context, Renderer renderer, String name) throws IOException, JSONException
	{
		String location = "SpriteSheets/" + name;
		JSONObject json = Utility.loadJSONObject(context.getAssets().open(location + "/desc.json"));
		JSONArray animations = json.getJSONArray("animations");
		
		for(int i = 0; i < animations.length(); ++i)
		{
			mAnimations.add(new Animation(animations.getJSONObject(i)));
		}
		mColumns = json.getInt("columns");
		mRows = json.getInt("rows");
		mFramesPerSecond = (float)json.getDouble("fps");
		
		mTexture = renderer.loadTexture(location + "/texture.png", Texture.WrapMode.ClampToEdge);
	
		mOnePixelX = 1.0f / (float)mTexture.getWidth();
		mOnePixelY = 1.0f / (float)mTexture.getHeight();
		
		mXSpacing = 1.0f / (float)mColumns;
		mYSpacing = 1.0f / (float)mRows;
	}
	
	public int getFrameCount()
	{
		return mRows * mColumns;
	}
	
	/**
	 * Update current uvs based on current frame number
	 */
	public float[] getUVs(int frameNumber)
	{
		int x = (frameNumber % mColumns);
		int y = (int)Math.floor(frameNumber / (double)mColumns);
		
		float xSpacing = mXSpacing;
		float ySpacing = mYSpacing;
		
		return new float[]{
				(x * xSpacing) + mOnePixelX, ((y+1) * ySpacing) - mOnePixelY,
				((x+1) * xSpacing) - mOnePixelX, ((y+1) * ySpacing) - mOnePixelY,
				((x+1) * xSpacing) - mOnePixelX, (y * ySpacing) + mOnePixelY,
				(x * xSpacing) + mOnePixelX, (y * ySpacing) + mOnePixelY
		};
	}
	
	public float getFramesPerSecond()
	{
		return mFramesPerSecond;
	}
	
	public Animation findAnimation(String name)
	{
		for(Animation anim : mAnimations)
		{
			if(anim.mName.equals(name))
			{
				return anim;
			}
		}
		return null;
	}
	
	public Vector<Animation> findAllAnimationsContaining(String name)
	{
		Vector<Animation> anims = new Vector<Animation>();
		for(Animation anim : mAnimations)
		{
			if(anim.mName.contains(name))
			{
				anims.add(anim);
			}
		}
		return anims;
	}
	
	public Texture getTexture()
	{
		return mTexture;
	}
}
