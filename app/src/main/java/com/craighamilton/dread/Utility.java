package com.craighamilton.dread;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.craighamilton.dread.IntersectableObject.LineIntersectionResult;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.AlphaAnimation;

public class Utility {

    public static JSONObject loadJSONObject(InputStream is) throws IOException, JSONException
    {
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		return new JSONObject(new String(buffer, "UTF-8"));
    }
    
    public static JSONArray loadJSONArray(InputStream is) throws IOException, JSONException
    {
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		return new JSONArray(new String(buffer, "UTF-8"));
    }
    
    public static Matrix3f loadMatrix3(JSONArray json) throws JSONException
    {
    	return new Matrix3f(
    			(float)json.getDouble(0), (float)json.getDouble(1), (float)json.getDouble(2),
    			(float)json.getDouble(3), (float)json.getDouble(4), (float)json.getDouble(5),
    			(float)json.getDouble(6), (float)json.getDouble(7), (float)json.getDouble(8));
    }
    
    public static Vector3f loadVector3(JSONObject json) throws JSONException
    {
    	return new Vector3f((float)json.getDouble("x"), (float)json.getDouble("y"), (float)json.getDouble("z"));
    }
    
    
    public static void toOpenGLMatrix(Matrix4f mat, float[] out)
    {
		out[0] = mat.get(0, 0);
		out[1] = mat.get(1, 0);
		out[2] = mat.get(2, 0);
		out[3] = mat.get(3, 0);
		
		out[4] = mat.get(0, 1);
		out[5] = mat.get(1, 1);
		out[6] = mat.get(2, 1);
		out[7] = mat.get(3, 1);
		
		out[8] = mat.get(0, 2);
		out[9] = mat.get(1, 2);
		out[10] = mat.get(2, 2);
		out[11] = mat.get(3, 2);
		
		out[12] = mat.get(0, 3);
		out[13] = mat.get(1, 3);
		out[14] = mat.get(2, 3);
		out[15] = mat.get(3, 3);
    }
    
    
    public static Matrix4f IdentityMatrix4()
    {
    	return new Matrix4f(
    			1.0f, 0.0f, 0.0f, 0.0f,
    			0.0f, 1.0f, 0.0f, 0.0f,
    			0.0f, 0.0f, 1.0f, 0.0f,
    			0.0f, 0.0f, 0.0f, 1.0f
    			);
    }
    
    public static Matrix3f IdentityMatrix3()
    {
    	return new Matrix3f(
    			1.0f, 0.0f, 0.0f,
    			0.0f, 1.0f, 0.0f,
    			0.0f, 0.0f, 1.0f);
    }
    
    public static Matrix4f CreatePerspectiveProjectionMatrix(float yFov, float aspect, float zNear, float zFar)
    {
    	
    	float f = 1.0f / (float)Math.tan(Math.toRadians(yFov) / 2.0f);
    	return new Matrix4f(
    			f / aspect, 0.0f, 0.0f, 0.0f,
    			0.0f, f, 0.0f, 0.0f,
    			0.0f, 0.0f, (zFar + zNear) / (zNear - zFar), (2 * zFar * zNear) / (zNear - zFar),
    			0.0f, 0.0f, -1.0f, 0.0f
    			);
    }
    
    public static Matrix4f CreateOrthographicProjectionMatrix(float left, float right, float bottom, float top, float near, float far)
    {
    	return new Matrix4f(
    			2.0f / (right - left), 0.0f, 0.0f, -((right + left) / (right - left)),
    			0.0f, 2.0f / (top - bottom), 0.0f, -((top + bottom) / (top - bottom)),
    			0.0f, 0.0f, -2.0f / (far - near), -((far + near) / (far - near)),
    			0.0f, 0.0f, 0.0f, 1.0f);
    }
    
    public static Matrix4f CreateTranslationMatrix(Vector3f translation)
    {
    	return new Matrix4f(
    			1.0f, 0.0f, 0.0f, translation.mX,
    			0.0f, 1.0f, 0.0f, translation.mY,
    			0.0f, 0.0f, 1.0f, translation.mZ,
    			0.0f, 0.0f, 0.0f, 1.0f);
    }
    
