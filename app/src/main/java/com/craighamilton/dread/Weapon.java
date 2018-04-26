package com.craighamilton.dread;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/**
 * Weapon class used to describe each weapon in the game
 * @author Craig
 *
 */
public class Weapon {
	
	public static interface Listener
	{
		public void weaponSwitchedOut(Weapon weapon);
		public void weaponSwitchedIn(Weapon weapon);
		public void shotFired(Weapon weapon, int bulletDamage);
	}
	private Listener mListener = null;
	
	public void setListener(Listener listener)
	{
		mListener = listener;
	}
	
	/**
	 * Whether this weapon is positioned on the left or right side
	 * of the screen for dual-wielding
	 */
	public enum Position
	{
		LeftSide,
		RightSide
	};
	
	private Position mPosition;
	
	private final Vibrator mVibrator;
	
	private int mTotalAmmoCount = 0;
	private int mMagazineAmmoCount = 0;
	private boolean mInfiniteAmmo = true; // all weapons have infinite ammo
	
	private final Sound mFireSound;
	private final Sound mReloadSound;
	private final Sound mEmptySound;

	/**
	 * The image that will be displayed at runtime overlaid on the screen
	 */
	private Sprite mViewSprite;
	
	/**
	 *  the muzzle flash texture
	 */
	private Sprite mMuzzleFlashSprite;
	private float mMuzzleFlashTimer = 0.0f;
	
	
	private float mReloadTimer = 0.0f;
	
	private enum State
	{
		Idle,
		FiringBack,
		FiringForward,
		SwitchingOut,
		SwitchedOut,
		SwitchingIn,
		ReloadingOut,
		ReloadingIn,
		Reloading
	}
	
	private State mState = State.SwitchedOut;
	
	private final WeaponType mType;
	
	/**
	 * If the weapon is currently being used by a holster
	 */
	public boolean mHolsterLocked = false;
	
	// firing timer
	private float	mFireTimer;
	private boolean mTriggerDown = false;
	
	/**
	 * Animations
	 */
	private VectorAnimation mReloadAnimation;
	private VectorAnimation mSwitchAnimation;
	private VectorAnimation mKickBackAnimation;
	
	private Vector3f mIdlePos;
	private Vector3f mSwitchOutPos;
	private Vector3f mKickBackPos;
	
	private float mKickBackTime;
	private float mKickForwardTime;
	
	/**
	 * The muzzle offset from the center of the view sprite (in screen space)
	 * (Same as type.mMuzzlePosXY as the view image position is initially 0,0)
	 */
	private float mMuzzleOffsetX;
	private float mMuzzleOffsetY;
	
	
	private static class ScreenCoord
	{
		float mX, mY;
	}
	private ScreenCoord weaponSpaceToScreenSpace(float x, float y, float screenWidth, float screenHeight)
	{
		ScreenCoord out = new ScreenCoord();
		out.mX = (x / (float)Constants.WeaponTextureWidth) * screenWidth;
		
		float height = screenWidth * (Constants.WeaponTextureHeight / (float)Constants.WeaponTextureWidth);
		
		// scale the y to maintain the weapon space aspect ratio
		out.mY = (y / (float)Constants.WeaponTextureHeight) * height;
		return out;
	}
	
