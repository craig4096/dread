package com.craighamilton.dread;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

public class SensorInput implements SensorEventListener {

	
	public interface Listener
	{
		public void updateViewMatrix(Matrix3f viewRot);
	}
	
	private Sensor mGravitySensor;
	private Sensor mMagneticFieldSensor;
	private SensorManager mSensorManager;
	private Listener mListener = null;
	private boolean mIsAvailable = false;
	
	/**
	 * When the player starts the game this is the initial rotation they
	 * are around the real world z-axis represented as a rotation matrix around
	 * the z axis
	 */
	private Matrix3f mBaseRotationMatrix = Utility.IdentityMatrix3();
	private boolean mFirstSensorEvent;
	
	public SensorInput(Context context, Listener listener)
	{
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(android.os.Build.VERSION.SDK_INT > 8)
        {
        	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        else
        {
        	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        mIsAvailable = prefs.getBoolean("device_orientation_enabled", false) &&
        		mGravitySensor != null && mMagneticFieldSensor != null;
        mFirstSensorEvent = true;
        mListener = listener;
	}
	
	public void onResume()
	{
    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
    	mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void onPause()
	{
    	mSensorManager.unregisterListener(this);
	}
	
	/**
	 * Determines whether the sensor input is available or not
	 * @return
	 */
	public boolean isEnabled()
	{
		return mIsAvailable;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
	
	private float[] mGravData = null;
	private float[] mMagData = null;
	
	private float[] mGravAccum = { 0.0f, 0.0f, 0.0f };
	private float[] mMagAccum = { 0.0f, 0.0f, 0.0f };
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		switch(event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			{
				mGravData = event.values.clone();
			}
			break;
		case Sensor.TYPE_GRAVITY:
			{
				mGravData = event.values.clone();
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			{
				mMagData = event.values.clone();
			}
			break;
		}
		
		if(mGravData != null && mMagData != null)
		{	
			// Basic low pass filter
			final float alpha = 0.95f;
			
			mGravAccum[0] = alpha * mGravAccum[0] + (1 - alpha) * mGravData[0];
			mGravAccum[1] = alpha * mGravAccum[1] + (1 - alpha) * mGravData[1];
			mGravAccum[2] = alpha * mGravAccum[2] + (1 - alpha) * mGravData[2];
			
			mMagAccum[0] = alpha * mMagAccum[0] + (1 - alpha) * mMagData[0];
			mMagAccum[1] = alpha * mMagAccum[1] + (1 - alpha) * mMagData[1];
			mMagAccum[2] = alpha * mMagAccum[2] + (1 - alpha) * mMagData[2];
			
		    float[] matrixR = new float[9];
		    float[] matrixI = new float[9];
		    
		    boolean success = SensorManager.getRotationMatrix(
		    		   matrixR,
		    		   matrixI,
		    		   mGravAccum,
		    		   mMagAccum);
		    
		    mGravData = null;
		    mMagData = null;
		   
		    if(success)
		    {
		    	// matrixR = device to world, transpose to get view rotation matrix
		    	Matrix3f viewRot = new Matrix3f(
						matrixR[0], matrixR[3], matrixR[6],
						matrixR[1], matrixR[4], matrixR[7],
						matrixR[2], matrixR[5], matrixR[8]);
		    	
				// rendering in landscape view, rotate the view matrix 90 degrees about its own z-axis
				// yAxis = xAxis
				// xAxis = -yAxis
				Vector3f xAxis = new Vector3f();
				Vector3f yAxis = new Vector3f();
				viewRot.getRow(0, xAxis);
				viewRot.getRow(1, yAxis);
				
				viewRot.setRow(0, new Vector3f( -yAxis.mX, -yAxis.mY, -yAxis.mZ ));
				viewRot.setRow(1, new Vector3f( xAxis.mX, xAxis.mY, xAxis.mZ ));
				
		    	if(mFirstSensorEvent)
		    	{
		    		// calculate the initial yaw rotation matrix
		    		Vector3f viewDir = new Vector3f();
		    		viewRot.getRow(2, viewDir);
		    		viewDir = new Vector3f(-viewDir.mX, -viewDir.mY, 0.0f);
		    		viewDir.normalize();
		    		
		    		float rotation = (float)Math.atan2((float)viewDir.mX, (float)viewDir.mY);
		    		
		    		mBaseRotationMatrix = Utility.CreateZRotationMatrix(-rotation);
		    		
		    		mFirstSensorEvent = false;
		    	}
		    	if(mListener != null)
		    	{
		    		mListener.updateViewMatrix(viewRot);
		    	}
		    }
		}
	}
	
	public Matrix3f getBaseRotationMatrix()
	{
		return mBaseRotationMatrix;
	}
	
}
