package com.craighamilton.dread;

import org.json.JSONException;
import org.json.JSONObject;

public class WeaponLocation implements IntersectableObject {

	private final String mWeaponType;
	private final int mIndex;
	
	/**
	 * The sprite highlight for this weapon location
	 */
	private Sprite mSprite;
	
	/**
	 * 
	 */
	private Sphere mSphere;
	
	/**
	 * This is the initial rotation of the sprite facing the sector centre, we store this
	 * so we can apply a rotation around it's facing axis to act as an animation
	 */
	private Matrix3f mInitialRotationMatrix;
	
	/**
	 * The current rotation of the sprite around it's sector-facing axis
	 * to act as an animation, stored in radians
	 */
	private float mAnimationRotation = 0.0f;
	
	
	public WeaponLocation(GameSession game, Sector sector, JSONObject json, int index) throws JSONException
	{	
		mWeaponType = json.getString("weapon_type");
		mIndex = index;
		
		// Load intersection quad
		Vector3f position = Utility.loadVector3(json.getJSONObject("position"));
		float radius = (float)json.getDouble("radius");

		mSphere = new Sphere(position, radius);
		
		mSprite = new Sprite(game.getWeaponLocationTexture());
		mSprite.setPosition(position);
		mSprite.setScale(radius, radius, 1.0f);
		
		
		Vector3f direction = sector.getPosition().clone();
		direction.sub(position);
		direction.normalize();
		
		Vector3f up = new Vector3f(0,0,1);
		Vector3f tangent = new Vector3f();
		tangent.cross(up, direction);
		
		up.cross(direction, tangent);
		
		mInitialRotationMatrix = new Matrix3f(
				tangent.mX, up.mX, direction.mX,
				tangent.mY, up.mY, direction.mY,
				tangent.mZ, up.mZ, direction.mZ
				);
	}
	
	public void update(float dt)
	{
		mAnimationRotation += dt * (Constants.WeaponIconRotateSpeed * 2.0f * Math.PI);
	}
	
	public void draw(Renderer renderer)
	{
		// apply the animation rotation beforehand
		Matrix3f finalRotation = mInitialRotationMatrix.clone();
		finalRotation.mul(Utility.CreateZRotationMatrix(mAnimationRotation));
		
		mSprite.setRotation(finalRotation);
		mSprite.draw(renderer);
	}

	@Override
	public LineIntersectionResult intersectsLine(Vector3f a, Vector3f b)
	{
		return mSphere.intersectsLine(a, b);
	}
	
	public String getWeaponType()
	{
		return mWeaponType;
	}
	
	public int getIndex()
	{
		return mIndex;
	}
}
