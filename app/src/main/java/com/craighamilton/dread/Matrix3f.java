package com.craighamilton.dread;

public class Matrix3f {

	private float mElements[][] = new float[3][3];
	
	public Matrix3f()
	{
	}
	
	public Matrix3f(float _11, float _12, float _13,
					float _21, float _22, float _23,
					float _31, float _32, float _33)
	{
		mElements[0][0] = _11;
		mElements[0][1] = _12;
		mElements[0][2] = _13;
		mElements[1][0] = _21;
		mElements[1][1] = _22;
		mElements[1][2] = _23;
		mElements[2][0] = _31;
		mElements[2][1] = _32;
		mElements[2][2] = _33;
	}
	
	public void set(Matrix3f mat)
	{
		mElements[0][0] = mat.mElements[0][0];
		mElements[0][1] = mat.mElements[0][1];
		mElements[0][2] = mat.mElements[0][2];
		mElements[1][0] = mat.mElements[1][0];
		mElements[1][1] = mat.mElements[1][1];
		mElements[1][2] = mat.mElements[1][2];
		mElements[2][0] = mat.mElements[2][0];
		mElements[2][1] = mat.mElements[2][1];
		mElements[2][2] = mat.mElements[2][2];
	}
	
	public void setRow(int row, Vector3f vec)
	{
		mElements[row][0] = vec.mX;
		mElements[row][1] = vec.mY;
		mElements[row][2] = vec.mZ;
	}
	
	public void getRow(int row, Vector3f vec)
	{
		vec.set(mElements[row][0], mElements[row][1], mElements[row][2]);
	}
	
	public void transpose()
	{
		float tmp;
		
		tmp = mElements[0][1];
		mElements[0][1] = mElements[1][0];
		mElements[1][0] = tmp;
		
		tmp = mElements[0][2];
		mElements[0][2] = mElements[2][0];
		mElements[2][0] = tmp;

		tmp = mElements[1][2];
		mElements[1][2] = mElements[2][1];
		mElements[2][1] = tmp;
	}
	
	public Matrix3f clone()
	{
		return new Matrix3f(
				mElements[0][0], mElements[0][1], mElements[0][2],
				mElements[1][0], mElements[1][1], mElements[1][2],
				mElements[2][0], mElements[2][1], mElements[2][2]);
	}
	
	/*
	public void add(Matrix3f other)
	{
		for(int row = 0; row < 3; ++row)
		{
			for(int col = 0; col < 3; ++col)
			{
				mElements[row][col] += other.mElements[row][col];
			}
		}
	}
	
	public void mul(float scalar)
	{
		for(int row = 0; row < 3; ++row)
		{
			for(int col = 0; col < 3; ++col)
			{
				mElements[row][col] *= scalar;
			}
		}
	}
	*/
	
	public void mul(Matrix3f other)
	{
		/*
		float[][] tmp = new float[3][3];
		for(int row = 0; row < 3; ++row)
		{
			for(int col = 0; col < 3; ++col)
			{
				float val = 0.0f;
				for(int i = 0; i < 3; ++i)
				{
					val += get(row, i) * other.get(i, col);
				}
				tmp[row][col] = val;
			}
		}
		for(int row = 0; row < 3; ++row)
		{
			for(int col = 0; col < 3; ++col)
			{
				mElements[row][col] = tmp[row][col];
			}
		}
		*/
		
		float _00 = mElements[0][0] * other.mElements[0][0] +
					mElements[0][1] * other.mElements[1][0] +
					mElements[0][2] * other.mElements[2][0];
	
		float _01 = mElements[0][0] * other.mElements[0][1] +
					mElements[0][1] * other.mElements[1][1] +
					mElements[0][2] * other.mElements[2][1];
		
		float _02 = mElements[0][0] * other.mElements[0][2] +
					mElements[0][1] * other.mElements[1][2] +
					mElements[0][2] * other.mElements[2][2];
		
		float _10 = mElements[1][0] * other.mElements[0][0] +
					mElements[1][1] * other.mElements[1][0] +
					mElements[1][2] * other.mElements[2][0];
	
		float _11 = mElements[1][0] * other.mElements[0][1] +
					mElements[1][1] * other.mElements[1][1] +
					mElements[1][2] * other.mElements[2][1];
		
		float _12 = mElements[1][0] * other.mElements[0][2] +
					mElements[1][1] * other.mElements[1][2] +
					mElements[1][2] * other.mElements[2][2];
		
		float _20 = mElements[2][0] * other.mElements[0][0] +
					mElements[2][1] * other.mElements[1][0] +
					mElements[2][2] * other.mElements[2][0];
	
		float _21 = mElements[2][0] * other.mElements[0][1] +
					mElements[2][1] * other.mElements[1][1] +
					mElements[2][2] * other.mElements[2][1];
	
		float _22 = mElements[2][0] * other.mElements[0][2] +
					mElements[2][1] * other.mElements[1][2] +
					mElements[2][2] * other.mElements[2][2];
		
		mElements[0][0] = _00;
		mElements[0][1] = _01;
		mElements[0][2] = _02;
		
		mElements[1][0] = _10;
		mElements[1][1] = _11;
		mElements[1][2] = _12;
		
		mElements[2][0] = _20;
		mElements[2][1] = _21;
		mElements[2][2] = _22;
	}
	
	
	public float get(int row, int col)
	{
		return mElements[row][col];
	}
	
	public void set(int row, int col, float value)
	{
		mElements[row][col] = value;
	}

	public Vector3f rotate(Vector3f point)
	{
		float x = mElements[0][0] * point.mX +
					mElements[0][1] * point.mY +
					mElements[0][2] * point.mZ;
		
		float y = mElements[1][0] * point.mX +
					mElements[1][1] * point.mY +
					mElements[1][2] * point.mZ;
		
		float z = mElements[2][0] * point.mX +
					mElements[2][1] * point.mY +
					mElements[2][2] * point.mZ;
		
		return new Vector3f(x, y, z);
	}
}
