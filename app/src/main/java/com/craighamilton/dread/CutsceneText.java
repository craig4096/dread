package com.craighamilton.dread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

public class CutsceneText extends Activity implements AnimationListener {

	private Animation mFadeInAnimation;
	private Animation mFadeOutAnimation;
	private Class mNextActivityClass;
	private int mFadeTimeMs;
	private int mWaitTimeMs;
	private String mText;
	private TextView mTextView;
	private Bundle mBundle;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.cutscene_text);
	    
	    Bundle bundle = getIntent().getExtras();
	    if(bundle != null)
	    {
	    	mText = bundle.getString(PersistVars.CutsceneText_Text);
	    	mFadeTimeMs = bundle.getInt(PersistVars.CutsceneText_FadeTimeMs);
	    	mWaitTimeMs = bundle.getInt(PersistVars.CutsceneText_WaitTimeMs);
	    	mNextActivityClass = (Class)bundle.get(PersistVars.CutsceneText_NextActivityClass);
	    	mBundle = bundle;
	    }
	    
	    mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
	    mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
	    
	    mFadeInAnimation.setAnimationListener(this);
	    mFadeOutAnimation.setAnimationListener(this);
	    
	    mFadeInAnimation.setDuration(mFadeTimeMs);
	    mFadeOutAnimation.setDuration(mFadeTimeMs);
	    
	    mTextView = (TextView)findViewById(R.id.cutscene_textview);
	    mTextView.startAnimation(mFadeInAnimation);
	    mTextView.setText(mText);
	}
	
	@Override
	public void onAnimationEnd(Animation a)
	{
		if(a == mFadeInAnimation)
		{
			// wait for mWaitTimeMs before starting the fade out animation
			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){
				@Override
				public void run()
				{
					mTextView.startAnimation(mFadeOutAnimation);
				}
				
			}, mWaitTimeMs);
		}
		else if(a == mFadeOutAnimation)
		{
			if(mNextActivityClass != null)
			{
				Intent intent = new Intent(this, mNextActivityClass);
				intent.putExtras(mBundle);
				startActivity(intent);
			}
			finish();
			Utility.setViewAlpha(mTextView, 0.0f);
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
}
