package com.craighamilton.dread;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference button = (Preference)findPreference("reset_save");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	        @Override
	        public boolean onPreferenceClick(Preference arg0) {
	            resetSave();
	            return true;
	        }
	    });
	}
	
    private void resetSave()
    {
    	Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle(R.string.reset_save_confirm_title);
		alert.setMessage(R.string.reset_save_confirm_text);
		
		alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Clear the save game data...
		    	SharedPreferences saveGame = getSharedPreferences(PersistVars.SaveGame, Context.MODE_PRIVATE);
		    	SharedPreferences.Editor edit = saveGame.edit();
		    	edit.clear();
		    	edit.commit();
			}
		});
		alert.setNegativeButton(R.string.no, null);
		alert.show();
    }
}