    public static Matrix4f CreateScalingMatrix(Vector3f scale)
    {
    	return new Matrix4f(
    			scale.mX, 0.0f, 0.0f, 0.0f,
    			0.0f, scale.mY, 0.0f, 0.0f,
    			0.0f, 0.0f, scale.mZ, 0.0f,
    			0.0f, 0.0f, 0.0f, 1.0f);
    }
    

    
    public static Matrix3f CreateXRotationMatrix(float rotation)
    {
    	float cost = (float)Math.cos(rotation);
    	float sint = (float)Math.sin(rotation);
    	
    	return new Matrix3f(
    			1.0f, 0.0f, 0.0f,
    			0.0f, cost, -sint,
    			0.0f, sint, cost);
    }
    
    public static Matrix3f CreateYRotationMatrix(float rotation)
    {
    	float cost = (float)Math.cos(rotation);
    	float sint = (float)Math.sin(rotation);
    	
    	return new Matrix3f(
    			cost, 0.0f, -sint,
    			0.0f, 1.0f, 0.0f,
    			sint, 0.0f, cost);
    }
    
    public static Matrix3f CreateZRotationMatrix(float rotation)
    {
    	float cost = (float)Math.cos(rotation);
    	float sint = (float)Math.sin(rotation);
    	
    	return new Matrix3f(
    			cost, -sint, 0.0f,
    			sint, cost, 0.0f,
    			0.0f, 0.0f, 1.0f);
    }
    
  
	//private static Matrix4f mOrthoProjection = Utility.CreateOrthographicProjectionMatrix(-1, 1, -1, 1, -1, 1);
	
	public static class RenderState2D
	{
		private Matrix4f mPrevProjMat;
		private Matrix4f mPrevViewMat;
		private Renderer mRenderer;
	}
	
	public static RenderState2D begin2DDraw(Renderer renderer, float width, float height)
	{
		Matrix4f orthoProjection = Utility.CreateOrthographicProjectionMatrix(
				0, width, 0, height, -1, 1);
		
		RenderState2D rs = new RenderState2D();
		rs.mRenderer = renderer;
		rs.mPrevProjMat = renderer.getProjectionMatrix();
		rs.mPrevViewMat = renderer.getViewMatrix();
		
		renderer.setProjectionMatrix(orthoProjection);
		renderer.setViewMatrix(Utility.IdentityMatrix4());
		return rs;
	}
    
    public static void end2DDraw(RenderState2D rs)
    {	
		rs.mRenderer.setViewMatrix(rs.mPrevViewMat);
		rs.mRenderer.setProjectionMatrix(rs.mPrevProjMat);
    }
    
    public static <T> int getRandomIndex(Vector<T> v)
    {
		return (int)(Math.floor(Math.random() * (double)v.size())) % v.size();
    }
    
    public static <T> T getRandomElement(Vector<T> v)
    {
		return v.get(getRandomIndex(v));
    }
    
    public static void saveStringArray(SharedPreferences.Editor editor, String arrayName, Vector<String> array)
    {
    	editor.putInt(arrayName + "_size", array.size());
    	for(int i = 0; i < array.size(); ++i)
    	{
    		editor.putString(arrayName + "_" + i, array.get(i));
    	}
    }
    
    public static Vector<String> loadStringArray(SharedPreferences prefs, String arrayName)
    {
    	Vector<String> array = new Vector<String>();
    	int size = prefs.getInt(arrayName + "_size", 0);
    	for(int i = 0; i < size; ++i)
    	{
    		array.add(prefs.getString(arrayName + "_" + i, ""));
    	}
    	return array;
    }
    
    /**
     * Checks if a line intersects with an orientated quad
     * @param worldToQuad Inverse quad transformation matrix
     * @param va line.a
     * @param vb line.b 
     * @return the result of the intersection
     */
    public static IntersectableObject.LineIntersectionResult LineIntersectsQuad(Matrix4f worldToQuad, Vector3f va, Vector3f vb)
    {
		LineIntersectionResult rval = new LineIntersectionResult();
		rval.mIntersected = false;
		
		Vector3f a = worldToQuad.transform(va);
		Vector3f b = worldToQuad.transform(vb);
		
		float ratio = -1.0f;
		// check if it intersects the xy plane
		if(a.mZ < 0.0f)
		{
			if(b.mZ >= 0.0f)
			{
				ratio = -a.mZ / (b.mZ - a.mZ);
			}
		}
		else
		{
			if(b.mZ < 0.0f)
			{
				ratio = a.mZ / (a.mZ - b.mZ);
			}
		}
		if(ratio >= 0.0f)
		{
			// calculate the point of intersection
			Vector3f poi = (Vector3f)b.clone();
			poi.sub(a);
			poi.scale(ratio);
			poi.add(a);
			
			// check if the poi is inside the scale boundaries
			// first calculate poi relative to the position of the sprite on
			// the xy plane
			if(poi.mX >= -1.0f && poi.mX <= 1.0f &&
					poi.mY >= -1.0f && poi.mY <= 1.0f)
			{
				rval.mIntersected = true;
				rval.mRatio = ratio;
			}
		}
		
		return rval;
    }
    
