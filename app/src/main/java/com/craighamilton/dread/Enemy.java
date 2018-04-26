package com.craighamilton.dread;

import java.io.IOException;
import java.util.Vector;


public class Enemy implements IntersectableObject, AnimationHelper.Listener {

	public interface Listener
	{
		public void enemyReachedDestination();
	}
	
	private Listener mListener;
	
	// the shared sprite sheet
	private AnimationHelper mAnimation;
	private Sprite mSprite;
	
	private Sprite mDropShadowSprite = null;
	
	private int mHealth;
	private final int mInitialHealth;
	
	private Vector3f mPosition;
	private final Vector3f mDestination;
	private final float mWidth;
	private final float mHeight;
	
	private final float mSpeed;

	
	private final Vector3f mDirection;
	private final Vector3f mTangent;
	private static Vector3f mUp = new Vector3f(0,0,1);
	
	private final int mSpawnIndex;
	private boolean mShouldRemove = false;
	private boolean mIsDead = false;
	public boolean mInformedPlayerOfEnemyDeath = false;
	
	private float mFadeTimer = 0.0f;
	private boolean mFadingOut = false;
	private final boolean mIsBoss;
	
	private Vector<Sound> mHurtSounds = new Vector<Sound>();
	private Vector<Sound> mDeathSounds = new Vector<Sound>();
	private Colour mColour;
	
	
	public Enemy(GameSession game, int spawnIndex, EnemyType type, Vector3f position, Vector3f destination) throws IOException
	{
		mSpawnIndex = spawnIndex;
		
		mPosition = position.clone();
		mDestination = destination.clone();
		
		mHealth = type.mHealth;
		mInitialHealth = mHealth;
		mWidth = type.mWidth;
		mHeight = type.mHeight;
		mSpeed = type.mSpeed;
		
		mDirection = destination.clone();
		mDirection.sub(position);
		mDirection.normalize();
		mTangent = new Vector3f();
		mTangent.cross(mUp, mDirection);
		
		mAnimation = new AnimationHelper(type.mSpriteSheet);
		mAnimation.setListener(this);
		
		mSprite = new Sprite(type.mSpriteSheet.getTexture());
		mSprite.setScale(mWidth * 0.5f, mHeight * 0.5f, 1.0f);

		mIsBoss = type.mIsBoss;
		
		for(int i = 0; i < type.mHurtSounds.size(); ++i)
		{
			Sound sound = game.loadSound(type.mHurtSounds.get(i));
			if(sound != null)
			{
				mHurtSounds.add(sound);
			}
		}
		for(int i = 0; i < type.mDeathSounds.size(); ++i)
		{
			Sound sound = game.loadSound(type.mDeathSounds.get(i));
			if(sound != null)
			{
				mDeathSounds.add(sound);
			}
		}
		
		// Drop shadow sprite sits below the enemy
		mDropShadowSprite = new Sprite(game.getEnemyDropShadowTexture());
		mDropShadowSprite.setScale(mWidth * 0.5f, mWidth * 0.5f, 1.0f);
		
		System.out.println("");
		mAnimation.playAnimation("approaching");
		
		// enemy colour
		mColour = new Colour(
				type.mColour.mRed,
				type.mColour.mGreen,
				type.mColour.mBlue);
	}
	
	/**
	 * 
	 * @param damage
	 */
	public void takeHit(int damage)
	{
		int prevHealth = mHealth;
		mHealth -= damage;
	
		float hurtSoundIncrement = mInitialHealth * Constants.EnemyHurtSoundHealthFraction;
		if((int)((float)prevHealth / hurtSoundIncrement) != (int)((float)mHealth / hurtSoundIncrement) && mHealth > 0)
		{
			if(mHurtSounds.size() > 0)
			{
				int index = (int)(Math.random() * mHurtSounds.size());
				if(index == mHurtSounds.size())
				{
					index = 0;
				}
				mHurtSounds.get(index).play();
			}
		}
		
		if(mHealth <= 0)
		{
			mHealth = 0;
			if(!mIsDead)
			{
				if(mDeathSounds.size() > 0)
				{
					int index = (int)(Math.random() * mDeathSounds.size());
					if(index == mDeathSounds.size())
					{
						index = 0;
					}
					mDeathSounds.get(index).play();
				}
				mIsDead = true;
				// play dying animation
				mAnimation.playRandomAnimationContainingName("dying");
				mAnimation.setLooping(false);
			}
		}
	}
	
	public void draw(Renderer renderer)
	{
		// Set Rotation
		Matrix3f spriteRot = new Matrix3f(
				mTangent.mX, mUp.mX, mDirection.mX,
				mTangent.mY, mUp.mY, mDirection.mY,
				mTangent.mZ, mUp.mZ, mDirection.mZ
				);
		mSprite.setRotation(spriteRot);
		
		// Set Position
		mSprite.setPosition(mPosition.mX, mPosition.mY, mPosition.mZ + (mHeight * 0.5f));
		
		// Set tex coords
		if(mAnimation.haveTexCoordsChanged())
		{
			float[] texcoords = mAnimation.getCurrentTexCoords();
			mSprite.setTexCoords(texcoords);
		}
		
		// position thye drop shadow
		mDropShadowSprite.setPosition(mPosition.mX, mPosition.mY, mPosition.mZ);
		
		// Draw
		float fadeAlpha = Math.min(1.0f, mFadeTimer / Constants.EnemyFadeInOutTime);
		
		
		Colour fadeColour = new Colour(1, 1, 1, fadeAlpha);
		mDropShadowSprite.drawWithColour(renderer, fadeColour);
		mColour.mAlpha = fadeAlpha;
		mSprite.drawWithColour(renderer, mColour);
	}
	
	public void update(float dt)
	{
		if(!mFadingOut)
		{
			mFadeTimer += dt;
			if(mFadeTimer > Constants.EnemyFadeInOutTime)
			{
				mFadeTimer = Constants.EnemyFadeInOutTime;
			}
		}
		else
		{
			mFadeTimer -= dt;
			if(mFadeTimer <= 0.0f)
			{
				mShouldRemove = true;
			}
		}
		
		mAnimation.update(dt);
		if(!mIsDead)
		{
			// update enemy position, move towards player
			Vector3f dir = mDirection.clone();
			dir.scale(mSpeed * dt);
			mPosition.add(dir);
			
			Vector3f tmp = mPosition.clone();
			tmp.sub(mDestination);
			if(tmp.length() < Constants.EnemyGameOverProximity)
			{
				if(mListener != null)
				{
					mListener.enemyReachedDestination();
				}
			}
		}
	}
	
	public int getSpawnIndex()
	{
		return mSpawnIndex;
	}
	
	public boolean shouldRemove()
	{
		return mShouldRemove;
	}
	
	public boolean isDead()
	{
		return mIsDead;
	}
	
	public int getHealth()
	{
		return mHealth;
	}
	
	public boolean isBoss()
	{
		return mIsBoss;
	}
	
	public int getInitialHealth()
	{
		return mInitialHealth;
	}
	
	public Sprite getSprite()
	{
		return mSprite;
	}

	@Override
	public LineIntersectionResult intersectsLine(Vector3f a, Vector3f b) {
		return mSprite.intersectsLine(a, b);
	}
	
	public void setListener(Listener listener)
	{
		mListener = listener;
	}

	@Override
	public void animationFinished(String animationName)
	{
		if(animationName.contains("dying"))
		{
			// wait until the enemy has faded out before setting isDead = true
			mFadingOut = true;
		}
	}
	
	public Vector3f getPosition()
	{
		return mPosition.clone();
	}
}