	/**
	 * 
	 * @param obj serialisation
	 * @throws JSONException
	 * @throws IOException 
	 */
	public Weapon(WeaponType type, GameSession game) throws JSONException, IOException
	{
		mType = type;
		
		mTotalAmmoCount = 10000000;
		mMagazineAmmoCount = type.mMagazineCapacity;
		
		// load sounds
		mFireSound = game.loadSound(type.mFireSound);
		mReloadSound = game.loadSound(type.mReloadSound);
		mEmptySound = game.loadSound(type.mEmptySound);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(game);
		if(prefs.getBoolean("vibration_enabled", false))
		{
			mVibrator = (Vibrator) game.getSystemService(Context.VIBRATOR_SERVICE);
		}
		else
		{
			mVibrator = null;
		}
		
		mViewSprite = new Sprite(type.mViewTexture);
		mMuzzleFlashSprite = new Sprite(type.mMuzzleFlashTexture);
		
		mIdlePos = new Vector3f();
		mSwitchOutPos = new Vector3f();
		mKickBackPos = new Vector3f();
		
		// kick back distance (in weapon space)
		float kickBackDistWS = (float)Math.sqrt((mType.mKickBackOffsetX * mType.mKickBackOffsetX) +
									(mType.mKickBackOffsetY + mType.mKickBackOffsetY));
		mKickBackTime = kickBackDistWS / mType.mKickBackSpeed;
		mKickForwardTime = kickBackDistWS / mType.mKickForwardSpeed;
		
		mReloadAnimation = new VectorAnimation(mIdlePos, mSwitchOutPos, Constants.WeaponReloadingSwitchOutInTime);
		
		mSwitchAnimation = new VectorAnimation(mIdlePos, mSwitchOutPos, Constants.WeaponSwitchOutInTime);
		// Initially in the switched-out state therefore need to advance switch animation to end as this
		// is where it will start from
		mSwitchAnimation.setCurrentTime(1.0f);
		
		mKickBackAnimation = new VectorAnimation(mIdlePos, mKickBackPos, mKickBackTime);
	}
	
	public void onResolutionChanged(Renderer renderer, int width, int height)
	{
		float fScreenWidth = (float)width;
		float fScreenHeight = (float)height;
		
		// screen size of the weapon view sprite
		ScreenCoord viewSpriteScale = weaponSpaceToScreenSpace(
				mType.mViewTexture.getWidth() / 2.0f,
				mType.mViewTexture.getHeight() / 2.0f,
				fScreenWidth, fScreenHeight);
		float spriteScaleX = viewSpriteScale.mX;
		float spriteScaleY = viewSpriteScale.mY;
		
		// Calculate the various animation point positions
		mIdlePos.set(spriteScaleX, spriteScaleY, 0.0f);
		
		mSwitchOutPos.set(mIdlePos);
		mSwitchOutPos.add(new Vector3f(0, -fScreenHeight / 2.0f, 0));
		
		mKickBackPos.set(mIdlePos);
		
		ScreenCoord kickBack = weaponSpaceToScreenSpace(mType.mKickBackOffsetX, mType.mKickBackOffsetY, fScreenWidth, fScreenHeight);
		mKickBackPos.add(new Vector3f(
				kickBack.mX,
				kickBack.mY,
				0.0f));
		
		// Reset animation end points
		mReloadAnimation.setStart(mIdlePos);
		mReloadAnimation.setEnd(mSwitchOutPos);
		
		mSwitchAnimation.setStart(mIdlePos);
		mSwitchAnimation.setEnd(mSwitchOutPos);
		
		mKickBackAnimation.setStart(mIdlePos);
		mKickBackAnimation.setEnd(mKickBackPos);
		
    	mViewSprite.setScale(spriteScaleX, spriteScaleY, 1.0f);
   
		ScreenCoord muzzlePos = weaponSpaceToScreenSpace(
				(float)mType.mMuzzlePosX, (float)mType.mMuzzlePosY,
				fScreenWidth, fScreenHeight);
		
		// calculate the size of the muzzle sprite
		ScreenCoord muzzleScale = weaponSpaceToScreenSpace(
				mType.mMuzzleFlashTexture.getWidth() / 2.0f,
				mType.mMuzzleFlashTexture.getHeight() / 2.0f,
				fScreenWidth, fScreenHeight);
		
		// muzzle position is relative to bottom left corner of muzzle sprite
		muzzlePos.mX += muzzleScale.mX;
		muzzlePos.mY += muzzleScale.mY;
		
		// subtract the muzzle centre position (muzzlePos) from the view sprite centre
		// to get the muzzle offset
		mMuzzleOffsetX = muzzlePos.mX - spriteScaleX;
		mMuzzleOffsetY = muzzlePos.mY - spriteScaleY;
		
    	mMuzzleFlashSprite.setScale(muzzleScale.mX, muzzleScale.mY, 1.0f);
	}

