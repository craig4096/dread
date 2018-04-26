package com.craighamilton.dread;

public class Constants {

	public static final float CrosshairSizeInches = 0.05f;
	public static final float TravelIconSizeInches = 0.1f;
	
	public static final float AmmoBarWidthInches = 0.2f;
	public static final float AmmoBarEdgeInches = 0.1f;
	public static final float AmmoBarHeightRatio = 0.8f;
	
	public static final float AlertBarWidthInches = 0.05f;
	
	public static final float EnemyHealthBarHeightMetres = 0.1f;
	
	public static final float PlayerEyeHeightMetres = 1.8f;
	
	public static final float WeaponHighlightSize = 0.1f;
	
	public static final float HeadingBarHeightRatio = 0.1f;
	
	public static final float PitchBarWidthRatio = 0.05f;

	
	public static final int WeaponTextureWidth = 960;
	public static final int WeaponTextureHeight = 540;
	
	/**
	 * How close (metres) the enemy needs to be to the player to trigger game over
	 */
	public static final float EnemyGameOverProximity = 2.0f;
	
	
	public static final float EnemyFadeInOutTime = 1.5f;
	
	/**
	 * Crosshair colours
	 */
	public static final Colour CrosshairDoorHoverColour = new Colour(0.5f, 0.0f, 0.0f);
	
	public static final Colour CrosshairWeaponHoverColour = new Colour(0.0f, 0.0f, 0.5f);
	
	public static final Colour CrosshairIdleColour = new Colour(0.0f, 0.0f, 0.0f);
	
	/**
	 * The padding that differentiates the ammo bar texture from its background
	 * specified in pixels relative the background image
	 */
	public static final int AmmoBarTexturePadding = 4;
	
	/**
	 * Maximum number of slots in the ammo background texture
	 */
	public static final int AmmoBarMaxCapacity = 25;
	
	public static final int SensorAverageSampleCount = 30;
	
	public static final float HeadingBarAlertFlashesPerSec = 2.0f;
	
	public static final float HeadingBarVisibleRotationRange = (float)(Math.PI / 2.0);
	
	public static final float WeaponIconRotateSpeed = 0.25f;
	
	public static final float ActionWidthRatio = 0.2f;
	public static final float ActionHeightRatio = 0.2f;
	
	
	/**
	 * Time it takes for a weapon to switch in/out when reloading
	 */
	public static final float WeaponReloadingSwitchOutInTime = 0.5f;
	/**
	 * Time in takes for a weapon to switch in/out when switching between weapons
	 */
	public static final float WeaponSwitchOutInTime = 0.5f;
	
	
	public static final int MaxEnemiesAlive = 5;
	
	
	public static final float EnemyHurtSoundHealthFraction = 1.0f / 4.0f;
	
	public static final float CameraYFov = 75.0f;
	public static final float CameraZNear = 0.1f;
	public static final float CameraZFar = 100.0f;
	
	public static final int CutsceneTextFadeTimeMs = 1000;
	public static final int CutsceneTextWaitTimeMs = 4000;
	
	
	public static final int SplashScreenFadeInTimeMs = 4000;
	public static final int SplashScreenFadeOutTimeMs = 4000;
	
	
	public static final float HealthBarHeightInches = 0.1f;
	public static final float HealthBarYOffsetInches = 0.1f;
	
	
	/**
	 * The maximum possible rotational velocity the camera can rotate
	 * when swiping, defined in radians
	 */
	public static final float MaxSwipeVelocity = (float)Math.PI;
	
	/**
	 * The range (+ or -) at which point the swiping velocity hits maximum
	 * from when the player initially touches the swipe bar 
	 */
	public static final float SwipeVelocityRangeInches = 1.5f;
	
	/**
	 * How much swipe velocity will be reduced by each frame
	 */
	public static final float SwipeVelocityDampingCoefficient = 0.8f;
	
	
	public static final float BackgroundMusicFadeTime = 2.0f;
	
}
