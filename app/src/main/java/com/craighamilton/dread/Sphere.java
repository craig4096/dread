package com.craighamilton.dread;

public class Sphere implements IntersectableObject {

	private Vector3f mPosition;
	private float mRadius;
	
	public Sphere(Vector3f position, float radius)
	{
		this.mPosition = position;
		this.mRadius = radius;
	}
	
	public static class RayIntersectResult
	{
		public boolean mIntersected;
		public float mDistance;
		
		public RayIntersectResult(boolean intersected, float distance)
		{
			mIntersected = intersected;
			mDistance = distance;
		}
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param distance distance from a 
	 * @return
	 */
	public RayIntersectResult intersectsRay(Vector3f origin_, Vector3f direction)
	{
		// adjust ray origin so that it is relative to the sphere centre, we can then
		// treat the sphere as if it is positioned at the origin (0,0,0)
		Vector3f origin = (Vector3f)origin_.clone();
		origin.sub(mPosition);
		
		// ray equation: P = O + Dt, where O = origin, D = direction
		// sphere equation = P2 - R2  = 0, where R = radius
		// substitute P in sphere equ:
		//    (O + Dt)2 - R2 = 0
		//	  expand:
		//	  O2 + 2(ODt) + Dt2 - R2 = 0
		// f(t) = [Dt2] + [2ODt] + [O2 - R2]
		// f(t) = D2(t)2 + 2OD(t) + [O2 - R2]
		// quadratic equ.
		// a = D2
		float a = direction.dot(direction);
		// b = 2OD
		float b = 2 * (direction.dot(origin));
		// c = [O2 - R2]
		float c = origin.dot(origin) - (mRadius * mRadius);
		
		// now solve the quadratic equation
		// calculate discriminant (b2 - 4ac)
		float d = (b * b) - (4 * a * c);
		
		// has no roots - did not intersect
		if(d < 0.0f)
		{
			return new RayIntersectResult(false, 0.0f);
		}
		// has one root - intersected at the edge of the sphere
		else if(d == 0.0f)
		{
			float t0 = -b / (2 * a);
			return new RayIntersectResult(true, t0);
		}
		else
		{
			// has two roots
			float t0 = (-b + (float)Math.sqrt(d)) / (2 * a);
			//float t1 = (-b - (float)Math.sqrt(d)) / (2 * a);
			return new RayIntersectResult(true, t0);
		}
	}
	
	public LineIntersectionResult intersectsLine(Vector3f a, Vector3f b)
	{
		// return value
		LineIntersectionResult rval = new LineIntersectionResult();
		rval.mIntersected = false;
		
		Vector3f dir = (Vector3f)b.clone();
		dir.sub(a);
		dir.normalize();
		RayIntersectResult r = intersectsRay(a, dir);
		if(r.mIntersected)
		{
			float length = dir.dot(b);
			if(r.mDistance >= 0.0f && r.mDistance <= length)
			{
				rval.mIntersected = true;
				rval.mRatio = r.mDistance / length;
			}
		}
		return rval;
	}
	
	public Vector3f getPosition()
	{
		return mPosition;
	}
	
	public float getRadius()
	{
		return mRadius;
	}
}
