package com.craighamilton.dread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class SplashScreen extends Activity implements AnimationListener {

	Animation mFadeInAnimation;
	Animation mFadeOutAnimation;
	ImageView mView;
	
	/////////////////////////////////////////////////////////////////////////
	// License Code
	/////////////////////////////////////////////////////////////////////////
	Handler mHandler;
	
	/////////////////////////////////////////////////////////////////////////
	
	boolean mSplashScreenFinished = false;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.splash_screen);
	    
	    mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
	    mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
	    
	    mFadeInAnimation.setAnimationListener(this);
	    mFadeOutAnimation.setAnimationListener(this);
	    
	    mFadeInAnimation.setDuration(Constants.SplashScreenFadeInTimeMs);
	    mFadeOutAnimation.setDuration(Constants.SplashScreenFadeOutTimeMs);
	    
	    mView = (ImageView)findViewById(R.id.splashScreen);
	    mView.startAnimation(mFadeInAnimation);
	    
	    View background = findViewById(android.R.id.content);
	    if(background != null)
	    {
	    	background.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					mSplashScreenFinished = true;
					goToMainMenu();
					return true;
				}
		    });
	    }
	    
	    
	    mHandler = new Handler();
	}
	
	private boolean mGoingToMainMenu = false;
	private void goToMainMenu()
	{
		if(!mGoingToMainMenu)
		{
	    	Intent intent = new Intent(SplashScreen.this, MainActivity.class);
	    	startActivity(intent);
	    	finish();
	    	mGoingToMainMenu = true;
		}
	}

	@Override
	public void onAnimationEnd(Animation a)
	{
		if(a == mFadeInAnimation)
		{
			mView.startAnimation(mFadeOutAnimation);
		}
		else if(a == mFadeOutAnimation)
		{
			mSplashScreenFinished = true;
			goToMainMenu();
			Utility.setViewAlpha(mView, 0.0f);
		}
	}

	@Override
	public void onAnimationRepeat(Animation a)
	{
	}

	@Override
	public void onAnimationStart(Animation a)
	{
	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

}
