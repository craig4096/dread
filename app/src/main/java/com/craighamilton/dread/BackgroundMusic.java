package com.craighamilton.dread;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class BackgroundMusic {

	/**
	 * Volume of the current track when a transition call is made
	 * Usually this will be 1.0 but when a transitionTo call is made
	 * during a transition we store the volume here
	 */
	private float mCurrentTrackStartVolume;
	private MediaPlayer mCurrentTrack;
	private float mCurrentTrackVolume;
	private float mTransitionTimer;
	private MediaPlayer mNextTrack;
	private final Context mContext;
	private boolean mPaused;
	
	private String mCurrentTrackFilename;
	private String mNextTrackFilename;
	
	
	public BackgroundMusic(Context context)
	{
		mContext = context;
		mCurrentTrackVolume = 0.0f;
		mPaused = false;
		mNextTrack = null;
		mCurrentTrack = null;
		mNextTrackFilename = "";
		mCurrentTrackFilename = "";
	}
	
	private MediaPlayer load(String filename) throws IOException
	{
		AssetFileDescriptor fd = mContext.getAssets().openFd(filename);
		MediaPlayer mp = new MediaPlayer();
		mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
		mp.prepare();
		mp.setLooping(true);
		return mp;
	}
	
	public synchronized void transitionTo(String filename) throws IOException
	{
		filename = "Sounds/" + filename + ".ogg";
		
		if(filename.equals(mNextTrackFilename))
		{
			// Already transitioning to this track
			return;
		}
		// If we would be transitioning to an already loaded track then cancel
		if(mNextTrackFilename == "" && filename.equals(mCurrentTrackFilename))
		{
			return;
		}
		
		// release the next sound regardless of any current transition
		// state
		if(mNextTrack != null)
		{
			mNextTrack.release();
			mNextTrack = null;
		}
		
		// load the next sound and start playing
		mNextTrack = load(filename);
		mNextTrack.start();
		mNextTrackFilename = filename;
		mTransitionTimer = 0.0f;
		
		// record the volume of the current track, we degrade this value over the
		// next [Constants.BackgroundMusicFadeTime] seconds
		if(mCurrentTrack != null)
		{
			mCurrentTrackStartVolume = mCurrentTrackVolume;
		}
	}
	
	public synchronized void update(float dt)
	{
		// Ambient sound fade
		if(!mPaused && mNextTrack != null)
		{
			mTransitionTimer += dt;

			float volume = Math.min(mTransitionTimer / Constants.BackgroundMusicFadeTime, 1.0f);
			mNextTrack.setVolume(volume, volume);
		
			if(mCurrentTrack != null)
			{
				mCurrentTrackVolume = (1.0f - volume) * mCurrentTrackStartVolume;
				
				mCurrentTrack.setVolume(mCurrentTrackVolume, mCurrentTrackVolume);
			}
			
			// [Current = Next] when transition is complete
			if(volume >= 1.0f)
			{
				if(mCurrentTrack != null)
				{
					mCurrentTrack.release();
					mCurrentTrack = null;
				}
				mCurrentTrack = mNextTrack;
				mCurrentTrackFilename = mNextTrackFilename;
				mCurrentTrackVolume = 1.0f;
				mNextTrack = null;
				mNextTrackFilename = "";
			}
		}
	}
	
	
	public synchronized void onPause()
	{
		if(!mPaused)
		{
			if(mCurrentTrack != null)
			{
				mCurrentTrack.pause();
			}
			if(mNextTrack != null)
			{
				mNextTrack.pause();
			}
			mPaused = true;
		}
	}
	
	public synchronized void onResume()
	{
		if(mPaused)
		{
			if(mCurrentTrack != null)
			{
				mCurrentTrack.start();
			}
			if(mNextTrack != null)
			{
				mNextTrack.start();
			}
			mPaused = false;
		}
	}

	
	public synchronized void onDestroy()
	{
		if(mCurrentTrack != null)
		{
			mCurrentTrack.reset();
			mCurrentTrack.release();
			mCurrentTrack = null;
		}
		if(mNextTrack != null)
		{
			mNextTrack.reset();
			mNextTrack.release();
			mNextTrack = null;
		}
	}
}
