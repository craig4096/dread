package com.craighamilton.dread;

public interface IntersectableObject {

	public static class LineIntersectionResult
	{
		public boolean mIntersected;
		public float mRatio; // between 0 and 1
	}
	
	public LineIntersectionResult intersectsLine(Vector3f a, Vector3f b);
}
