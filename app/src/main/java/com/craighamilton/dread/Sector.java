package com.craighamilton.dread;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import android.util.Log;

/**
 * Sectors are the main building blocks of a level
 * each sector contains a collection of doors connecting it with other sectors
 * along with a cubemap as a visual representation of the sector
 * @author craig
 *
 */
public class Sector implements SpawnPoint.Listener {

	private final GameSession mGame;
	
	/**
	 * visual component of this sector
	 */
	private Cubemap mCubemap = null;
	
	/**
	 * Position of the sector in world space
	 */
	private Vector3f mPosition;
	
	/**
	 * name of this sector
	 */
	private String mName = null;
	
	/**
	 * doors connect two sectors to one another
	 * @see Door
	 */
	private Vector<Door> mDoors = new Vector<Door>();
	
	/**
	 * weapon store points
	 */
	private Vector<WeaponLocation> mWeaponLocations = new Vector<WeaponLocation>();
	
	/**
	 * Enemy spawn points for this sector
	 */
	private Vector<SpawnPoint> mEnemySpawnPoints = new Vector<SpawnPoint>();
	
	/**
	 * The ambient sound that is played when there are no enemies in the sector
	 */
	private final String mAmbientSound;
	
	// TODO: Remove?
	private final String mEnemySound;
	
	/**
	 * If this sector has an intro text cutscene, this value is the key
	 * lookup into the strings.xml table, otherwise the string will
	 * be empty
	 */
	private final String mIntroCutsceneText;
	
	/**
	 * If this sector has an intro video cutscene, this value is
	 * the name of the video file, stored in assets/Videos, otherwise
	 * the string will be empty
	 */
	private final String mIntroCutsceneVideoUri;
	
	/**
	 * Enemies will be queued to be added to a sector if there are too
	 * many on screen (i.e. if the number exceeds that of Constants.MaxEnemiesAlive)
	 */
	private Queue<Enemy> mQueuedEnemies = new LinkedList<Enemy>();
	
	
	/**
	 *  Enemies
	 */
	private Vector<Enemy> mEnemies = new Vector<Enemy>();
	private int mTotalEnemyCount;
	
	public GameSession getGame()
	{
		return mGame;
	}
	
	/**
	 * Constructor
	 * @param context
	 * @param dirname
	 * @param alreadyCleared whether or not the sector has previously been cleared
	 * @throws IOException
	 * @throws JSONException
	 */
	public Sector(GameSession game, String dirname, boolean alreadyCleared) throws IOException, JSONException
	{	
		mGame = game;
		// derive sector name from path
		mName = dirname.substring(dirname.lastIndexOf('/')+1);
		
		// load sector properties from json...
		JSONObject json = Utility.loadJSONObject(game.getAssets().open(dirname + "/sector.json"));
		
		mAmbientSound = json.getString("ambient_sound");
		mEnemySound = json.getString("enemy_sound");
		
		mIntroCutsceneText = json.getString("cutscene_text");
		mIntroCutsceneVideoUri = json.getString("cutscene_video");
		
		// sector position
		mPosition = Utility.loadVector3(json.getJSONObject("position"));
		
		// load doors
		JSONArray doors = json.getJSONArray("doors");
		for(int i = 0; i < doors.length(); ++i)
		{
			mDoors.add(new Door(doors.getJSONObject(i)));
		}
		
		// load weapon store points
		JSONArray storePoints = json.getJSONArray("weapon_store_points");
		for(int i = 0; i < storePoints.length(); ++i)
		{
			mWeaponLocations.add(new WeaponLocation(game, this, storePoints.getJSONObject(i), i));
		}
		
		// load the cubemap
		mCubemap = new Cubemap(game.getRenderer(), dirname, mPosition);
		
		// load enemy spawn points
		mTotalEnemyCount = 0;
		
		// only need to load spawn points if the sector has not been cleared yet
		if(alreadyCleared)
		{
			mGame.setBackgroundMusic(mAmbientSound);
		}
		else
		{
			JSONArray spawnPoints = json.getJSONArray("spawn_points");
			for(int i = 0; i < spawnPoints.length(); ++i)
			{
				JSONObject obj = spawnPoints.getJSONObject(i);
				SpawnPoint spawn = new SpawnPoint(this, obj);
				spawn.setListener(this);
				mTotalEnemyCount += spawn.getEnemyCount();
				mEnemySpawnPoints.add(spawn);
			}
			
			if(mTotalEnemyCount > 0)
			{
				mGame.setBackgroundMusic(mEnemySound);
			}
			else
			{
				mGame.setBackgroundMusic(mAmbientSound);
			}
		}
	}
	