	public WeaponType getType()
	{
		return mType;
	}
	
	/**
	 * Renders the weapon
	 */
	public void draw(Renderer renderer)
	{
		// first determine which animation is currently playing
		Vector3f position = null;
		switch(mState)
		{
		case Idle:
			position = mIdlePos.clone();
			break;
		case FiringBack:
		case FiringForward:
			position = mKickBackAnimation.getCurrentPosition();
			break;
		case SwitchingOut:
		case SwitchedOut:
		case SwitchingIn:
			position = mSwitchAnimation.getCurrentPosition();
			break;
		case ReloadingOut:
		case ReloadingIn:
		case Reloading:
			position = mReloadAnimation.getCurrentPosition();
			break;
		}
		
		assert(position != null);
		if(mMuzzleFlashTimer > 0.0f)
		{
			Vector3f muzzlePos = position.clone();
			muzzlePos.add(mMuzzleOffsetX, mMuzzleOffsetY, 0.0f);
			if(mPosition == Position.RightSide)
			{
				muzzlePos.mX = (float)renderer.getScreenWidth() - muzzlePos.mX;
			}
			mMuzzleFlashSprite.setPosition(muzzlePos);
			mMuzzleFlashSprite.draw(renderer);
		}
		
		if(mPosition == Position.RightSide)
		{
			position.mX = (float)renderer.getScreenWidth() - position.mX;
		}
		mViewSprite.setPosition(position);
		mViewSprite.draw(renderer);
	}
	
	public void beginFire()
	{
		if(mFireTimer == 0.0f)
		{
			if(mState == State.Idle || mState == State.FiringForward || mState == State.FiringBack)
			{
				// initial shot
				shoot();
			}
		}
		mTriggerDown = true;
	}
	
	public void endFire()
	{
		mTriggerDown = false;
	}
	
	// when a bullet fires from the gun
	private void shoot()
	{	
		// only shoot if we have enough ammo
		if(mMagazineAmmoCount > 0)
		{
			mMagazineAmmoCount--;
			
			if(mFireSound != null)
			{
				mFireSound.play();
			}
			
			// this is how much time until the next bullet can be fired
			mFireTimer = mType.mFireRateRecip;
			
			mState = State.FiringBack;
			mKickBackAnimation.setAnimationTime(mKickBackTime);
			
			mMuzzleFlashTimer = mType.mMuzzleFlashDuration;
			
			if(mVibrator != null)
			{
				mVibrator.vibrate(10);
			}
			
			if(mListener != null)
			{
				mListener.shotFired(this, mType.mBulletDamage);
			}
		}
		else
		{
			// play the empty sound
			if(mEmptySound != null)
			{
				mEmptySound.play();
			}
			mFireTimer = mType.mFireRateRecip;
		}
	}
	
