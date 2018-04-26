package com.craighamilton.dread;

import java.util.Collection;

public class IntersectionQuery
{
	private Vector3f mA, mB;
	private float mClosestRatio;
	private IntersectableObject mClosestObject;
	
	public IntersectionQuery(Vector3f a, Vector3f b)
	{
		mA = a.clone();
		mB = b.clone();
		mClosestRatio = 1.1f;
	}
	
	public interface SearchFilter
	{
		/**
		 * Returns true if the object in the collection should be considered for the test
		 */
		public boolean test(IntersectableObject obj);
	}
	
	public void testObjects(Collection<? extends IntersectableObject> objects, SearchFilter filter)
	{
		for(IntersectableObject obj : objects)
		{
			if(filter != null && !filter.test(obj))
			{
				continue;
			}
			
			IntersectableObject.LineIntersectionResult r = obj.intersectsLine(mA, mB);
			if(r.mIntersected)
			{
				if(r.mRatio < mClosestRatio)
				{
					mClosestRatio = r.mRatio;
					mClosestObject = obj;
				}
			}
		}
	}
	
	public float getClosestRatio()
	{
		return mClosestRatio;
	}
	
	public IntersectableObject getClosestObject()
	{
		return mClosestObject;
	}
}