	public Cubemap getCubemap()
	{
		return mCubemap;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public Vector3f getPosition()
	{
		return mPosition;
	}
	
	public void draw(Renderer renderer)
	{
		Matrix4f prevModelMatrix = renderer.getModelMatrix();
		
		renderer.setModelMatrix(Utility.CreateTranslationMatrix(mPosition));
		mCubemap.draw(renderer);
		renderer.setModelMatrix(prevModelMatrix);
		
		
		// sort enemies based on distance from camera
		Vector<Enemy> enemiesSorted = new Vector<Enemy>();
		enemiesSorted.addAll(mEnemies);
		Collections.sort(enemiesSorted, new Comparator<Enemy>(){

			@Override
			public int compare(Enemy a, Enemy b)
			{
				Vector3f avec = a.getPosition();
				avec.sub(Sector.this.getPosition());
				
				Vector3f bvec = b.getPosition();
				bvec.sub(Sector.this.getPosition());
				
				float alen = avec.lengthSqrd();
				float blen = bvec.lengthSqrd();
				
				if(alen < blen)
				{
					return 1;
				}
				else if(blen < alen)
				{
					return -1;
				}
				return 0;
			}
		});
		
		// draw all enemies
		for(Enemy enemy : enemiesSorted)
		{
			enemy.draw(renderer);
		}
	}
	
	public void update(float dt)
	{
		// update enemies
		int enemiesRemoved = 0;
		boolean bossDied = false;
		for(Iterator<Enemy> i = mEnemies.iterator(); i.hasNext();)
		{
			Enemy enemy = i.next();
			enemy.update(dt);
			
			if(enemy.isDead())
			{
				if(!enemy.mInformedPlayerOfEnemyDeath)
				{
					// remove from heading bar
					mGame.getPlayer().enemyDied(enemy);
					enemy.mInformedPlayerOfEnemyDeath = true;
				}
			}
			
			// remove the enemy if they died
			if(enemy.shouldRemove())
			{	
				if(enemy.isBoss())
				{
					bossDied = true;
				}
				
				// remove the old enemy
				i.remove();
				enemiesRemoved++;
			}
		}
		
		if(bossDied)
		{
			// kill all enemies
			for(int i = 0; i < mEnemies.size(); ++i)
			{
				mEnemies.get(i).takeHit(Integer.MAX_VALUE);
			}
			
			mTotalEnemyCount = 0;
			
			// Remove all queued enemies
			mQueuedEnemies.clear();
			
			// No more enemies can spawn
			for(SpawnPoint spawn : mEnemySpawnPoints)
			{
				spawn.stopSpawning();
			}
		}
		
		// if we removed enemies then add any enemies that are waiting in the queue
		while(enemiesRemoved-- > 0)
		{
			if(mQueuedEnemies.size() > 0)
			{
				Enemy enemy = mQueuedEnemies.poll();
				assert(enemy != null);
				addEnemy(enemy);
			}
		}
		
		// update enemy spawn points
		for(SpawnPoint spawn : mEnemySpawnPoints)
		{
			spawn.update(dt);
		}

		if(allEnemiesDead())
		{
			try
			{
				mGame.setBackgroundMusic(mAmbientSound);
			}
			catch(IOException e)
			{
				//Log.d("Acorn", "Could not load enemy sound");
			}
		}
	}

	/**
	 * Casts a ray into this sector and returns any objects it finds
	 * @param viewDir the pick vector
	 * @return object that viewDir intersects with, null if no object found
	 */
	public static enum TraceCategory
	{
		Door,
		WeaponLocation,
		Enemy
	}
	public IntersectableObject findObject(Vector3f viewDir, EnumSet<TraceCategory> category, IntersectionQuery.SearchFilter filter)
	{
		Vector3f v = viewDir.clone();
		v.scale(5000.0f);
		v.add(mPosition);
		IntersectionQuery query = new IntersectionQuery(mPosition, v);
		if(category.contains(TraceCategory.Door))
		{
			query.testObjects(mDoors, filter);
		}
		if(category.contains(TraceCategory.WeaponLocation))
		{
			query.testObjects(mWeaponLocations, filter);
		}
		if(category.contains(TraceCategory.Enemy))
		{
			query.testObjects(mEnemies, filter);
		}
		return query.getClosestObject();
	}
	
	public boolean allEnemiesDead()
	{
		return (mTotalEnemyCount == 0 && mEnemies.size() == 0);
	}

	private void addEnemy(Enemy enemy)
	{
		mTotalEnemyCount--;
		enemy.setListener(mGame);
		mEnemies.add(enemy);
		
		// calculate rotation from enemy to sector centre
		Vector3f dir = enemy.getPosition();
		dir.sub(mPosition);
		mGame.getPlayer().enemySpawned(enemy, Utility.GetHeadingFromPoint(dir.mX, dir.mY));
	}
	
	@Override
	public void spawnEnemy(Enemy enemy)
	{
		// if enemies already in queue then add enemy to queue
		if(mQueuedEnemies.size() > 0)
		{
			mQueuedEnemies.add(enemy);
		}
		else
		{
			// if we are at max capacity add to queue
			if(mEnemies.size() >= Constants.MaxEnemiesAlive)
			{
				mQueuedEnemies.add(enemy);
			}
			else
			{
				// add the enemy
				addEnemy(enemy);
			}
		}
	}
	
	public void unload()
	{
		mCubemap.cleanUp(mGame.getRenderer());
	}
	
	public String getIntroCutsceneText()
	{
		return mIntroCutsceneText;
	}
	
	public String getIntroCutsceneVideoUri()
	{
		return mIntroCutsceneVideoUri;
	}
}
