package com.craighamilton.dread;

public class Sprite extends Object3D implements IntersectableObject {
	
	protected Texture mTexture;
	protected AttributeBuffer mTexCoords = null;
	
	public Sprite()
	{
		mTexture = null;
	}
	
	public Sprite(Texture texture)
	{
		mTexture = texture;
	}
	
	public void setTexCoords(float[] texcoords)
	{
		mTexCoords = new AttributeBuffer(texcoords);
	}
	
	public void setTexture(Texture texture)
	{
		mTexture = texture;
	}
	
	public Texture getTexture()
	{
		return mTexture;
	}
	
	public void drawWithColour(Renderer renderer, Colour colour)
	{
		Matrix4f previousModelMatrix = renderer.getModelMatrix();
		renderer.setModelMatrix(getModelMatrix());
		renderer.drawTexture(mTexture, mTexCoords, colour);
		renderer.setModelMatrix(previousModelMatrix);
	}
	
	public void draw(Renderer renderer)
	{
		Matrix4f previousModelMatrix = renderer.getModelMatrix();
		renderer.setModelMatrix(getModelMatrix());
		renderer.drawTexture(mTexture, mTexCoords);
		renderer.setModelMatrix(previousModelMatrix);
	}
	
	@Override
	public LineIntersectionResult intersectsLine(Vector3f va, Vector3f vb)
	{
		Matrix3f inverseRotation = mRotation.toMatrix3();
		inverseRotation.transpose();
		return Utility.LineIntersectsQuad(getPosition(), inverseRotation, mScale.mX, mScale.mY, va, vb);
	}
}
