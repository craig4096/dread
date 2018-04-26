package com.craighamilton.dread;

import java.util.Vector;

public class HeadingBar {

	/**
	 * the current heading/rotation around the z axis
	 * specified in radians
	 */
	private float mHeadingRotation;
	
	/**
	 * This is the visible range of sprite items that will be renderer
	 * on the bar, specified in radians
	 */
	private float mVisibleRotationRange;
	
	private Sprite mBarSprite;
	
	private Sprite mLeftAlertSprite;
	private Sprite mRightAlertSprite;
	
	private boolean mLeftAlertVisible = false;
	private boolean mRightAlertVisible = false;
	private float mAlertTimer = 0.0f;
	
	/**
	 * A SpriteItem is a sprite which is positioned at a certain
	 * rotation, this rotation translates to be displayed on the heading
	 * bar at runtime, if it is within the visible rotation range
	 */
	public static class SpriteItem
	{
		final Sprite mSprite;
		/**
		 * The rotation this sprite item sits at
		 */
		float mRotation;
		
		/**
		 * Whether the alert icons should show if this item is off-screen
		 */
		boolean mShowAlertIfOffscreen;
		
		SpriteItem(Texture texture, float rotation, boolean showAlertIfOffscreen)
		{
			mSprite = new Sprite(texture);
			mRotation = rotation;
			mShowAlertIfOffscreen = showAlertIfOffscreen;
		}
	}
	private Vector<SpriteItem> mSpriteItems = new Vector<SpriteItem>();
	
	/**
	 * Current screen dimensions
	 */
	private int mScreenWidth;
	private int mScreenHeight;
	
	/**
	 * Constructor
	 */
	public HeadingBar(GameSession game)
	{	
		mBarSprite = new Sprite(game.getRenderer().loadTexture("Textures/SwipeBar.png"));
		// North
		SpriteItem north = new SpriteItem(game.getRenderer().loadTexture("Textures/North.png"), 0.0f, false);
		// East
		SpriteItem east = new SpriteItem(game.getRenderer().loadTexture("Textures/East.png"), (float)Math.PI * 0.5f, false);
		// South
		SpriteItem south = new SpriteItem(game.getRenderer().loadTexture("Textures/South.png"), (float)Math.PI * 1.0f, false);
		// West
		SpriteItem west = new SpriteItem(game.getRenderer().loadTexture("Textures/West.png"), (float)Math.PI * 1.5f, false);
		
		mSpriteItems.add(north);
		mSpriteItems.add(east);
		mSpriteItems.add(south);
		mSpriteItems.add(west);
		
		mVisibleRotationRange = Constants.HeadingBarVisibleRotationRange;
		
		Texture leftAlertTexture = game.getRenderer().loadTexture("Textures/LeftAlert.png");
		mLeftAlertSprite = new Sprite(leftAlertTexture);
		mRightAlertSprite = new Sprite(leftAlertTexture);
		// flip the texture coordinates for the right side sprite
		mRightAlertSprite.setTexCoords(new float[]{
				1.0f, 1.0f,
				0.0f, 1.0f,
				0.0f, 0.0f,
				1.0f, 0.0f
		});
	}
	
	/**
	 * Adds a sprite item to the heading bar
	 * @param item
	 */
	public void addSpriteItem(SpriteItem item)
	{
		updateSpriteItemScale(item);
		mSpriteItems.add(item);
	}
	
	/**
	 * Removes a sprite item from the heading bar
	 * @param item
	 * @return false if no item was found to be removed
	 */
	public boolean removeSpriteItem(SpriteItem item)
	{
		return mSpriteItems.remove(item);
	}
	
	public void update(float timeStep)
	{
		mAlertTimer += timeStep;
	}
	
	public void draw(Renderer renderer)
	{
		mBarSprite.draw(renderer);
		for(SpriteItem item : mSpriteItems)
		{
			item.mSprite.draw(renderer);
		}
		
		if(mLeftAlertVisible || mRightAlertVisible)
		{
			// calculate the alpha value based on the alert time
			float alpha = (float)Math.sin(mAlertTimer * Math.PI * Constants.HeadingBarAlertFlashesPerSec);
			// convert range from -1, 1 to 0.5, 1
			alpha = (alpha + 1.0f) * 0.5f;
			alpha = 0.5f + (alpha * 0.5f);
			
			if(mLeftAlertVisible)
			{
				mLeftAlertSprite.drawWithColour(renderer, new Colour(1,1,1,alpha));
			}
			if(mRightAlertVisible)
			{
				mRightAlertSprite.drawWithColour(renderer, new Colour(1,1,1,alpha));
			}
		}
	}
	
