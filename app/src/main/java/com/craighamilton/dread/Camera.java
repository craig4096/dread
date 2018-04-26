package com.craighamilton.dread;


public class Camera {
	
	private float mYFov;
	private float mZNear;
	private float mZFar;
	private float mAspectRatio;
	
	private Matrix4f mViewMatrix = new Matrix4f();
	private Matrix4f mProjMatrix = new Matrix4f();
	
	// translation and rotation components of the view matrix respectively
	private Vector3f mPosition = new Vector3f();
	private Matrix3f mViewMatrixRotation = new Matrix3f();
	
	public synchronized void setViewMatrixRotation(Matrix3f mat)
	{
		mViewMatrixRotation = mat.clone();
		updateViewMatrix();
	}
	
	private void updateViewMatrix()
	{
		mViewMatrix = new Matrix4f(mViewMatrixRotation);
		mViewMatrix.mul(Utility.CreateTranslationMatrix(new Vector3f(-mPosition.mX, -mPosition.mY, -mPosition.mZ)));
	}
	
	private void updateProjectionMatrix()
	{
		mProjMatrix = Utility.CreatePerspectiveProjectionMatrix(mYFov, mAspectRatio, mZNear, mZFar);
	}
	
	public Camera(float yFov, float aspect, float zNear, float zFar)
	{
		mYFov = yFov;
		mZNear = zNear;
		mZFar = zFar;
		mAspectRatio = aspect;
		updateProjectionMatrix();
	}
	
	public void setAspectRatio(float aspect)
	{
		mAspectRatio = aspect;
		updateProjectionMatrix();
	}
	
	public void setPosition(Vector3f pos)
	{
		mPosition = pos.clone();
		updateViewMatrix();
	}
	
	public Vector3f getPosition()
	{
		return mPosition.clone();
	}
	
	public synchronized Matrix4f getViewMatrix()
	{
		return mViewMatrix.clone();
	}
	
	public Matrix4f getProjectionMatrix()
	{
		return mProjMatrix.clone();
	}
	
	public Vector3f getViewDirection()
	{
		float[] row = new float[4];
		getViewMatrix().getRow(2, row);
		return new Vector3f(-row[0], -row[1], -row[2]);
	}
	
	public float getYFov()
	{
		return mYFov;
	}
	
	public float getZNear()
	{
		return mZNear;
	}
	
	public float getZFar()
	{
		return mZFar;
	}
	
	/**
	 * Returns the yaw rotation of the camera in radians between
	 * 0 and 2PI
	 * @return
	 */
	public float getYawRotation()
	{
		Vector3f viewDir = getViewDirection();
		float x = viewDir.mX;
		float y = viewDir.mY;
		return Utility.GetHeadingFromPoint(x, y);
	}
}
