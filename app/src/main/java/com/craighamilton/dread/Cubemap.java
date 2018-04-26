package com.craighamilton.dread;

import javax.microedition.khronos.opengles.GL10;


/**
 * Cubemap is used for rendering the main scenery of a sector
 * because the camera is fixed
 * @author craig
 *
 */
public class Cubemap {
	
	/**
	 * The textures defining the six sides of the cubemap
	 */
	private Texture[] mTextures = new Texture[6];
	
	private Matrix4f mTransform = Utility.IdentityMatrix4();
	
	private final IndexBuffer mIndexBuffer;
	private final AttributeBuffer mTexCoords;
	
	/**
	 * The mesh planes defining each side of the cubemap
	 */
	private AttributeBuffer[] mMeshes = new AttributeBuffer[6];
		
	public Cubemap(Renderer renderer, String dirname, Vector3f position)
	{
		String ext = ".jpg";
		mTextures[0] = renderer.loadTexture(dirname + "/posx" + ext, Texture.WrapMode.ClampToEdge); // posx
		mTextures[1] = renderer.loadTexture(dirname + "/negx" + ext, Texture.WrapMode.ClampToEdge); // negx
		mTextures[2] = renderer.loadTexture(dirname + "/posy" + ext, Texture.WrapMode.ClampToEdge); // posy
		mTextures[3] = renderer.loadTexture(dirname + "/negy" + ext, Texture.WrapMode.ClampToEdge); // negy
		mTextures[4] = renderer.loadTexture(dirname + "/posz" + ext, Texture.WrapMode.ClampToEdge); // posz
		mTextures[5] = renderer.loadTexture(dirname + "/negz" + ext, Texture.WrapMode.ClampToEdge); // negz
		
		mIndexBuffer = new IndexBuffer(new short[]{0, 1, 2, 0, 2, 3});
		mTexCoords = new AttributeBuffer(new float[]{
				0.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, 0.0f,
				0.0f, 0.0f
				});
		
		// Create all the mesh planes for each side of the cubemap
		mMeshes[0] = new AttributeBuffer( // POSX
				new float[] {
					1.0f, 1.0f, -1.0f,
					1.0f, -1.0f, -1.0f,
					1.0f, -1.0f, 1.0f,
					1.0f, 1.0f, 1.0f
				});
		
		mMeshes[1] = new AttributeBuffer( // NEGX
				new float[] {
					-1.0f, -1.0f, -1.0f,
					-1.0f, 1.0f, -1.0f,
					-1.0f, 1.0f, 1.0f,
					-1.0f, -1.0f, 1.0f
				});
		
		mMeshes[2] = new AttributeBuffer( // POSY
				new float[] {
					-1.0f, 1.0f, -1.0f,
					1.0f, 1.0f, -1.0f,
					1.0f, 1.0f, 1.0f,
					-1.0f, 1.0f, 1.0f
				});
		
		mMeshes[3] = new AttributeBuffer( // NEGY
				new float[] {
					1.0f, -1.0f, -1.0f,
					-1.0f, -1.0f, -1.0f,
					-1.0f, -1.0f, 1.0f,
					1.0f, -1.0f, 1.0f
				});
		
		mMeshes[4] = new AttributeBuffer( // POSZ
				new float[] {
					-1.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 1.0f,
					1.0f, -1.0f, 1.0f,
					-1.0f, -1.0f, 1.0f
				});
		
		mMeshes[5] = new AttributeBuffer( // NEGZ
				new float[] {
					-1.0f, -1.0f, -1.0f,
					1.0f, -1.0f, -1.0f,
					1.0f, 1.0f, -1.0f,
					-1.0f, 1.0f, -1.0f
				});
		setPosition(position);
	}
	
	private void setPosition(Vector3f position)
	{
		mTransform = Utility.CreateTranslationMatrix(position);
	}
	
	public void draw(Renderer renderer)
	{
		Matrix4f prevModelMatrix = renderer.getModelMatrix();
		renderer.setModelMatrix(mTransform);
		renderer.setBlendingEnabled(false);
		
		GL10 gl = renderer.getGLContext();
		mTexCoords.bindTexCoords2D(gl);
		
		for(int i = 0; i < 6; ++i)
		{
			mMeshes[i].bindVertex3(gl);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[i].getGLTextureID());
			mIndexBuffer.drawTriangles(gl);
		}
		
		renderer.setBlendingEnabled(true);
		renderer.setModelMatrix(prevModelMatrix);
	}
	
	public void cleanUp(Renderer renderer)
	{
		for(Texture texture : mTextures)
		{
			renderer.releaseTexture(texture);
		}
	}
}
