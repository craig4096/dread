package com.craighamilton.dread;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

public class EnemyType {

	public final int mHealth;
	public final float mWidth;
	public final float mHeight;
	public final float mSpeed;
	public final Vector<String> mHurtSounds = new Vector<String>();
	public final Vector<String> mDeathSounds = new Vector<String>();
	public final SpriteSheet mSpriteSheet;
	public final Colour mColour;
	public final boolean mIsBoss;

	
	public EnemyType(GameSession game, String typeName) throws IOException, JSONException
	{
		// load enemy json file
		JSONObject desc = Utility.loadJSONObject(game.getAssets().open("Enemies/" + typeName + ".json"));
		String gg = desc.getString("sprite_sheet");
		mHealth = desc.getInt("health");
		mWidth = (float)desc.getDouble("width");
		mHeight = (float)desc.getDouble("height");
		mSpeed = (float)desc.getDouble("speed");
		mIsBoss = desc.getBoolean("is_boss");
		JSONArray hurtSounds = desc.getJSONArray("hurt_sounds");
		for(int i = 0; i < hurtSounds.length(); ++i)
		{
			mHurtSounds.add(hurtSounds.getString(i));
		}
		JSONArray deathSounds = desc.getJSONArray("death_sounds");
		for(int i = 0; i < deathSounds.length(); ++i)
		{
			mDeathSounds.add(deathSounds.getString(i));
		}
		mSpriteSheet = new SpriteSheet(game, game.getRenderer(), gg);
		
		int color = 0;
		try
		{
			color = Color.parseColor(desc.getString("colour"));
		}
		catch(IllegalArgumentException e)
		{
		}
		mColour = new Colour(color);
	}
	
}
