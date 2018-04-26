package com.craighamilton.dread;

import java.io.IOException;

public class Sound {
	
	private final int mSoundId;
	private final GameSession mGame;
	
	public Sound(GameSession game, int soundId) throws IOException
	{
		mGame = game;
		mSoundId = soundId;
	}
	
	public void play()
	{
		mGame.getSoundPool().play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
	}
	
	public void stop()
	{
		mGame.getSoundPool().stop(mSoundId);
	}
	
	public void setLooping(boolean looping)
	{
		mGame.getSoundPool().setLoop(mSoundId, looping ? -1 : 0);
	}
}
