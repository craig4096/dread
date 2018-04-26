package com.craighamilton.dread;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WeaponDisplay extends Activity {

	private String mWeaponType;
	private int mWeaponIndex;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mWeaponType = bundle.getString("weapon_type");
        mWeaponIndex = bundle.getInt("weapon_index");
        setContentView(R.layout.weapon_display);


        // Set the preview image and ammo type preview
        ImageView displayImageView = (ImageView)this.findViewById(R.id.weaponImage);
		try
		{
			Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("Weapons/" + mWeaponType + "/preview.png"));
	        displayImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
        String ammoImage = bundle.getString("ammo_image");
		ImageView ammoImageView = (ImageView)this.findViewById(R.id.ammoImage);
		try
		{
			Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("Textures/" + ammoImage + ".png"));
	        ammoImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		

        
		// Set the name
		TextView weaponName = (TextView)findViewById(R.id.weaponName);
		weaponName.setText(Utility.getString(this, "weapon_name_" + mWeaponType));
		
		// Damage
		TextView damage = (TextView)findViewById(R.id.Damage);
		damage.setText("" + bundle.getInt("weapon_damage"));
		
		// Firerate
		TextView firerate = (TextView)findViewById(R.id.FireRate);
		firerate.setText("" + bundle.getFloat("weapon_firerate") + " rps");
		
		// Reload Time
		TextView reloadTime = (TextView)findViewById(R.id.ReloadSpeed);
		reloadTime.setText("" + bundle.getFloat("weapon_reload_time") + " s");
		
		// Capacity
		TextView capacity = (TextView)findViewById(R.id.Capacity);
		capacity.setText("" + bundle.getInt("weapon_capacity"));
		
		// Automatic
		TextView automatic = (TextView)findViewById(R.id.Automatic);
		automatic.setText("" + (bundle.getBoolean("weapon_automatic") ? Utility.getString(this, "yes") : Utility.getString(this, "no")));
		
		
		Button backButton = (Button)findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult();
				finish();
			}
		});
    }
	
	private void setResult()
	{
		Intent intent = new Intent();
		intent.putExtra("weapon_type", mWeaponType);
		intent.putExtra("weapon_index", mWeaponIndex);
		setResult(RESULT_OK, intent);
	}
	
	@Override
	public void onBackPressed()
	{
		setResult();
		super.onBackPressed();
	}
}
