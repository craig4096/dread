package com.craighamilton.dread;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WeaponHolster implements Weapon.Listener
{
	private final Player mPlayer;
	private Sprite mAmmoIndicatorSprite;
	private Sprite mAmmoIndicatorBackgroundSprite;
	private Weapon.Position mPosition;
	private int mCurrentWeaponIndex = -1;
	private int mSwitchToWeaponIndex = -1;
	private int mLastAmmoCount = -1;
	private final boolean mAmmoBarVisible;
	private final float mAspect;
	
	public WeaponHolster(GameSession game, Player player, Weapon.Position pos)
	{
		mPlayer = player;
		mPosition = pos;
		
		mAmmoIndicatorBackgroundSprite = new Sprite(game.getAmmoBarBackgroundTexture());
		mAspect = mAmmoIndicatorBackgroundSprite.getTexture().getHeight() /
					(float)mAmmoIndicatorBackgroundSprite.getTexture().getWidth();
		mAmmoIndicatorSprite = new Sprite();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(game);
		mAmmoBarVisible = prefs.getBoolean("ammo_bar_visible", false);
	}
	
	private void resizeAmmoBarBackgroundSprite(Sprite sprite, int screenWidth, int screenHeight, float xdpi)
	{
		final float widthPixels = Constants.AmmoBarWidthInches * xdpi;	
		final float heightPixels = widthPixels * mAspect;
		
		// the (x, y) position  of the sprite
		float xpos = Constants.AmmoBarEdgeInches * xdpi;
		float ypos = ((float)screenHeight - heightPixels) / 2.0f;
		
		if(mPosition == Weapon.Position.RightSide)
		{
			xpos = (float)screenWidth - (xpos + widthPixels);
		}
		
		sprite.setRectXYWH(xpos, ypos, widthPixels, heightPixels);
		
		// set the texture coords
		sprite.setTexCoords(new float[]{
				0.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, 0.0f,
				0.0f, 0.0f
		});
	}
	
	/**
	 * Sets the ammo bar sprite based on the following parameters
	 * @param sprite 
	 * @param currentAmmoCount		- The current ammo left in the magazine
	 * @param magazineSize			- The total magazine capacity
	 * @param maxDisplayBullets		- The maximum number of bullets we should display on the ammo bar
	 * @param screenWidth
	 * @param screenHeight
	 * @param xdpi
	 */
	private void resizeAmmoBarSprite(
			Sprite sprite, int currentAmmoCount, int magazineSize,
			int maxDisplayBullets, int screenWidth, int screenHeight, float xdpi)
	{
		final float widthPixels = Constants.AmmoBarWidthInches * xdpi;
		final float heightPixels = widthPixels * mAspect;
		
		// the (x, y) position  of the sprite
		float xpos = Constants.AmmoBarEdgeInches * xdpi;
		float ypos = ((float)screenHeight - heightPixels) / 2.0f;
		
		if(mPosition == Weapon.Position.RightSide)
		{
			xpos = (float)screenWidth - (xpos + widthPixels);
		}
		
		// maximum number of bullets we will be drawing
		int maxDisplayRounds = Math.min(magazineSize, maxDisplayBullets);
		
		// padding
		float padx = Constants.AmmoBarTexturePadding * (widthPixels / (float)mAmmoIndicatorBackgroundSprite.getTexture().getWidth());
		float pady = Constants.AmmoBarTexturePadding * (heightPixels / (float)mAmmoIndicatorBackgroundSprite.getTexture().getHeight());
		
		// the height of an individual bullet (in pixels)
		float bulletHeightPixels = (heightPixels - (2 * pady)) / (float)maxDisplayBullets;
		
		float ratio = currentAmmoCount / (float)magazineSize;
		
		float ammoStackHeight = (maxDisplayRounds * ratio) * bulletHeightPixels;
		
		sprite.setRectXYWH(xpos + padx, ypos + pady, widthPixels - (padx * 2), ammoStackHeight);
		
		
		boolean flippedX = mPosition == Weapon.Position.RightSide;
		// set the texture coords
		sprite.setTexCoords(new float[]{
				(flippedX ? 1.0f : 0.0f), 1.0f,
				(flippedX ? 0.0f : 1.0f), 1.0f,
				(flippedX ? 0.0f : 1.0f), 1.0f - ((float)maxDisplayRounds * ratio),
				(flippedX ? 1.0f : 0.0f), 1.0f - ((float)maxDisplayRounds * ratio)
		});
	}
	
	public void onResolutionChanged(Renderer renderer, int width, int height)
	{
		if(mAmmoBarVisible)
		{
			float xdpi = renderer.getXDpi();
			// if we are currently in the process of switching a weapon then
			// use the next weapon otherwise use current
			if(mSwitchToWeaponIndex != -1)
			{
				updateAmmoBar(mPlayer.getWeapon(mSwitchToWeaponIndex));
			}
			else if(mCurrentWeaponIndex != -1)
			{
				updateAmmoBar(mPlayer.getWeapon(mCurrentWeaponIndex));
			}
			resizeAmmoBarBackgroundSprite(mAmmoIndicatorBackgroundSprite, width, height, xdpi);
		}
	}
	
	public void draw(Renderer renderer)
	{
    	if(mCurrentWeaponIndex != -1)
    	{
    		Weapon currentWeapon = mPlayer.getWeapon(mCurrentWeaponIndex);
    		
    		currentWeapon.draw(renderer);
    		
    		if(mAmmoBarVisible)
    		{
	    		int ammoCount = currentWeapon.getMagazineAmmoCount();
	    		if(ammoCount != mLastAmmoCount)
	    		{
	    			resizeAmmoBarSprite(mAmmoIndicatorSprite, ammoCount, currentWeapon.getMagazineCapacity(), Constants.AmmoBarMaxCapacity,
	    					renderer.getScreenWidth(), renderer.getScreenHeight(), renderer.getXDpi());
	    			
	    			mLastAmmoCount = ammoCount;
	    		}
	
	        	// draw the ammo counter on the side
	        	mAmmoIndicatorBackgroundSprite.draw(renderer);
	        	mAmmoIndicatorSprite.draw(renderer);
    		}
    	}
	}
	
	@Override
	public void weaponSwitchedIn(Weapon weapon)
	{
	}
	
	@Override
	public void weaponSwitchedOut(Weapon weapon)
	{
		// stop listening to old weapon
		mPlayer.getWeapon(mCurrentWeaponIndex).setListener(null);
		mPlayer.getWeapon(mCurrentWeaponIndex).mHolsterLocked = false;
		mCurrentWeaponIndex = mSwitchToWeaponIndex;
		mSwitchToWeaponIndex = -1;
		// start switching in the next weapon
		if(mCurrentWeaponIndex != -1)
		{
			Weapon nextWeapon = mPlayer.getWeapon(mCurrentWeaponIndex);
			// start listening to new weapon
			nextWeapon.setListener(this);
			nextWeapon.setPosition(mPosition);
			// switch in
			nextWeapon.switchIn();
		}
	}
	
	@Override
	public void shotFired(Weapon weapon, int bulletDamage)
	{
		//if(weapon == mCurrentWeapon)
		{
			mPlayer.shotFired(bulletDamage);
		}
	}
	
	/**
	 * Updates the ammo bar to reflect the state of the specified weapon
	 * @param weapon
	 */
	private void updateAmmoBar(Weapon weapon)
	{
		WeaponType type = weapon.getType();
		if(type != null)
		{
			// set the new ammo texture
			mAmmoIndicatorSprite.setTexture(type.mAmmoTexture);
			Renderer renderer = mPlayer.getGame().getRenderer();
			resizeAmmoBarSprite(mAmmoIndicatorSprite,
					weapon.getMagazineAmmoCount(),
					weapon.getMagazineCapacity(),
					Constants.AmmoBarMaxCapacity,
					renderer.getScreenWidth(),
					renderer.getScreenHeight(),
					renderer.getXDpi());
		}
	}
	
	public boolean trySwitchToWeapon(int weaponIndex)
	{
		// can't switch to the weapon if is locked or if it is not switched out
		if(weaponIndex != -1 && (!mPlayer.getWeapon(weaponIndex).isSwitchedOut() || mPlayer.getWeapon(weaponIndex).mHolsterLocked))
		{
			return false;
		}
		
		boolean rval = true;
		// start switching the current weapon out
		if(mCurrentWeaponIndex != -1)
		{
			if(mPlayer.getWeapon(mCurrentWeaponIndex).isIdle())
			{
				mSwitchToWeaponIndex = weaponIndex;
				if(weaponIndex != -1)
				{
					Weapon nextWeapon = mPlayer.getWeapon(weaponIndex);
					nextWeapon.mHolsterLocked = true;
					updateAmmoBar(nextWeapon);
				}
				mPlayer.getWeapon(mCurrentWeaponIndex).switchOut();
			}
			else
			{
				// cannot switch if current weapon is not idle
				rval = false;
			}
		}
		else
		{
			// immediately switch to the next weapon
			mCurrentWeaponIndex = weaponIndex;
			mSwitchToWeaponIndex = -1;
			if(mCurrentWeaponIndex != -1)
			{
				Weapon nextWeapon = mPlayer.getWeapon(mCurrentWeaponIndex);
				nextWeapon.setListener(this);
				nextWeapon.mHolsterLocked = true;
				nextWeapon.setPosition(mPosition);
				nextWeapon.switchIn();
				updateAmmoBar(nextWeapon);
			}
		}
		return rval;
	}
	
	public void switchWeaponUp()
	{	
    	// find the next available index
    	for(int index = mCurrentWeaponIndex+1;; ++index)
    	{
    		// if we are back at the start
    		if(index >= mPlayer.getWeaponCount())
    		{
    			if(trySwitchToWeapon(-1))
    			{
    				break;
    			}
    			break;
    		}
    		
    		// 
    		if(trySwitchToWeapon(index))
    		{
    			break;
    		}
    	}
	}
	
	public Weapon getCurrentWeapon()
	{
		if(mCurrentWeaponIndex != -1)
		{
			return mPlayer.getWeapon(mCurrentWeaponIndex);
		}
		return null;
	}
	
	public int getCurrentWeaponIndex()
	{
		return mCurrentWeaponIndex;
	}
}
