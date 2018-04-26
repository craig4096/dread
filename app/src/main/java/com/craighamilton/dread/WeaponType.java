package com.craighamilton.dread;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class WeaponType
{
	public final String mName;
	
	public final String mDirectoryPath;
	
	public final Texture mViewTexture;
	public final Texture mMuzzleFlashTexture;
	
	public final Texture mAmmoTexture;
	
	public final String mAmmoTextureName;
	
	/**
	 * Shots per second, this should be zero if the weapon is not automatic
	 */
	public final float mFireRate;
	public final float mFireRateRecip; // cached value
	
	public final float mMuzzleFlashDuration;
	
	/**
	 * how far back the weapon will kick when fired, units are
	 * in (virtual weapon coordinates) @see Constants.WeaponTexture(Width|Height)
	 */
	//public final float mKickBackDistance;
	
	public final float mKickBackOffsetX;
	public final float mKickBackOffsetY;
	
	/**
	 * Kick back speed of the weapon, units (virtual weapon coordinates)
	 */
	public final float mKickBackSpeed;
	
	/**
	 * Kick forward speed in inches per second
	 */
	public final float mKickForwardSpeed;
	
	/**
	 * Is this an automatic weapon or not
	 */
	public final boolean mIsAutomatic;


	public final float mReloadTime;
	
	public final int mMagazineCapacity;
	public final int mBulletDamage;
	
	/**
	 * the Centre position of the muzzle sprite relative to the
	 * weapon view texture
	 */
	public final int mMuzzlePosX;
	public final int mMuzzlePosY;
	
	/**
	 * Weapon sounds
	 */
	public final String mFireSound;
	public final String mReloadSound;
	public final String mEmptySound;
	
	/**
	 * Each weapon in the game has a specific index slot
	 * which is used when scrolling through the different weapons
	 * at runtime
	 */
	//private final int mIndexSlot;
	
	/**
	 * Constructor
	 * @param game
	 * @param json
	 * @param dirname
	 * @throws JSONException
	 * @throws IOException
	 */
	public WeaponType(JSONObject json, Renderer renderer, String dirname) throws JSONException, IOException
	{
		mDirectoryPath = dirname;
		mName = dirname.substring(dirname.lastIndexOf('/')+1);
		mIsAutomatic = json.getBoolean("automatic");
		//mKickBackDistance = (float)json.getDouble("kick_back_dist");
		
		mKickBackOffsetX = (float)json.getDouble("kick_back_offset_x");
		mKickBackOffsetY = (float)json.getDouble("kick_back_offset_y");
		mFireRate = (float)json.getDouble("fire_rate");
		mFireRateRecip = 1.0f / mFireRate;
		mMagazineCapacity = json.getInt("mag_capacity");
		mBulletDamage = json.getInt("bullet_damage");
		mReloadTime = (float)json.getDouble("reload_time");
		mKickBackSpeed = (float)json.getDouble("kick_back_speed");
		mKickForwardSpeed = (float)json.getDouble("kick_forward_speed");
		
		mMuzzleFlashDuration = (float)json.getDouble("muzzle_flash_duration");
		
		mAmmoTextureName = json.getString("ammo_texture");
		
		mViewTexture = renderer.loadTexture(dirname + "/view.png", Texture.WrapMode.ClampToEdge);
		mMuzzleFlashTexture = renderer.loadTexture(dirname + "/muzzle.png", Texture.WrapMode.ClampToEdge);
		mAmmoTexture = renderer.loadTexture("Textures/" + json.getString("ammo_texture") + ".png");
		
		mMuzzlePosX = json.getInt("muzzle_x");
		mMuzzlePosY = json.getInt("muzzle_y");
		
		mReloadSound = json.getString("reload_sound");
		mFireSound = json.getString("fire_sound");
		mEmptySound = json.getString("empty_sound");
	}
	
	public String getName()
	{
		return mName;
	}
	
	/*
	public int getIndexSlot()
	{
		return mIndexSlot;
	}
	*/
}