	/**
	 * Set the heading rotation, this will usually be the heading the player is
	 * facing
	 */
	public void setRotation(float rotation)
	{
		mHeadingRotation = rotation;
		updateSpriteItemPositions();
	}
	
	/**
	 * Called whenever the resolution of the screen changes
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void onResolutionChanged(Renderer renderer, int screenWidth, int screenHeight)
	{
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		updateSpriteItemScales();
		
		float barHalfHeight = (screenHeight * Constants.HeadingBarHeightRatio) / 2.0f;
    	mBarSprite.setScale(screenWidth / 2.0f, barHalfHeight, 1.0f);
    	mBarSprite.setPosition(screenWidth / 2.0f, barHalfHeight, 0.0f);
    	
    	// assume the image has an aspect ratio of 1.0
    	/*
    	mLeftAlertSprite.setScale(barHalfHeight, barHalfHeight, 1.0f);
    	mLeftAlertSprite.setPosition(barHalfHeight,	barHalfHeight,	0.0f);
    	
    	mRightAlertSprite.setScale(barHalfHeight, barHalfHeight, 1.0f);
    	mRightAlertSprite.setPosition(screenWidth - barHalfHeight, barHalfHeight, 0.0f);
    	*/
    	final float alertWidthPixels = Constants.AlertBarWidthInches * renderer.getXDpi();
    	
    	mLeftAlertSprite.setScale(alertWidthPixels * 0.5f, screenHeight * 0.5f, 1.0f);
    	mLeftAlertSprite.setPosition(alertWidthPixels * 0.5f, screenHeight * 0.5f, 0.0f);
    	
    	mRightAlertSprite.setScale(alertWidthPixels * 0.5f, screenHeight * 0.5f, 1.0f);
    	mRightAlertSprite.setPosition(screenWidth - (alertWidthPixels * 0.5f), screenHeight * 0.5f, 0.0f);
    	
	}
	
	private float getClosestRotation(float heading, float target)
	{
		float a = target - heading;
		float b = (target + (float)(Math.PI * 2.0)) - heading;
		float c = (target - (float)(Math.PI * 2.0)) - heading;
		
		float abs_a = Math.abs(a);
		float abs_b = Math.abs(b);
		float abs_c = Math.abs(c);
		
		if(abs_a < abs_b && abs_a < abs_c)
		{
			return a;
		}
		if(abs_b < abs_a && abs_b < abs_c)
		{
			return b;
		}
		return c;
	}
	
	private void updateSpriteItemPositions()
	{
		mLeftAlertVisible = false;
		mRightAlertVisible = false;
		
		for(SpriteItem item : mSpriteItems)
		{
			float xpos = getClosestRotation(mHeadingRotation, item.mRotation);
			// normalise to be within range (-0.5, 0.5)
			xpos /= mVisibleRotationRange;
			
			if(item.mShowAlertIfOffscreen)
			{
				if(xpos < -0.5f)
				{
					mLeftAlertVisible = true;
				}
				else if(xpos > 0.5f)
				{
					mRightAlertVisible = true;
				}
			}
			
			// convert to screen space position
			xpos *= mScreenWidth;
			// centre
			xpos += mScreenWidth / 2.0f;
			
			item.mSprite.setPosition(xpos, mScreenHeight * (Constants.HeadingBarHeightRatio / 2.0f), 0);
		}
	}
	
	private void updateSpriteItemScale(SpriteItem item)
	{
		float height = mScreenHeight * Constants.HeadingBarHeightRatio;
		Texture t = item.mSprite.getTexture();
		float width = height * (t.getWidth() / (float)t.getHeight());
		item.mSprite.setScale(width / 2.0f, height / 2.0f, 1.0f);
	}
	
	private void updateSpriteItemScales()
	{
		for(SpriteItem item : mSpriteItems)
		{
			updateSpriteItemScale(item);
		}
	}
}
