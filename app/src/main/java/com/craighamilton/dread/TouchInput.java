package com.craighamilton.dread;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This class handles all of the touch input and translates it to a series of events
 * which are sent to the game (or attached listener object)
 * @author Craig
 */
public class TouchInput implements OnTouchListener {
	

	public enum Event
	{
		SwitchLeftWeapon,
		SwitchRightWeapon,
		BeginFireLeftWeapon,
		EndFireLeftWeapon,
		BeginFireRightWeapon,
		EndFireRightWeapon,
		Action
	};
	
	private Queue<Event> mEventQueue = new LinkedList<Event>();
	
	private synchronized void queueEvent(Event event)
	{
		mEventQueue.add(event);
	}
	
	public synchronized Event getNextEvent()
	{
		return mEventQueue.poll();
	}
	
	private int mLeftPointerID = -1;
	private int mRightPointerID = -1;
	private int mSwipePointerID = -1;
	
	private float mVelocityMultiplier = 1.0f;
	
	private final float mXDpi;
	
	
	public TouchInput(Context context, float screenXDpi)
	{
		mXDpi = screenXDpi;
		
		// determine the velocity multiplier from swipe sensitivity option
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String sens = prefs.getString("swipe_sensitivity", "N");
		if(sens.equals("VL"))
		{
			mVelocityMultiplier = 0.25f;
		}
		else if(sens.equals("L"))
		{
			mVelocityMultiplier = 0.6f; 
		}
		else if(sens.equals("N"))
		{
			mVelocityMultiplier = 1.0f; 
		}
		else if(sens.equals("F"))
		{
			mVelocityMultiplier = 1.4f; 
		}
		else if(sens.equals("VF"))
		{
			mVelocityMultiplier = 1.75f; 
		}
	}
	
	/**
	 * Maps screen locations to discrete action event types
	 */
	enum ActionType
	{
		SwitchLeftWeapon,
		SwitchRightWeapon,
		FireLeftWeapon,
		FireRightWeapon,
		SwipeBar,
		Action
	}
	private ActionType classifyTouchEvent(float x, float y, float width, float height)
	{	
		if(y > height * (1.0f - Constants.HeadingBarHeightRatio))
		{	
			return ActionType.SwipeBar;
		}
		
		float startX = (width * ((1.0f - Constants.ActionWidthRatio) / 2.0f));
		float startY = (height * ((1.0f - Constants.ActionHeightRatio) / 2.0f));
		
		if(x > startX && x < (startX + (Constants.ActionWidthRatio * width)) &&
				y > startY && y < (startY + (Constants.ActionHeightRatio * height)))
		{
			return ActionType.Action;
		}
		else
		{
			float halfWidth = width / 2.0f;
			float halfHeight = height / 2.0f;
			
			if(x < halfWidth)
			{
				if(y < halfHeight)
				{
					return ActionType.SwitchLeftWeapon;
				}
				else
				{
					return ActionType.FireLeftWeapon;
				}
			}
			else
			{
				if(y < halfHeight)
				{
					return ActionType.SwitchRightWeapon;
				}
				else
				{
					return ActionType.FireRightWeapon;
				}
			}
		}
	}
	
	
	private float mCurrentRotation;
	private float mCurrentVelocity;
	private float mStartXPos;
	private float mCurrentXPos;
	
	
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		int width = view.getWidth();
		int height = view.getHeight();
		
		boolean consumed = false;

		// check if the events is triggering the left
		switch(event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				// get the id of the pointer
				int pointerIndex = (event.getActionMasked() == MotionEvent.ACTION_DOWN) ? 0 : event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				
				float x = event.getX(pointerIndex);
				float y = event.getY(pointerIndex);

				switch(classifyTouchEvent(x, y, (float)width, (float)height))
				{
					case SwitchLeftWeapon:
					{
						queueEvent(Event.SwitchLeftWeapon);
					}
					break;
					case SwitchRightWeapon:
					{
						queueEvent(Event.SwitchRightWeapon);
					}
					break;
					case FireLeftWeapon:
					{
						mLeftPointerID = pointerId;
						queueEvent(Event.BeginFireLeftWeapon);
					}
					break;
					case FireRightWeapon:
					{
						mRightPointerID = pointerId;
						queueEvent(Event.BeginFireRightWeapon);
					}
					break;
					case Action:
					{
						queueEvent(Event.Action);
					}
					break;
					case SwipeBar:
					{
						mStartXPos = x;
						mCurrentXPos = x;
						mSwipePointerID = pointerId;
					}
					break;
				}
				consumed = true;
			}
			break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			{
				// get the id of the pointer
				int pointerIndex = (event.getActionMasked() == MotionEvent.ACTION_UP) ? 0 : event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				if(pointerId == mLeftPointerID)
				{
					mLeftPointerID = -1;
					queueEvent(Event.EndFireLeftWeapon);
				}
				else if(pointerId == mRightPointerID)
				{
					mRightPointerID = -1;
					queueEvent(Event.EndFireRightWeapon);
				}
				else if(pointerId == mSwipePointerID)
				{
					mCurrentXPos = mStartXPos;
					mSwipePointerID = -1;
				}
				consumed = true;
			}
			break;
			case MotionEvent.ACTION_MOVE:
			{
				for(int i = 0; i < event.getPointerCount(); ++i)
				{
					if(event.getPointerId(i) == mSwipePointerID)
					{
						float x = event.getX(i);
						
						mCurrentXPos = x;
						return true;
					}
				}
			}
			break;
		}
		return consumed;
	}
	
	public void update(float dt)
	{
		float offsetInches = (mCurrentXPos - mStartXPos) / mXDpi;
		if(offsetInches != 0)
		{
			float ratio = Math.max(
							Math.min(offsetInches / Constants.SwipeVelocityRangeInches,
									1.0f), -1.0f);
			
			mCurrentVelocity = ratio * (Constants.MaxSwipeVelocity * mVelocityMultiplier);
		}
		
		
		mCurrentRotation += mCurrentVelocity * dt;
		
		mCurrentVelocity *= Constants.SwipeVelocityDampingCoefficient;
	}
	
	public float getYawRotation()
	{
		return mCurrentRotation;
	}
}
