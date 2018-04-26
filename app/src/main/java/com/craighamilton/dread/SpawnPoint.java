package com.craighamilton.dread;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpawnPoint {
	
	public interface Listener
	{
		/**
		 * Called when a spawn point wishes to spawn an enemy
		 * @param enemy
		 */
		public void spawnEnemy(Enemy enemy);
	}
	

	private final Vector3f mPosition;
	
	private Listener mListener;
	private final GameSession mGame;
	private float mElapsedTime;
	
	public SpawnPoint(Sector sector, JSONObject json) throws JSONException, IOException
	{
		mGame = sector.getGame();
		mPosition = Utility.loadVector3(json.getJSONObject("position"));
		mElapsedTime = 0.0f;
		
		Vector3f dstPosition = sector.getPosition().clone();
		dstPosition.sub(new Vector3f(0, 0, Constants.PlayerEyeHeightMetres));

		JSONArray spawnList = json.getJSONArray("spawn_list");
		for(int i = 0; i < spawnList.length(); ++i)
		{
			JSONObject entry = spawnList.getJSONObject(i);
			mEnemySpawnRequests.add(
					new EnemySpawnRequest(
							entry.getString("type"),
							dstPosition.clone(),
							(float)entry.getDouble("time")
							));
		}
	}
	
	/**
	 * Enemy spawn requests
	 */
	private static class EnemySpawnRequest
	{
		public float mSpawnTime;
		public Vector3f mDestination;
		public String mEnemyType;
		
		public EnemySpawnRequest(String type, Vector3f destination, float spawnTime)
		{
			mEnemyType = type;
			mDestination = destination;
			mSpawnTime = spawnTime;
		}
	}
	private Vector<EnemySpawnRequest> mEnemySpawnRequests = new Vector<EnemySpawnRequest>();
	
	/**
	 * Determines whether we still have enemies to be spawned from this spawn point
	 * @return
	 */
	public boolean canSpawnMoreEnemies()
	{
		return mEnemySpawnRequests.size() > 0;
	}
	
	
	/**
	 * Stops any more enemies from spawning
	 */
	public void stopSpawning()
	{
		mEnemySpawnRequests.clear();
	}
	
	public void update(float dt)
	{
		mElapsedTime += dt;
		
		// process enemy spawn requests
		for(Iterator<EnemySpawnRequest> i = mEnemySpawnRequests.iterator(); i.hasNext();)
		{
			EnemySpawnRequest request = i.next();
			if(mElapsedTime > request.mSpawnTime)
			{
				Enemy enemy = null;
				try
				{
					enemy = new Enemy(mGame, 0, mGame.getOrLoadEnemyType(request.mEnemyType), mPosition.clone(), request.mDestination.clone());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(mListener != null && enemy != null)
				{
					mListener.spawnEnemy(enemy);
				}
				i.remove();
			}
		}
	}
	
	public int getEnemyCount()
	{
		return mEnemySpawnRequests.size();
	}
	
	public void setListener(Listener listener)
	{
		mListener = listener;
	}
}
