package com.craighamilton.dread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

public class Player {
	
	private final GameSession mGame;
	
	private WeaponHolster mLeftWeaponHolster;
	private WeaponHolster mRightWeaponHolster;
	
	/**
	 * All weapon blueprints (types)
	 */
	private Vector<WeaponType> mWeaponTypes = new Vector<WeaponType>();
	
	/**
	 * All weapons the player currently owns
	 */
	private Vector<Weapon> mWeapons = new Vector<Weapon>();
	
	/**
	 * Crosshair
	 */
	private Sprite mCrosshairSprite;
	
	/**
	 * Icon when player is hovering over travel location
	 */
	private Sprite mTravelSprite;
	
	private Sprite mHealthBarBackground;
	private Sprite mHealthBar;
	private boolean mTargetingEnemy = false;
	
	/**
	 * texture displayed on the heading bar when an enemy is in sight
	 */
	private Texture mEnemyBlipTexture;
	
	/**
	 * Heading Bar
	 */
	private HeadingBar mHeadingBar;
	
	/**
	 * Constructor
	 * @param game
	 * @throws JSONException
	 * @throws IOException
	 */
	public Player(GameSession game) throws JSONException, IOException
	{
		mGame = game;
		mLeftWeaponHolster = new WeaponHolster(game, this, Weapon.Position.LeftSide);
		mRightWeaponHolster = new WeaponHolster(game, this, Weapon.Position.RightSide);
		
		mEnemyBlipTexture = game.getRenderer().loadTexture("Textures/EnemyBlip.png");
		
		// Load all weapon types
		String[] weapons = null;
		weapons = game.getAssets().list("Weapons");
		for(int i = 0; i < weapons.length; ++i)
		{
			String dirname = "Weapons/" + weapons[i];
			// weapon descriptor filename
			String filename = dirname + "/" + weapons[i] + ".json";
			// load the json object
			JSONObject json = Utility.loadJSONObject(game.getAssets().open(filename));
			
			WeaponType weaponType = new WeaponType(json, game.getRenderer(), dirname);
			mWeaponTypes.add(weaponType);
		}
		
    	mCrosshairSprite = new Sprite(game.getRenderer().loadTexture("Textures/Crosshair.png"));
    	mTravelSprite = new Sprite(game.getRenderer().loadTexture("Textures/TravelIcon.png"));
    	mHeadingBar = new HeadingBar(game);
    	
    	mHealthBar = new Sprite(game.getRenderer().loadTexture("Textures/HealthBar.png"));
    	mHealthBarBackground = new Sprite(game.getRenderer().loadTexture("Textures/HealthBarBackground.png"));
	}

	/**
	 * Returns the weapon type from its name
	 * @param typeName
	 * @return
	 */
	public WeaponType getWeaponType(String typeName)
	{
		for(WeaponType type : mWeaponTypes)
		{
			if(type.mName.equals(typeName))
			{
				return type;
			}
		}
		return null;
	}
	
    public void update(float dt)
    {	
    	Weapon left = mLeftWeaponHolster.getCurrentWeapon();
    	if(left != null)
    	{
    		left.update(dt);
    	}
    	
    	Weapon right = mRightWeaponHolster.getCurrentWeapon();
    	if(right != null)
    	{
    		right.update(dt);
    	}
    	
    	mHeadingBar.update(dt);
    }
    
    public void setHeadingRotation(float rotation)
    {
    	mHeadingBar.setRotation(rotation);
    }
    
