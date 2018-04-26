package com.craighamilton.dread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameOverScreen extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.game_over);
	    
        Button continueButton = (Button)findViewById(R.id.gameOverContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startGameSession();
			}
		});

        Button quitButton = (Button)findViewById(R.id.gameOverQuit);
        quitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
    private void startGameSession()
    {
    	Intent intent = new Intent(this, GameSession.class);
    	finish();
    	startActivity(intent);
    }

}
