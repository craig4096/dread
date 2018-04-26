package com.craighamilton.dread;

public class Object3D {

	protected Vector3f mPosition = new Vector3f(0, 0, 0);
	protected Vector3f mScale = new Vector3f(1, 1, 1);
	protected Matrix4f mRotation = Utility.IdentityMatrix4();
	protected Matrix4f mTransform = Utility.IdentityMatrix4();

	
	private void updateTransform()
	{
		mTransform = Utility.CreateTranslationMatrix(mPosition);
		mTransform.mul(mRotation);
		mTransform.mul(Utility.CreateScalingMatrix(mScale));
	}
	
	public Matrix4f getModelMatrix()
	{
		return mTransform.clone();
	}
	
	public Matrix4f getInverseModelMatrix()
	{
		// TODO: Optimise for orthogonal
		Matrix4f m = getModelMatrix();
		m.invert();
		return m;
	}
	
	public void setPosition(Vector3f pos)
	{
		mPosition.set(pos);
		updateTransform();
	}
	
	public void setPosition(float x, float y, float z)
	{
		mPosition.set(x, y, z);
		updateTransform();
	}
	public Vector3f getPosition()
	{
		return mPosition.clone();
	}
	
	public void setRotation(Matrix3f rot)
	{
		mRotation.set(rot);
		updateTransform();
	}
	
	public void setScale(Vector3f scale)
	{
		mScale.set(scale);
		updateTransform();
	}
	
	public void setScale(float x, float y, float z)
	{
		mScale.set(x, y, z);
		updateTransform();
	}
	
	public Vector3f getScale()
	{
		return mScale.clone();
	}
	
	/**
	 * Helper methods for Sprite rendering
	 */
	public void setRect(float left, float bottom, float right, float top)
	{
		setPosition((left + right) * 0.5f, (bottom + top) * 0.5f, 0.0f);
		setScale((right - left) * 0.5f, (top - bottom) * 0.5f, 1.0f);
	}
	
	public void setRectXYWH(float x, float y, float width, float height)
	{
		setPosition(x + (width * 0.5f), y + (height * 0.5f), 0.0f);
		setScale(width * 0.5f, height * 0.5f, 1.0f);
	}
}