    /**
     * Called initially and whenever the screen resolution changes
     * @param renderer
     * @param width
     * @param height
     */
    public void onResolutionChanged(Renderer renderer, int width, int height)
    {
    	mCrosshairSprite.setScale(Constants.CrosshairSizeInches * renderer.getXDpi(), Constants.CrosshairSizeInches * renderer.getYDpi(), 1.0f);
    	mCrosshairSprite.setPosition(renderer.getScreenWidth() / 2.0f, renderer.getScreenHeight() / 2.0f, 0.0f);
    	
    	mTravelSprite.setScale(Constants.TravelIconSizeInches * renderer.getXDpi(), Constants.TravelIconSizeInches * renderer.getYDpi(), 1.0f);
    	mTravelSprite.setPosition(renderer.getScreenWidth() / 2.0f, renderer.getScreenHeight() / 2.0f, 0.0f);

    	mHeadingBar.onResolutionChanged(renderer, width, height);

    	mLeftWeaponHolster.onResolutionChanged(renderer, width, height);
    	mRightWeaponHolster.onResolutionChanged(renderer, width, height);
    	
    	for(int i = 0; i < mWeapons.size(); ++i)
    	{
    		mWeapons.get(i).onResolutionChanged(renderer, width, height);
    	}
    	
    	float healthBarScaleY = (Constants.HealthBarHeightInches * renderer.getYDpi()) / 2.0f;
    	float healthBarAspect = mHealthBarBackground.getTexture().getWidth() /
    									(float)mHealthBarBackground.getTexture().getHeight();
    	
    	float healthBarOffsetY = Constants.HealthBarYOffsetInches * renderer.getYDpi();
    	
    	mHealthBarBackground.setScale(healthBarScaleY * healthBarAspect, healthBarScaleY, 0.0f);
    	mHealthBarBackground.setPosition(
    			width * 0.5f, height - (healthBarScaleY + healthBarOffsetY), 0.0f);
    }
    
    public enum CrosshairType
    {
    	None,
    	Aim,
    	Travel
    }
    
    public void draw(Renderer renderer, CrosshairType crosshairType)
    {
    	Utility.RenderState2D rs = Utility.begin2DDraw(renderer, (float)renderer.getScreenWidth(), (float)renderer.getScreenHeight());
    	mLeftWeaponHolster.draw(renderer);
    	mRightWeaponHolster.draw(renderer);

    	switch(crosshairType)
    	{
    	case None:
    		break;
    	case Aim:
    		mCrosshairSprite.draw(renderer);
    		break;
    	case Travel:
    		mTravelSprite.draw(renderer);
    		break;
    	}

    	mHeadingBar.draw(renderer);
    	
    	if(mTargetingEnemy)
    	{
	    	mHealthBarBackground.draw(renderer);
	    	mHealthBar.draw(renderer);
    	}
    	
    	Utility.end2DDraw(rs);
    }
    
    public GameSession getGame()
    {
    	return mGame;
    }
    
    public void beginLeftWeaponFire()
    {
    	Weapon weapon = mLeftWeaponHolster.getCurrentWeapon();
    	if(weapon != null)
    	{
    		weapon.beginFire();
    	}
    }
    
    public void endLeftWeaponFire()
    {
    	Weapon weapon = mLeftWeaponHolster.getCurrentWeapon();
    	if(weapon != null)
    	{
    		weapon.endFire();
    	}
    }
    
    public void beginRightWeaponFire()
    {
    	Weapon weapon = mRightWeaponHolster.getCurrentWeapon();
    	if(weapon != null)
    	{
    		weapon.beginFire();
    	}
    }
    
    public void endRightWeaponFire()
    {
    	Weapon weapon = mRightWeaponHolster.getCurrentWeapon();
    	if(weapon != null)
    	{
    		weapon.endFire();
    	}
    }
    
    private WeaponType findWeaponTypeFromName(String name)
    {
    	for(int i = 0; i < mWeaponTypes.size(); ++i)
    	{
    		WeaponType type = mWeaponTypes.get(i);
    		if(type.getName().equals(name))
			{
    			return type;
			}
    	}
    	return null;
    }
    
    public int getWeaponCount()
    {
    	return mWeapons.size();
    }
    
    public Weapon getWeapon(int index)
    {
    	return mWeapons.get(index);
    }
    
    /**
     * Called when the player has discovered/unlocked a new weapon
     * @param weapontype the weapon type name
     * @throws IOException 
     * @throws JSONException 
     */
    public void weaponUnlocked(String weaponTypeName) throws JSONException, IOException
    {
    	WeaponType type = findWeaponTypeFromName(weaponTypeName);
    	if(type != null)
    	{
			// create a new weapon of this type
			Weapon weapon = new Weapon(type, mGame);
			// add to mWeapons
			mWeapons.add(weapon);
			
			// if their is an empty space somewhere
			if(mRightWeaponHolster.getCurrentWeaponIndex() == -1)
			{
				mRightWeaponHolster.trySwitchToWeapon(mWeapons.size()-1);
			}
			else if(mLeftWeaponHolster.getCurrentWeaponIndex() == -1)
			{
				mLeftWeaponHolster.trySwitchToWeapon(mWeapons.size()-1);
			}
			else
			{
				// else just automatically switch the right holster to this
				// newly acquired weapon
				mRightWeaponHolster.trySwitchToWeapon(mWeapons.size()-1);
			}
    	}
    }
    
