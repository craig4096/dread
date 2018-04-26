package com.craighamilton.dread;


import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Cutscene extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener {

	private MediaPlayer mMediaPlayer = null;
	private SurfaceView mSurfaceView = null;
	private Class mNextActivityClass;
	private String mVideoName;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.cutscene);
	    
	    Bundle bundle = getIntent().getExtras();
	    if(bundle != null)
	    {
	    	mNextActivityClass = (Class)bundle.get(PersistVars.Cutscene_NextActivityClass);
	    	mVideoName = bundle.getString(PersistVars.Cutscene_VideoUri);
	    }
	    // Get the surface view the cutscene will be displayed upon
	    mSurfaceView = (SurfaceView)findViewById(R.id.cutsceneSurfaceView);
	    if(mSurfaceView != null)
	    {
	    	mSurfaceView.getHolder().addCallback(this);
	    }
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			// Start playing the video
			AssetFileDescriptor fd = getAssets().openFd(mVideoName);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
			mMediaPlayer.setDisplay(mSurfaceView.getHolder());
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		// Go to the next activity
		if(mNextActivityClass != null)
		{
			Intent intent = new Intent(this, mNextActivityClass);
			startActivity(intent);
		}
		finish();
	}
}
