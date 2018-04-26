package com.craighamilton.dread;

import org.json.JSONException;
import org.json.JSONObject;

public class Door implements IntersectableObject {
	
	/**
	 * The name of the sector that this door connects to
	 */
	private String mLinksToSectorName;
	
	/**
	 * The matrix defining the orientation and size of the intersection quad
	 */
	private Matrix4f mWorldToQuad;
	
	/**
	 * Constructor
	 * @param json
	 * @throws JSONException
	 * @throws IOException 
	 */
	public Door(JSONObject json) throws JSONException
	{
		// get the name of the sector this door links to
		mLinksToSectorName = json.getString("links_to");
		
		Matrix3f rotationMatrix = Utility.loadMatrix3(json.getJSONArray("rotation"));
		Vector3f position = Utility.loadVector3(json.getJSONObject("position"));
		
		Vector3f scale = new Vector3f((float)json.getDouble("width") / 2.0f, (float)json.getDouble("height") / 2.0f, 1.0f);
		
		// translate
		mWorldToQuad = Utility.CreateTranslationMatrix(position);
		// rotate
		mWorldToQuad.mul(new Matrix4f(rotationMatrix));
		// scale
		mWorldToQuad.mul(Utility.CreateScalingMatrix(scale));
		// invert
		mWorldToQuad.invert();
	}
	
	public String getLinksToSectorName()
	{
		return mLinksToSectorName;
	}


	@Override
	public LineIntersectionResult intersectsLine(Vector3f a, Vector3f b)
	{
		return Utility.LineIntersectsQuad(mWorldToQuad, a, b);
	}
}