    public void switchRightWeaponUp()
    {	
    	mRightWeaponHolster.switchWeaponUp();
    }
    
    public void switchLeftWeaponUp()
    {
    	mLeftWeaponHolster.switchWeaponUp();
    }
    
    // callback from weapon holster when a shot has been fired
    public void shotFired(int damage)
    {
    	mGame.shotFired(damage);
    }
    
    public void save(SharedPreferences.Editor edit)
    {
    	edit.putInt("left_weapon_index", mLeftWeaponHolster.getCurrentWeaponIndex());
    	edit.putInt("right_weapon_index", mRightWeaponHolster.getCurrentWeaponIndex());
    	// save all weapons as an array of type names
    	Vector<String> typeNames = new Vector<String>();
    	for(Weapon weapon : mWeapons)
    	{
    		typeNames.add(weapon.getTypeName());
    	}
    	Utility.saveStringArray(edit, "weapons", typeNames);
    }
    
    public void load(SharedPreferences saveGame) throws JSONException, IOException
    {
    	// load mWeapons
    	Vector<String> weaponTypeNames = Utility.loadStringArray(saveGame, "weapons");
    	for(String typeName : weaponTypeNames)
    	{
    		WeaponType type = findWeaponTypeFromName(typeName);
    		if(type != null)
    		{
	    		// create a new weapon based on type
				Weapon weapon = new Weapon(type, mGame);
				// add to mWeapons
				mWeapons.add(weapon);
    		}
    	}
    	
    	int leftWeaponIndex = saveGame.getInt("left_weapon_index", -1);
    	int rightWeaponIndex = saveGame.getInt("right_weapon_index", -1);
    	
    	mLeftWeaponHolster.trySwitchToWeapon(leftWeaponIndex);
    	mRightWeaponHolster.trySwitchToWeapon(rightWeaponIndex);
    }
    
    private HashMap<Enemy, HeadingBar.SpriteItem> mEnemyBlips = new HashMap<Enemy, HeadingBar.SpriteItem>();
    
    /**
     * Called when an enemy is spawned in the current sector
     * @param enemy
     */
    public void enemySpawned(Enemy enemy, float rotation)
    {
    	HeadingBar.SpriteItem enemyBlip = new HeadingBar.SpriteItem(mEnemyBlipTexture, rotation, true);
    	mEnemyBlips.put(enemy, enemyBlip);
    	mHeadingBar.addSpriteItem(enemyBlip);
    }
    
    /**
     * Called when an enemy is removed from the current sector (dies)
     * @param enemy
     */
    public void enemyDied(Enemy enemy)
    {
    	HeadingBar.SpriteItem item = mEnemyBlips.get(enemy);
    	if(item != null)
    	{
    		mHeadingBar.removeSpriteItem(item);
    	}
    	mEnemyBlips.remove(enemy);
    }
    
    
    /**
     * 
     */
    public void setCurrentTargetedEnemy(Enemy enemy)
    {
    	if(enemy != null)
    	{
	    	float ratio = enemy.getHealth() / (float)enemy.getInitialHealth();
	    	
	    	// x scale of the health bar
	    	Vector3f backgroundScale = mHealthBarBackground.getScale();
	    	float scaleX = backgroundScale.mX * ratio;
	    	float scaleY = backgroundScale.mY;
	    	
	    	mHealthBar.setScale(scaleX, scaleY, 0.0f);
	    	mHealthBar.setPosition(mHealthBarBackground.getPosition());
	    	mTargetingEnemy = true;
	    	
	    	// calculate texture coordinates
	    	float uLeft = (1.0f - ratio) * 0.5f;
	    	float uRight = (ratio * 0.5f) + 0.5f;
	    	
	    	mHealthBar.setTexCoords(new float[]{
					uLeft, 1.0f,
					uRight, 1.0f,
					uRight, 0.0f,
					uLeft, 0.0f
	    	});
    	}
    	else
    	{
    		mTargetingEnemy = false;
    	}
    }
}