	public void update(float timeStep)
	{
		mMuzzleFlashTimer -= timeStep;
		if(mMuzzleFlashTimer <= 0.0f)
		{
			mMuzzleFlashTimer = 0.0f;
		}
		
		mFireTimer -= timeStep;
		if(mFireTimer <= 0.0f)
		{
			mFireTimer = 0.0f;
		}
		
		switch(mState)
		{
			case FiringBack:
			case FiringForward:
			case Idle:
			{	
				// we are ready to fire again
				if(mFireTimer == 0.0f)
				{
					// if the player still has their finger on the trigger and this is an automatic
					// weapon
					if(mTriggerDown && mType.mIsAutomatic)
					{
						// shoot again!
						shoot();
					}
				}
				
				if(mState == State.FiringBack)
				{
						// travel from 0 -> mKickBackDistance in (mFireRateRecip / 2)
						mKickBackAnimation.advance(timeStep);
						if(mKickBackAnimation.atEnd())
						{
							// kicked fully back, start going forward
							mState = State.FiringForward;
							
							mKickBackAnimation.setAnimationTime(mKickForwardTime);
							mKickBackAnimation.setCurrentTime(mKickForwardTime);
						}
				}
				else if(mState == State.FiringForward)
				{
					// kick forward speed is a lot less than kick back speed
					mKickBackAnimation.advance(-timeStep);
					if(mKickBackAnimation.atBeginning())
					{
						mState = State.Idle;
						
						// check if we need to reload
						if(mMagazineAmmoCount == 0 && (mInfiniteAmmo || mTotalAmmoCount > 0))
						{
							mState = State.ReloadingOut;
						}
					}
				}
			}
			break;
			case SwitchingOut:
			{
				mSwitchAnimation.advance(timeStep);
				if(mSwitchAnimation.atEnd())
				{
					mState = State.SwitchedOut;
					if(mListener != null)
					{
						mListener.weaponSwitchedOut(this);
					}
				}
			}
			break;
			case SwitchingIn:
			{
				mSwitchAnimation.advance(-timeStep);
				if(mSwitchAnimation.atBeginning())
				{
					mState = State.Idle;
					if(mListener != null)
					{
						mListener.weaponSwitchedIn(this);
					}
				}
			}
			break;
			case ReloadingOut:
			{
				mReloadAnimation.advance(timeStep);
				if(mReloadAnimation.atEnd())
				{	
					// Reloading!!!
					// transfer as much ammo from mTotalAmmoCount to mMagazingAmmoCount
					int maxAmount = mType.mMagazineCapacity - mMagazineAmmoCount;
					int amount;
					if(mInfiniteAmmo)
					{
						amount = maxAmount;
					}
					else
					{
						amount = Math.min(maxAmount, mTotalAmmoCount);
						mTotalAmmoCount -= amount;
					}
					mMagazineAmmoCount += amount;
					// play reload sound
					if(mReloadSound != null)
					{
						mReloadSound.play();
					}
					mState = State.Reloading;
					mReloadTimer = mType.mReloadTime;
				}
			}
			break;
			case Reloading:
			{
				// Just wait for the amount of reload time
				// (reload sound will be playing here)
				mReloadTimer -= timeStep;
				if(mReloadTimer <= 0.0f)
				{
					mState = State.ReloadingIn;
				}
			}
			break;
			case ReloadingIn:
			{
				mReloadAnimation.advance(-timeStep);
				if(mReloadAnimation.atBeginning())
				{
					mState = State.Idle;
				}
			}
			break;
			case SwitchedOut:
				// Do nothing
				break;
		}
	}
	
	public void switchOut()
	{
		if(mState == State.Idle)
		{
			mState = State.SwitchingOut;
		}
	}
	
	public void switchIn()
	{
		if(mState == State.SwitchedOut)
		{
			mState = State.SwitchingIn;
		}
	}
	
	public boolean isIdle()
	{
		return mState == State.Idle;
	}
	
	public boolean isSwitchedOut()
	{
		return mState == State.SwitchedOut;
	}
	
	public void addAmmo(int count)
	{
		mTotalAmmoCount += count;
	}
	
	public int getMagazineAmmoCount()
	{
		return mMagazineAmmoCount;
	}
	
	public int getMagazineCapacity()
	{
		return mType.mMagazineCapacity;
	}
	
	public void setPosition(Position pos)
	{
		mPosition = pos;
		if(mPosition == Position.LeftSide)
		{
			// invert tex-coords if on left side
			float[] texcoords = {
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 0.0f
			};
			mViewSprite.setTexCoords(texcoords);
			mMuzzleFlashSprite.setTexCoords(texcoords);
		}
		else
		{
			float[] texcoords = {
					1.0f, 1.0f,
					0.0f, 1.0f,
					0.0f, 0.0f,
					1.0f, 0.0f
			};
			mViewSprite.setTexCoords(texcoords);
			mMuzzleFlashSprite.setTexCoords(texcoords);
		}
	}
	
	public String getTypeName()
	{
		return mType.getName();
	}
}