    /**
     * Alternative version to above method, uses a world centre position, rotation matrix and width.height params
     * to define quad
     * @param worldPos
     * @param inverseRotationMatrix
     * @param width
     * @param height
     * @param va
     * @param vb
     * @return
     */
    public static IntersectableObject.LineIntersectionResult LineIntersectsQuad(Vector3f worldPos,
    		Matrix3f inverseRotationMatrix, float scaleX, float scaleY, Vector3f va, Vector3f vb)
    {
		LineIntersectionResult rval = new LineIntersectionResult();
		rval.mIntersected = false;
		
		Vector3f ta = worldPos.clone();
		ta.sub(va);
		Vector3f tb = worldPos.clone();
		tb.sub(vb);
		
		// rotate these points into door space
		Vector3f a = inverseRotationMatrix.rotate(ta);
		Vector3f b = inverseRotationMatrix.rotate(tb);
		
		
		float ratio = -1.0f;
		// check if it intersects the xy plane
		if(a.mZ < 0.0f)
		{
			if(b.mZ >= 0.0f)
			{
				ratio = -a.mZ / (b.mZ - a.mZ);
			}
		}
		else
		{
			if(b.mZ < 0.0f)
			{
				ratio = a.mZ / (a.mZ - b.mZ);
			}
		}
		if(ratio >= 0.0f)
		{
			// calculate the point of intersection
			Vector3f poi = (Vector3f)b.clone();
			poi.sub(a);
			poi.scale(ratio);
			poi.add(a);
			
			// check if the poi is inside the scale boundaries
			// first calculate poi relative to the position of the sprite on
			// the xy plane
			if(poi.mX >= -scaleX && poi.mX <= scaleX &&
					poi.mY >= -scaleY && poi.mY <= scaleY)
			{
				rval.mIntersected = true;
				rval.mRatio = ratio;
			}
		}
		
		return rval;
    }
    
    
    public static float GetHeadingFromPoint(float x, float y)
    {
		// normalise (x, y) components of viewDir
		float len = (x * x) + (y * y);
		if(len != 0.0f)
		{
			x /= len;
			y /= len;
			
			float val = (float)Math.atan2(x, y);
			if(val < 0.0f)
			{
				val = (float)Math.PI + ((float)Math.PI + val);
			}
			return val;
		}
		return 0.0f;
    }
    
    public static void putStringSet(SharedPreferences.Editor edit, String setName, Set<String> stringSet)
    {
    	StringBuilder builder = new StringBuilder();
    	Iterator<String> it = stringSet.iterator();
    	while(it.hasNext())
    	{
    		String str = it.next();
    		int length = str.length();
    		builder.append("[" + length + "]");
    		builder.append(str);
    	}
    	edit.putString(setName, builder.toString());
    }
    
    public static Set<String> getStringSet(SharedPreferences prefs, String setName)
    {
    	String setString = prefs.getString(setName, "");
    	
    	HashSet<String> out = new HashSet<String>();
    	
    	int a = setString.indexOf('[');
    	int b = setString.indexOf(']', a+1);
    	while(a != -1 && b != -1)
    	{
	    	int length = Integer.parseInt(setString.substring(a+1, b));
	    	
	    	a = b+1;
	    	b = a + length;
	    	if(a != -1 && b != -1)
	    	{
	    		out.add(setString.substring(a, b));
	    	}
	    	
	    	a = setString.indexOf("[", b);
	    	b = setString.indexOf("]", a+1);
    	}
    	return out;
    }
    
    /**
     * Sets the view's alpha value, this is to support API's < 11
     * @param view
     * @param alpha
     */
    public static void setViewAlpha(View view, float alpha)
    {
    	AlphaAnimation a = new AlphaAnimation(alpha, alpha);
    	a.setDuration(0);
    	a.setFillAfter(true);
    	view.startAnimation(a);
    }
    
    
	public static String getString(Context context, String key)
	{
		int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
		if(resId > 0)
		{
			return context.getResources().getString(resId);
		}
		return key;
	}
	
	
	public static int nextPowerOfTwo(int value)
	{
		int pwrTwo = 1;

		while(pwrTwo < value)
			pwrTwo <<= 1;

		return pwrTwo;
	}
}
