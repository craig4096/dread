package com.craighamilton.dread;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.Log;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameSession extends Activity implements Enemy.Listener, SensorInput.Listener
{
	// the game's rendering class
	private Renderer mRenderer;
	private GLSurfaceView mSurfaceView;
	
	private Player mPlayer;
	private Camera mCamera;
	
	/**
	 * Background music
	 */
	private boolean mBackgroundMusicEnabled;
	private BackgroundMusic mBackgroundMusic;
	
	private boolean mHealthBarsEnabled;
	
	
	/**
	 * For all in game sounds
	 */
	private SoundPool mSoundPool = null;
	private boolean mSoundsEnabled;
	private Map<String, Integer> mSoundCache = new HashMap<String, Integer>();
	
	public SoundPool getSoundPool()
	{
		return mSoundPool;
	}
	
	public Sound loadSound(String name) throws IOException
	{
		if(mSoundsEnabled)
		{
			if(mSoundCache.containsKey(name))
			{
				return new Sound(this, mSoundCache.get(name));
			}
			
			int soundId = mSoundPool.load(getAssets().openFd("Sounds/" + name + ".ogg"), 0);
			mSoundCache.put(name, soundId);
			return new Sound(this, soundId);
		}
		return null;
	}
	
	
	/**
	 * Global resources
	 */
	private Texture mWeaponLocationTexture;
	private Texture mAmmoBarBackgroundTexture;
	private Texture mEnemyDropShadowTexture;

	/**
	 * Current view rotation matrix from the device sensor
	 */
	private Matrix3f mSensorViewRotationMatrix = Utility.IdentityMatrix3();
	private synchronized void setSensorViewRotationMatrix(Matrix3f mat)
	{
		mSensorViewRotationMatrix = mat;
	}
	private synchronized Matrix3f getSensorViewRotationMatrix()
	{
		return mSensorViewRotationMatrix;
	}
	
	/**
	 * Door the player is currently hovering over, null if none
	 */
	private Door mCurrentDoorHover = null;
	
	/**
	 * Weapon location the player is currently hovering over
	 */
	private WeaponLocation mCurrentWeaponHover = null;
	
	/**
	 * The name of the starting sector for this level
	 */
	private String mStartingSectorName;
	
	/**
	 * The name of the last sector that the player was in, empty
	 * if the current sector is the first sector the player has visited
	 */
	private String mLastSectorName = "";
	
	/**
	 * The current loaded sector
	 */
	private Sector mCurrentSector = null;
	private String mCurrentSectorName;
	
	
	/**
	 * list of all weapon locations that have been unlocked
	 * these strings are of the format: SectorName.WeaponLocationIndex
	 * and are used purely to determine whether a location has already been unlocked
	 * and also in the save game
	 */
	private Set<String> mUnlockedWeaponLocations;
	
	/**
	 * Names of all sectors that have been cleared by the player
	 * (Stored in the save file)
	 */
	private Set<String> mClearedSectors;
	
	private boolean mCurrentSectorCleared = false;
	
	/**
	 * Names of sectors whose cutscenes have already played
	 */
	private Set<String> mCutscenesPlayed;
	
	/**
	 * Class for managing touch input events
	 */
	private TouchInput mTouchInput;
	
	/**
	 * Class for managing sensor input
	 */
	private SensorInput mSensorInput;
	
	/**
	 * Set to true when the player has died
	 */
	private boolean mGameOver = false;
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mSensorInput = new SensorInput(this, this);
        mTouchInput = new TouchInput(this, getResources().getDisplayMetrics().xdpi);
        
        // Create the renderer object
        mRenderer = new Renderer(this);
        
        mSurfaceView = new GLSurfaceView(this);
        mSurfaceView.setOnTouchListener(mTouchInput);
        
    	// load global resources
    	mWeaponLocationTexture = mRenderer.loadTexture("Textures/WeaponLocation.png");
    	mAmmoBarBackgroundTexture = mRenderer.loadTexture("Textures/AmmoBarBackground.png");
    	mEnemyDropShadowTexture = mRenderer.loadTexture("Textures/EnemyDropShadow.png");
        
        // Create Camera
        mCamera = new Camera(Constants.CameraYFov, 1.0f /* Aspect ratio set later on in onSurfaceChanged() */,
        		Constants.CameraZNear, Constants.CameraZFar);
        
        // Sound management
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mSoundsEnabled = prefs.getBoolean("sounds_enabled", false);
		mBackgroundMusicEnabled = prefs.getBoolean("music_enabled", false);
		if(mBackgroundMusicEnabled)
		{
			mBackgroundMusic = new BackgroundMusic(this);
		}
        if(mSoundsEnabled)
        {
        	mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        
		mHealthBarsEnabled = prefs.getBoolean("enemy_health_bars_visible", false);
        
        // Create Player
		try {
			
			mPlayer = new Player(this);
			
			// load the game
			loadGame();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        mSurfaceView.setRenderer(mRenderer);
        setContentView(mSurfaceView);
    }
    
    @Override
    protected void onResume()
    {
    	mSensorInput.onResume();
    	mSurfaceView.onResume();
    	
    	if(mBackgroundMusicEnabled && mPauseAudioOnPause)
    	{
    		mBackgroundMusic.onResume();
    	}
    
    	super.onResume();
    }
    
    private boolean mPauseAudioOnPause = false;
    @Override
    protected void onPause()
    {
    	// Save game here
    	saveGame();
    	
    	mSurfaceView.onPause();
    	mSensorInput.onPause();
    	
    	if(mBackgroundMusicEnabled && mPauseAudioOnPause)
    	{
    		mBackgroundMusic.onPause();
    	}
    	super.onPause();
    }
    
    @Override
    protected void onStop()
    {
    	// release all resources here
    	super.onStop();
    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    }
    
    @Override
    protected void onDestroy()
    {
		if(mBackgroundMusicEnabled)
		{
			mBackgroundMusic.onDestroy();
		}
    	
    	synchronized(this)
    	{	
	    	if(mSoundPool != null)
	    	{
	    		mSoundPool.release();
	    		mSoundPool = null;
	    	}
    	}
    	super.onDestroy();
    }
    
    /**
     * Save the game to Shared Preferences
     */
    private void saveGame()
    {
    	// Bug: mClearedSectors being modified here in main thread and also in GameSession.update() thread
    	// method therefore synchronise all access/modification to both mUnlockedWeaponLocations
    	// and mClearedSectors
    	synchronized(this)
    	{
	    	SharedPreferences saveGame = getSharedPreferences(PersistVars.SaveGame, Context.MODE_PRIVATE);
	    	SharedPreferences.Editor edit = saveGame.edit();
	    	edit.putString("last_sector", mLastSectorName);
	    	if(mGameOver)
	    	{
	    		edit.putString("sector", mLastSectorName);
	    	}
	    	else
	    	{
	    		edit.putString("sector", mCurrentSectorName);
	    	}
	    	// save unlocked weapons
	    	Utility.putStringSet(edit, "unlocked_weapons", mUnlockedWeaponLocations);
	    	Utility.putStringSet(edit, "cleared_sectors", mClearedSectors);
	    	// save player state
	    	mPlayer.save(edit);
	    	edit.commit();
    	}
    }
    
    /**
     * Load the game from Shared Preferences
     * @throws IOException
     * @throws JSONException
     */
    private void loadGame() throws IOException, JSONException
    {
		// load the map from the input file stream
		JSONObject json = Utility.loadJSONObject(getAssets().open("game.json"));
		
		mStartingSectorName = json.getString("start_sector");
		
    	SharedPreferences saveGame = getSharedPreferences(PersistVars.SaveGame, Context.MODE_PRIVATE);
    	String sectorName = saveGame.getString("sector", "none");
    	mLastSectorName = saveGame.getString("last_sector", "");
    	
    	// load all sectors that have been cleared
    	mClearedSectors = Utility.getStringSet(saveGame, "cleared_sectors");
    	
    	if(sectorName.equals("none"))
    	{
    		loadSector(mStartingSectorName);
    	}
    	else
    	{
    		loadSector(sectorName);
    	}
    	
    	// load unlocked weapons
    	mUnlockedWeaponLocations = Utility.getStringSet(saveGame, "unlocked_weapons");
    	
    	mPlayer.load(saveGame);

    	/*
    	mPlayer.weaponUnlocked("ChainGun");
    	mPlayer.weaponUnlocked("GrenadeLauncher");
    	mPlayer.weaponUnlocked("CombatShotgun");
    	mPlayer.weaponUnlocked("Magnum");
    	mPlayer.weaponUnlocked("Sniper");
    	mPlayer.weaponUnlocked("Rifle");
    	mPlayer.weaponUnlocked("MachineGun");
    	mPlayer.weaponUnlocked("Uzi");
    	mPlayer.weaponUnlocked("Shotgun");
    	mPlayer.weaponUnlocked("Colt45");
    	mPlayer.weaponUnlocked("Laser");
    	*/
    }
    
    private synchronized boolean checkForCutsceneAndActivate()
    {
    	SharedPreferences prefs = getSharedPreferences(PersistVars.SaveGame, Context.MODE_PRIVATE);
    	Set<String> cutscenesPlayed = Utility.getStringSet(prefs, "cutscenes");
    	
    	boolean cutscenePlayed = cutscenesPlayed.contains(mCurrentSector.getName());
    	
   		if(!cutscenePlayed)
		{
   			Intent intent = null;
			// check if we should display a cutscene
			if(mCurrentSector.getIntroCutsceneVideoUri().length() > 0)
			{
				intent = new Intent(this, Cutscene.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				intent.putExtra(PersistVars.Cutscene_VideoUri, "Videos/" + mCurrentSector.getIntroCutsceneVideoUri());
				synchronized(this)
				{
					mPauseAudioOnPause = true;
				}
			}
			else if(mCurrentSector.getIntroCutsceneText().length() > 0)
			{
				intent = new Intent(this, CutsceneText.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				intent.putExtra(PersistVars.CutsceneText_FadeTimeMs, Constants.CutsceneTextFadeTimeMs);
				intent.putExtra(PersistVars.CutsceneText_WaitTimeMs, Constants.CutsceneTextWaitTimeMs);
				
				int resId = getResources().getIdentifier(
						mCurrentSector.getIntroCutsceneText(), "string", getPackageName());
				if(resId > 0)
				{
			    	intent.putExtra(PersistVars.CutsceneText_Text, getResources().getString(resId));
				}
				else
				{
					intent.putExtra(PersistVars.CutsceneText_Text, "<Localise>");
				}
				synchronized(this)
				{
					mPauseAudioOnPause = false;
				}
			}
			
			if(intent != null)
			{
				// now save to sectorsPlayed
				cutscenesPlayed.add(mCurrentSector.getName());
				SharedPreferences.Editor edit = prefs.edit();
				Utility.putStringSet(edit, "cutscenes", cutscenesPlayed);
				edit.commit();
				
				startActivity(intent);
				return true;
			}
		}
   		return false;
    }
    
    /**
     * Loads a specific sector into memory
     * @param sectorName
     */
	public boolean loadSector(String sectorName)
	{
		boolean sectorCleared;
		synchronized(this) {
			sectorCleared = mClearedSectors.contains(sectorName);
		}
		Sector sector;
		try {
			sector = new Sector(this, "Sectors/" + sectorName, sectorCleared);
		} catch (Exception e) {
			return false;
		}
		
		if(mCurrentSector != null)
		{
			mLastSectorName = mCurrentSector.getName();
			mCurrentSector.unload();
		}
		
		mCurrentSector = sector;
		mCurrentSectorName = mCurrentSector.getName();
		mCurrentSectorCleared = sectorCleared;
		mCurrentDoorHover = null;
		// update the camera position
		mCamera.setPosition(mCurrentSector.getPosition());
		
		// play any cut-scenes this sector may have
		checkForCutsceneAndActivate();
		return true;
	}
    
    /**
     * Callback from the renderer
     */
    public void onSurfaceChanged(int width, int height)
    {
    	mPlayer.onResolutionChanged(mRenderer, width, height);
    	mCamera.setAspectRatio(width / (float)height);
    }
    
    public void update(float dt)
    {	
    	// Input
    	TouchInput.Event event = mTouchInput.getNextEvent();
    	while(event != null)
    	{
    		switch(event)
    		{
    		case Action:
    			onActionPressed();
    			break;
    		case SwitchLeftWeapon:
    			mPlayer.switchLeftWeaponUp();
    			break;
    		case SwitchRightWeapon:
    			mPlayer.switchRightWeaponUp();
    			break;
    		case BeginFireLeftWeapon:
    			mPlayer.beginLeftWeaponFire();
    			break;
    		case EndFireLeftWeapon:
    			mPlayer.endLeftWeaponFire();
    			break;
    		case BeginFireRightWeapon:
    			mPlayer.beginRightWeaponFire();
    			break;
    		case EndFireRightWeapon:
    			mPlayer.endRightWeaponFire();
    			break;
    		}
    		
    		event = mTouchInput.getNextEvent();
    	}
    	
    	mTouchInput.update(dt);
    	
    	mPlayer.setHeadingRotation(mCamera.getYawRotation());
    	
    	// Update player
    	mPlayer.update(dt);
    	
    	// update the weapon hover animation if currently hovering over a weapon location
    	if(mCurrentWeaponHover != null)
    	{
    		mCurrentWeaponHover.update(dt);
    	}

		if(mCurrentSector != null)
		{
			// Update sector
			mCurrentSector.update(dt);
			
			// if no more enemies in sector
			if(mCurrentSector.allEnemiesDead())
			{
				if(!mCurrentSectorCleared)
				{
					// Synchronise all access to mClearedSectors (ConcurrentModificationException when calli
					synchronized(this)
					{
						mClearedSectors.add(mCurrentSectorName);
					}
					mCurrentSectorCleared = true;
				}
				
		    	// check if the player is hovering the aim cursor over a door or weapon location
				IntersectableObject obj = mCurrentSector.findObject(mCamera.getViewDirection(),
						EnumSet.of(Sector.TraceCategory.Door, Sector.TraceCategory.WeaponLocation), null);
				if(obj != null)
				{
					if(obj instanceof Door)
					{
						if((Door)obj != mCurrentDoorHover)
						{
							mCurrentDoorHover = (Door)obj;
						}
					}
					else if(obj instanceof WeaponLocation)
					{
						WeaponLocation loc = (WeaponLocation)obj;
						String id = mCurrentSectorName + "." + loc.getIndex();
						
						boolean weaponUnlocked;
						synchronized(this){
							weaponUnlocked = mUnlockedWeaponLocations.contains(id);
						}
						// Only highlight the weapon location if not unlocked
						if(!weaponUnlocked)
						{
							if(loc != mCurrentWeaponHover)
							{
								mCurrentWeaponHover = (WeaponLocation)obj;
							}
						}
					}
				}
				else
				{
					mCurrentDoorHover = null;
					mCurrentWeaponHover = null;
				}
			}
			else
			{
				if(mHealthBarsEnabled)
				{
					IntersectableObject obj = mCurrentSector.findObject(mCamera.getViewDirection(),
							EnumSet.of(Sector.TraceCategory.Enemy),
							new IntersectionQuery.SearchFilter() {
								@Override
								public boolean test(IntersectableObject obj)
								{
									if(obj instanceof Enemy)
									{
										Enemy enemy = (Enemy)obj;
										return !enemy.isDead();
									}
									return false;
								}
							});
					
					mPlayer.setCurrentTargetedEnemy((Enemy)obj);
				}
			}
		}
		
		if(mBackgroundMusicEnabled)
		{
			mBackgroundMusic.update(dt);
		}
    }
    
    public void draw(Renderer renderer)
    {
    	// Calculate the cameras view matrix rotation based on swipe rotation,
    	// initial yaw rotation of the device and the current orientation of the device
    	Matrix3f viewRot;
    	if(mSensorInput.isEnabled())
    	{
    		viewRot = getSensorViewRotationMatrix().clone();
    	}
    	else
    	{
    		viewRot = Utility.CreateXRotationMatrix((float)Math.toRadians(-90.0f));
    	}
    	viewRot.mul(Utility.CreateZRotationMatrix(mTouchInput.getYawRotation()));
    	if(mSensorInput.isEnabled())
    	{
    		// world will be rotated to account for the initial phone position
    		viewRot.mul(mSensorInput.getBaseRotationMatrix());
    	}
    	mCamera.setViewMatrixRotation(viewRot);
  
    	mRenderer.setProjectionMatrix(mCamera.getProjectionMatrix());
    	mRenderer.setViewMatrix(mCamera.getViewMatrix());
    	
    	// Render the current sector
		if(mCurrentSector != null)
		{
			mCurrentSector.draw(renderer);
		}
		
    	// if the player is hovering their cursor over a weapon location
    	if(mCurrentWeaponHover != null)
    	{
    		mCurrentWeaponHover.draw(renderer);
    	}
    	
    	// render player items (weapons, health etc.)
    	Player.CrosshairType crosshairType = Player.CrosshairType.Aim;
    	if(mCurrentDoorHover != null)
    	{
    		crosshairType = Player.CrosshairType.Travel;
    	}
    	else if(mCurrentWeaponHover != null)
    	{
    		crosshairType = Player.CrosshairType.None;
    	}
    	mPlayer.draw(renderer, crosshairType);
    }
	
	private static final int REQUEST_WEAPON_DISPLAY = 0;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode == REQUEST_WEAPON_DISPLAY)
		{
			String weaponType = intent.getExtras().getString("weapon_type");
			int weaponIndex = intent.getExtras().getInt("weapon_index");
			// construct the weapon location absolute id
			String id = mCurrentSectorName + "." + weaponIndex;
			synchronized(this) { // see onPause
				mUnlockedWeaponLocations.add(id);
			}
			// user has unlocked a specific weapon, switch to the weapon now
			try
			{
				mPlayer.weaponUnlocked(weaponType);
			}
			catch (Exception e)
			{
				//Log.d("", "Weapon type could not be loaded: " + weaponType);
			}
			mCurrentWeaponHover = null;
		}
	}
	
	public void shotFired(int damage)
	{
		// cast ray to see if we hit an enemy
		Enemy enemy = findEnemy(mCamera.getViewDirection());
		if(enemy != null)
		{
			enemy.takeHit(damage);
		}
	}
	
	public Enemy findEnemy(Vector3f viewDir)
	{
		return (Enemy)mCurrentSector.findObject(viewDir, EnumSet.of(Sector.TraceCategory.Enemy), new IntersectionQuery.SearchFilter() {
			@Override
			public boolean test(IntersectableObject obj)
			{
				assert(obj instanceof Enemy);
				Enemy enemy = (Enemy)obj;
				return !enemy.isDead();
			}
		});
	}

	@Override
	public void enemyReachedDestination()
	{
		// Game Over: enemy has reached player
		if(mGameOver == false)
		{
			mGameOver = true;
			// Start the game over screen activity
			Intent intent = new Intent(this, GameOverScreen.class);
			finish();
			startActivity(intent);
		}
	}
	
	public Renderer getRenderer()
	{
		return mRenderer;
	}

	public void onActionPressed()
	{
		if(mCurrentDoorHover != null)
		{
			// need to pick this up on the rendering thread, previously causing bugs
			// when loading opengl resources in main thread
			String nextSector = mCurrentDoorHover.getLinksToSectorName();
			if(nextSector.equals("EndGame"))
			{
				// This allows the game to start again with the player retaining all
				// weapons (This is the only reason we separate the mCurrentSectorName from
				// mCurrentSector.getName() - so we can adjust the save game - saveGame will be
				// called when the activity finishes)
				mLastSectorName = "";
				mCurrentSectorName = mStartingSectorName;
				synchronized(this) {
					mClearedSectors.clear();
				}
				
				Intent intent = new Intent(this, CreditsScreen.class);
				startActivity(intent);
				finish();
			}
			else
			{
				loadSector(mCurrentDoorHover.getLinksToSectorName());
			}
		}
		else if(mCurrentWeaponHover != null)
		{
			// Start a new activity that shows the weapon stats
			Intent intent = new Intent(this, WeaponDisplay.class);
			intent.putExtra("weapon_type", mCurrentWeaponHover.getWeaponType());
			intent.putExtra("weapon_index", mCurrentWeaponHover.getIndex());
			
			
			WeaponType type = mPlayer.getWeaponType(mCurrentWeaponHover.getWeaponType());
			if(type != null)
			{
				intent.putExtra("weapon_damage", type.mBulletDamage);
				intent.putExtra("weapon_firerate", type.mFireRate);
				intent.putExtra("weapon_reload_time", type.mReloadTime);
				intent.putExtra("weapon_capacity", type.mMagazineCapacity);
				intent.putExtra("weapon_automatic", type.mIsAutomatic);
				intent.putExtra("ammo_image", type.mAmmoTextureName);
			}
			synchronized(this)
			{
				// don't pause audio during weapon display
				mPauseAudioOnPause = false;
			}
			startActivityForResult(intent, REQUEST_WEAPON_DISPLAY);
		}
	}
	
	@Override
	public void updateViewMatrix(Matrix3f viewRot)
	{
		setSensorViewRotationMatrix(viewRot);
	}
	
	public int getScreenWidth()
	{
		return mSurfaceView.getWidth();
	}
	
	public int getScreenHeight()
	{
		return mSurfaceView.getHeight();
	}
	
	public Player getPlayer()
	{
		return mPlayer;
	}
	
	private HashMap<String, EnemyType> mCachedEnemyTypes = new HashMap<String, EnemyType>();
	public EnemyType getOrLoadEnemyType(String enemyType) throws IOException, JSONException
	{
		EnemyType found = mCachedEnemyTypes.get(enemyType);
		if(found != null)
		{
			return found;
		}
		EnemyType loaded = new EnemyType(this, enemyType);
		mCachedEnemyTypes.put(enemyType, loaded);
		return loaded;
	}
	
	public void setBackgroundMusic(String filename) throws IOException
	{
		if(mBackgroundMusicEnabled)
		{
			mBackgroundMusic.transitionTo(filename);
		}
	}
	
	/**
	 * Global resource accessors
	 */
	public Texture getWeaponLocationTexture()
	{
		return mWeaponLocationTexture;
	}
	public Texture getAmmoBarBackgroundTexture()
	{
		return mAmmoBarBackgroundTexture;
	}
	public Texture getEnemyDropShadowTexture()
	{
		return mEnemyDropShadowTexture;
	}
}
