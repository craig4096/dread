package com.craighamilton.dread;

public class Matrix4f {

	private float mElements[][] = new float[4][4];
	
	public Matrix4f()
	{
	}
	
	public Matrix4f(float _11, float _12, float _13, float _14,
					float _21, float _22, float _23, float _24,
					float _31, float _32, float _33, float _34,
					float _41, float _42, float _43, float _44)
	{
		mElements[0][0] = _11;
		mElements[0][1] = _12;
		mElements[0][2] = _13;
		mElements[0][3] = _14;
		mElements[1][0] = _21;
		mElements[1][1] = _22;
		mElements[1][2] = _23;
		mElements[1][3] = _24;
		mElements[2][0] = _31;
		mElements[2][1] = _32;
		mElements[2][2] = _33;
		mElements[2][3] = _34;
		mElements[3][0] = _41;
		mElements[3][1] = _42;
		mElements[3][2] = _43;
		mElements[3][3] = _44;
	}
	
	public Matrix4f(Matrix3f mat)
	{
		set(mat);
	}
	
	public void set(Matrix4f mat)
	{
		mElements[0][0] = mat.mElements[0][0];
		mElements[0][1] = mat.mElements[0][1];
		mElements[0][2] = mat.mElements[0][2];
		mElements[0][3] = mat.mElements[0][3];
		
		mElements[1][0] = mat.mElements[1][0];
		mElements[1][1] = mat.mElements[1][1];
		mElements[1][2] = mat.mElements[1][2];
		mElements[1][3] = mat.mElements[1][3];
		
		mElements[2][0] = mat.mElements[2][0];
		mElements[2][1] = mat.mElements[2][1];
		mElements[2][2] = mat.mElements[2][2];
		mElements[2][3] = mat.mElements[2][3];
		
		mElements[3][0] = mat.mElements[3][0];
		mElements[3][1] = mat.mElements[3][1];
		mElements[3][2] = mat.mElements[3][2];
		mElements[3][3] = mat.mElements[3][3];
	}
	
	public void set(Matrix3f mat)
	{
		mElements[0][0] = mat.get(0, 0);
		mElements[0][1] = mat.get(0, 1);
		mElements[0][2] = mat.get(0, 2);
		mElements[0][3] = 0.0f;
		
		mElements[1][0] = mat.get(1, 0);
		mElements[1][1] = mat.get(1, 1);
		mElements[1][2] = mat.get(1, 2);
		mElements[1][3] = 0.0f;
		
		mElements[2][0] = mat.get(2, 0);
		mElements[2][1] = mat.get(2, 1);
		mElements[2][2] = mat.get(2, 2);
		mElements[2][3] = 0.0f;
		
		mElements[3][0] = 0.0f;
		mElements[3][1] = 0.0f;
		mElements[3][2] = 0.0f;
		mElements[3][3] = 1.0f;
	}
	
	public Matrix4f clone()
	{
		return new Matrix4f(
				mElements[0][0], mElements[0][1], mElements[0][2], mElements[0][3],
				mElements[1][0], mElements[1][1], mElements[1][2], mElements[1][3],
				mElements[2][0], mElements[2][1], mElements[2][2], mElements[2][3],
				mElements[3][0], mElements[3][1], mElements[3][2], mElements[3][3]);
	}
	
