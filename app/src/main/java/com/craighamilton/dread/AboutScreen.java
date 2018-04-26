package com.craighamilton.dread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutScreen extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.about_screen);
	    
	    
	    Button email = (Button)findViewById(R.id.emailButton);
	    email.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});
	    
	    TextView text = (TextView)findViewById(R.id.aboutText);
	    text.setText(
	    		Utility.getString(this, "developed_by") + "\n" +
	    		Utility.getString(this, "developer_name") + "\n" +
	    		Utility.getString(this, "developer_email"));
	    
	}
	
	private void sendEmail()
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"craig.dreadev@gmail.com"});
		try
		{
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
	    	Builder alert = new AlertDialog.Builder(this);
			alert.setMessage(R.string.no_email);
			alert.setPositiveButton(R.string.ok, null);
			alert.show();
		}
	}

}
