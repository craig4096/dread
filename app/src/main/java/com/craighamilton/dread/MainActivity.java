package com.craighamilton.dread;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        
        Button continueButton = (Button)findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startGameSession();
			}
		});
        
        Button howToPlayButton = (Button)findViewById(R.id.howToPlay);
        howToPlayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startHowToPlay();
			}
		});
        
        Button quitButton = (Button)findViewById(R.id.quitButton);
        quitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        Button settingsButton = (Button)findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View v)
        	{
        		startSettings();
        	}
        });
        
        Button aboutButton = (Button)findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startAbout();
			}
		});
    }
    
    private void startHowToPlay()
    {
    	Intent intent = new Intent(this, HowToPlay.class);
    	startActivity(intent);
    }
    
    private void startGameSession()
    {
    	// If there is no save game then play the intro cutscene
		// Clear the save game data...
    	SharedPreferences saveGame = getSharedPreferences(PersistVars.SaveGame, Context.MODE_PRIVATE);
    	if(!saveGame.contains("sector"))
    	{
	    	Intent intent = new Intent(this, CutsceneText.class);
	    	intent.putExtra(PersistVars.CutsceneText_NextActivityClass, Cutscene.class);
	    	intent.putExtra(PersistVars.CutsceneText_Text, getResources().getString(R.string.intro_briefing));
	    	intent.putExtra(PersistVars.CutsceneText_FadeTimeMs, Constants.CutsceneTextFadeTimeMs);
	    	intent.putExtra(PersistVars.CutsceneText_WaitTimeMs, Constants.CutsceneTextWaitTimeMs);
	    	
	    	// Cutscene to play after the cutscene text
	    	intent.putExtra(PersistVars.Cutscene_NextActivityClass, GameSession.class);
	    	intent.putExtra(PersistVars.Cutscene_VideoUri, "Videos/Intro.mp4");
	    	startActivity(intent);
    	}
    	else
    	{
    		Intent intent = new Intent(this, GameSession.class);
    		startActivity(intent);
    	}
    }
    
    private void startAbout()
    {
    	Intent intent = new Intent(this, AboutScreen.class);
    	startActivity(intent);
    }
    
    private void startSettings()
    {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
}