	public void mul(Matrix4f other)
	{
		float _00 = mElements[0][0] * other.mElements[0][0] +
					mElements[0][1] * other.mElements[1][0] +
					mElements[0][2] * other.mElements[2][0] +
					mElements[0][3] * other.mElements[3][0];
		
		float _01 = mElements[0][0] * other.mElements[0][1] +
					mElements[0][1] * other.mElements[1][1] +
					mElements[0][2] * other.mElements[2][1] +
					mElements[0][3] * other.mElements[3][1];
		
		float _02 = mElements[0][0] * other.mElements[0][2] +
					mElements[0][1] * other.mElements[1][2] +
					mElements[0][2] * other.mElements[2][2] +
					mElements[0][3] * other.mElements[3][2];
		
		float _03 = mElements[0][0] * other.mElements[0][3] +
					mElements[0][1] * other.mElements[1][3] +
					mElements[0][2] * other.mElements[2][3] +
					mElements[0][3] * other.mElements[3][3];
		
		float _10 = mElements[1][0] * other.mElements[0][0] +
					mElements[1][1] * other.mElements[1][0] +
					mElements[1][2] * other.mElements[2][0] +
					mElements[1][3] * other.mElements[3][0];
	
		float _11 = mElements[1][0] * other.mElements[0][1] +
					mElements[1][1] * other.mElements[1][1] +
					mElements[1][2] * other.mElements[2][1] +
					mElements[1][3] * other.mElements[3][1];
		
		float _12 = mElements[1][0] * other.mElements[0][2] +
					mElements[1][1] * other.mElements[1][2] +
					mElements[1][2] * other.mElements[2][2] +
					mElements[1][3] * other.mElements[3][2];
		
		float _13 = mElements[1][0] * other.mElements[0][3] +
					mElements[1][1] * other.mElements[1][3] +
					mElements[1][2] * other.mElements[2][3] +
					mElements[1][3] * other.mElements[3][3];
		
		float _20 = mElements[2][0] * other.mElements[0][0] +
					mElements[2][1] * other.mElements[1][0] +
					mElements[2][2] * other.mElements[2][0] +
					mElements[2][3] * other.mElements[3][0];

		float _21 = mElements[2][0] * other.mElements[0][1] +
					mElements[2][1] * other.mElements[1][1] +
					mElements[2][2] * other.mElements[2][1] +
					mElements[2][3] * other.mElements[3][1];
	
		float _22 = mElements[2][0] * other.mElements[0][2] +
					mElements[2][1] * other.mElements[1][2] +
					mElements[2][2] * other.mElements[2][2] +
					mElements[2][3] * other.mElements[3][2];
	
		float _23 = mElements[2][0] * other.mElements[0][3] +
					mElements[2][1] * other.mElements[1][3] +
					mElements[2][2] * other.mElements[2][3] +
					mElements[2][3] * other.mElements[3][3];
		
		float _30 = mElements[3][0] * other.mElements[0][0] +
					mElements[3][1] * other.mElements[1][0] +
					mElements[3][2] * other.mElements[2][0] +
					mElements[3][3] * other.mElements[3][0];

		float _31 = mElements[3][0] * other.mElements[0][1] +
					mElements[3][1] * other.mElements[1][1] +
					mElements[3][2] * other.mElements[2][1] +
					mElements[3][3] * other.mElements[3][1];
	
		float _32 = mElements[3][0] * other.mElements[0][2] +
					mElements[3][1] * other.mElements[1][2] +
					mElements[3][2] * other.mElements[2][2] +
					mElements[3][3] * other.mElements[3][2];
	
		float _33 = mElements[3][0] * other.mElements[0][3] +
					mElements[3][1] * other.mElements[1][3] +
					mElements[3][2] * other.mElements[2][3] +
					mElements[3][3] * other.mElements[3][3];
		
		mElements[0][0] = _00;
		mElements[0][1] = _01;
		mElements[0][2] = _02;
		mElements[0][3] = _03;
		
		mElements[1][0] = _10;
		mElements[1][1] = _11;
		mElements[1][2] = _12;
		mElements[1][3] = _13;
		
		mElements[2][0] = _20;
		mElements[2][1] = _21;
		mElements[2][2] = _22;
		mElements[2][3] = _23;
		
		mElements[3][0] = _30;
		mElements[3][1] = _31;
		mElements[3][2] = _32;
		mElements[3][3] = _33;
		
		
		/*
		float[][] tmp = new float[4][4];
		
		for(int row = 0; row < 4; ++row)
		{
			for(int col = 0; col < 4; ++col)
			{
				float val = 0.0f;
				for(int i = 0; i < 4; ++i)
				{
					val += get(row, i) * other.get(i, col);
				}
				tmp[row][col] = val;
			}
		}
		for(int row = 0; row < 4; ++row)
		{
			for(int col = 0; col < 4; ++col)
			{
				set(row, col, tmp[row][col]);
			}
		}
		*/
	}
	
	public void getRow(int row, float[] out)
	{
		out[0] = mElements[row][0];
		out[1] = mElements[row][1];
		out[2] = mElements[row][2];
		out[3] = mElements[row][3];
	}
	
	public Matrix3f toMatrix3()
	{
		return new Matrix3f(
				mElements[0][0], mElements[0][1], mElements[0][2],
				mElements[1][0], mElements[1][1], mElements[1][2],
				mElements[2][0], mElements[2][1], mElements[2][2]);
	}
	
	public Vector3f transform(Vector3f point)
	{
		float x = mElements[0][0] * point.mX +
					mElements[0][1] * point.mY +
					mElements[0][2] * point.mZ +
					mElements[0][3];
		
		float y = mElements[1][0] * point.mX +
					mElements[1][1] * point.mY +
					mElements[1][2] * point.mZ +
					mElements[1][3];
		
		float z = mElements[2][0] * point.mX +
					mElements[2][1] * point.mY +
					mElements[2][2] * point.mZ +
					mElements[2][3];
		
		return new Vector3f(x, y, z);
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
	
	
	public float get(int row, int col)
	{
		return mElements[row][col];
	}
	
	public void set(int row, int col, float value)
	{
		mElements[row][col] = value;
	}
	
	public Matrix4f getInverted()
	{
		float d = (mElements[0][0] * mElements[1][1] - mElements[0][1] * mElements[1][0]) * (mElements[2][2] * mElements[3][3] - mElements[2][3] * mElements[3][2]) -
		(mElements[0][0] * mElements[1][2] - mElements[0][2] * mElements[1][0]) * (mElements[2][1] * mElements[3][3] - mElements[2][3] * mElements[3][1]) +
		(mElements[0][0] * mElements[1][3] - mElements[0][3] * mElements[1][0]) * (mElements[2][1] * mElements[3][2] - mElements[2][2] * mElements[3][1]) +
		(mElements[0][1] * mElements[1][2] - mElements[0][2] * mElements[1][1]) * (mElements[2][0] * mElements[3][3] - mElements[2][3] * mElements[3][0]) -
		(mElements[0][1] * mElements[1][3] - mElements[0][3] * mElements[1][1]) * (mElements[2][0] * mElements[3][2] - mElements[2][2] * mElements[3][0]) +
		(mElements[0][2] * mElements[1][3] - mElements[0][3] * mElements[1][2]) * (mElements[2][0] * mElements[3][1] - mElements[2][1] * mElements[3][0]);

		d = 1.0f / d;

		Matrix4f rval = new Matrix4f();
		rval.mElements[0][0] = d * (mElements[1][1] * (mElements[2][2] * mElements[3][3] - mElements[2][3] * mElements[3][2]) + mElements[1][2] * (mElements[2][3] * mElements[3][1] - mElements[2][1] * mElements[3][3]) + mElements[1][3] * (mElements[2][1] * mElements[3][2] - mElements[2][2] * mElements[3][1]));
		rval.mElements[0][1] = d * (mElements[2][1] * (mElements[0][2] * mElements[3][3] - mElements[0][3] * mElements[3][2]) + mElements[2][2] * (mElements[0][3] * mElements[3][1] - mElements[0][1] * mElements[3][3]) + mElements[2][3] * (mElements[0][1] * mElements[3][2] - mElements[0][2] * mElements[3][1]));
		rval.mElements[0][2] = d * (mElements[3][1] * (mElements[0][2] * mElements[1][3] - mElements[0][3] * mElements[1][2]) + mElements[3][2] * (mElements[0][3] * mElements[1][1] - mElements[0][1] * mElements[1][3]) + mElements[3][3] * (mElements[0][1] * mElements[1][2] - mElements[0][2] * mElements[1][1]));
		rval.mElements[0][3] = d * (mElements[0][1] * (mElements[1][3] * mElements[2][2] - mElements[1][2] * mElements[2][3]) + mElements[0][2] * (mElements[1][1] * mElements[2][3] - mElements[1][3] * mElements[2][1]) + mElements[0][3] * (mElements[1][2] * mElements[2][1] - mElements[1][1] * mElements[2][2]));
		rval.mElements[1][0] = d * (mElements[1][2] * (mElements[2][0] * mElements[3][3] - mElements[2][3] * mElements[3][0]) + mElements[1][3] * (mElements[2][2] * mElements[3][0] - mElements[2][0] * mElements[3][2]) + mElements[1][0] * (mElements[2][3] * mElements[3][2] - mElements[2][2] * mElements[3][3]));
		rval.mElements[1][1] = d * (mElements[2][2] * (mElements[0][0] * mElements[3][3] - mElements[0][3] * mElements[3][0]) + mElements[2][3] * (mElements[0][2] * mElements[3][0] - mElements[0][0] * mElements[3][2]) + mElements[2][0] * (mElements[0][3] * mElements[3][2] - mElements[0][2] * mElements[3][3]));
		rval.mElements[1][2] = d * (mElements[3][2] * (mElements[0][0] * mElements[1][3] - mElements[0][3] * mElements[1][0]) + mElements[3][3] * (mElements[0][2] * mElements[1][0] - mElements[0][0] * mElements[1][2]) + mElements[3][0] * (mElements[0][3] * mElements[1][2] - mElements[0][2] * mElements[1][3]));
		rval.mElements[1][3] = d * (mElements[0][2] * (mElements[1][3] * mElements[2][0] - mElements[1][0] * mElements[2][3]) + mElements[0][3] * (mElements[1][0] * mElements[2][2] - mElements[1][2] * mElements[2][0]) + mElements[0][0] * (mElements[1][2] * mElements[2][3] - mElements[1][3] * mElements[2][2]));
		rval.mElements[2][0] = d * (mElements[1][3] * (mElements[2][0] * mElements[3][1] - mElements[2][1] * mElements[3][0]) + mElements[1][0] * (mElements[2][1] * mElements[3][3] - mElements[2][3] * mElements[3][1]) + mElements[1][1] * (mElements[2][3] * mElements[3][0] - mElements[2][0] * mElements[3][3]));
		rval.mElements[2][1] = d * (mElements[2][3] * (mElements[0][0] * mElements[3][1] - mElements[0][1] * mElements[3][0]) + mElements[2][0] * (mElements[0][1] * mElements[3][3] - mElements[0][3] * mElements[3][1]) + mElements[2][1] * (mElements[0][3] * mElements[3][0] - mElements[0][0] * mElements[3][3]));
		rval.mElements[2][2] = d * (mElements[3][3] * (mElements[0][0] * mElements[1][1] - mElements[0][1] * mElements[1][0]) + mElements[3][0] * (mElements[0][1] * mElements[1][3] - mElements[0][3] * mElements[1][1]) + mElements[3][1] * (mElements[0][3] * mElements[1][0] - mElements[0][0] * mElements[1][3]));
		rval.mElements[2][3] = d * (mElements[0][3] * (mElements[1][1] * mElements[2][0] - mElements[1][0] * mElements[2][1]) + mElements[0][0] * (mElements[1][3] * mElements[2][1] - mElements[1][1] * mElements[2][3]) + mElements[0][1] * (mElements[1][0] * mElements[2][3] - mElements[1][3] * mElements[2][0]));
		rval.mElements[3][0] = d * (mElements[1][0] * (mElements[2][2] * mElements[3][1] - mElements[2][1] * mElements[3][2]) + mElements[1][1] * (mElements[2][0] * mElements[3][2] - mElements[2][2] * mElements[3][0]) + mElements[1][2] * (mElements[2][1] * mElements[3][0] - mElements[2][0] * mElements[3][1]));
		rval.mElements[3][1] = d * (mElements[2][0] * (mElements[0][2] * mElements[3][1] - mElements[0][1] * mElements[3][2]) + mElements[2][1] * (mElements[0][0] * mElements[3][2] - mElements[0][2] * mElements[3][0]) + mElements[2][2] * (mElements[0][1] * mElements[3][0] - mElements[0][0] * mElements[3][1]));
		rval.mElements[3][2] = d * (mElements[3][0] * (mElements[0][2] * mElements[1][1] - mElements[0][1] * mElements[1][2]) + mElements[3][1] * (mElements[0][0] * mElements[1][2] - mElements[0][2] * mElements[1][0]) + mElements[3][2] * (mElements[0][1] * mElements[1][0] - mElements[0][0] * mElements[1][1]));
		rval.mElements[3][3] = d * (mElements[0][0] * (mElements[1][1] * mElements[2][2] - mElements[1][2] * mElements[2][1]) + mElements[0][1] * (mElements[1][2] * mElements[2][0] - mElements[1][0] * mElements[2][2]) + mElements[0][2] * (mElements[1][0] * mElements[2][1] - mElements[1][1] * mElements[2][0]));
		return rval;
	}
	
	public void invert()
	{
		Matrix4f inverted = getInverted();
		this.set(inverted);
	}
}
