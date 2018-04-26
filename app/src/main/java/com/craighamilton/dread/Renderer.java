package com.craighamilton.dread;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;


public class Renderer implements GLSurfaceView.Renderer
{
	private GameSession mGame;
	private GL10 mGL;
	
	private Matrix4f mProjectionMatrix = Utility.IdentityMatrix4();
	private Matrix4f mModelMatrix = Utility.IdentityMatrix4();
	private Matrix4f mViewMatrix = Utility.IdentityMatrix4();
	
	private int mRenderThreadId = -1;
	
	/**
     * Mesh used in drawTexture method
     * @see drawTexture()
	 */
	private final IndexBuffer mDrawTextureIndices;
	private final AttributeBuffer mDrawTextureMesh;
	private final AttributeBuffer mDefaultDrawTextureTexCoords;
	
	/**
	 * Constructor
	 * @param game
	 */
	public Renderer(GameSession game)
	{
		this.mGame = game;
		
		mDrawTextureIndices = new IndexBuffer(new short[]{ 0, 1, 2, 0, 2, 3 });
		mDrawTextureMesh = new AttributeBuffer(new float[]{
							-1.0f, -1.0f, 0.0f,
							1.0f, -1.0f, 0.0f,
							1.0f, 1.0f, 0.0f,
							-1.0f, 1.0f, 0.0f
								});
		mDefaultDrawTextureTexCoords = new AttributeBuffer(new float[]{
							0.0f, 1.0f,
							1.0f, 1.0f,
							1.0f, 0.0f,
							0.0f, 0.0f
							});
	}
	
	
	/**
	 * If Renderer.loadTexture is called on a different thread, or before the
	 * renderer has been initialised then we add a load request to this queue
	 * which will be processed in the next onDrawFrameCall
	 */
	private Queue<Texture> mTextureLoadRequests = new LinkedList<Texture>();
	
	
	private long mStartTime;
	
	@Override
	public void onDrawFrame(GL10 gl)
	{
		// load any unloaded textures
		synchronized(this)
		{
			if(mTextureLoadRequests.size() > 0)
			{
				Texture loadRequest = mTextureLoadRequests.poll();
				while(loadRequest != null)
				{
					loadRequest.loadGL(mGL, mGame);
					loadRequest = mTextureLoadRequests.poll();
				}
			}
		}
		
		long curTime = System.currentTimeMillis();
		long dt = curTime - mStartTime;
		long gFrameTimeMs = 33; // 33 = 1000 / 30fps
		if(dt < gFrameTimeMs) 
		{
			try {
				Thread.sleep(33 - dt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mStartTime = System.currentTimeMillis();
		
		// update game
		mGame.update(gFrameTimeMs * 0.001f); // convert to seconds
		
		// Render...
		// clear colour buffer and depth
		gl.glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		updateMatrices();
		// render all game-specific objects
		mGame.draw(this);
	}
	
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		gl.glViewport(0, 0, width, height);
		mGame.onSurfaceChanged(width, height);
	}
	
	/*
	private void checkCorrectThread()
	{
		if(android.os.Process.myTid() != mThreadId)
		{
			Log.d("Acorn", "ERROR: Loading resource on wrong thread");
		}
	}
	*/
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		// record the thread id that the opengl context is running on
		mRenderThreadId = android.os.Process.myTid();
		
		mStartTime = System.currentTimeMillis();
		mGL = gl;
		gl.glEnable(GL10.GL_TEXTURE_2D);
		setBlendingEnabled(true);
		
		// Reload all textures
		for(Texture texture : mTextureCache.values())
		{
			texture.loadGL(mGL, mGame);
		}
	}

	
	/*
	 * Pipeline matrices
	 */
	public void setViewMatrix(Matrix4f matrix)
	{
		mViewMatrix = matrix.clone();
		updateMatrices();
	}
	
	public Matrix4f getViewMatrix()
	{
		return mViewMatrix.clone();
	}
	
	public void setModelMatrix(Matrix4f matrix)
	{
		mModelMatrix = matrix.clone();
		updateMatrices();
	}
	
	public Matrix4f getModelMatrix()
	{
		return mModelMatrix.clone();
	}
	
	public void setProjectionMatrix(Matrix4f matrix)
	{
		mProjectionMatrix = matrix.clone();
		updateMatrices();
	}
	
	public Matrix4f getProjectionMatrix()
	{
		return mProjectionMatrix.clone();
	}
	
	/**
	 * uploads the mProjection, mViewMatrix and mModelMatrix to opengl
	 */
	private void updateMatrices()
	{	
		float[] tmp = new float[16];
		// Update Projection matrix
		mGL.glMatrixMode(GL10.GL_PROJECTION);
		Utility.toOpenGLMatrix(mProjectionMatrix, tmp);
		mGL.glLoadMatrixf(tmp, 0);
		
		// Update Model View Matrix
		mGL.glMatrixMode(GL10.GL_MODELVIEW);
		Matrix4f modelView = getViewMatrix();
		modelView.mul(getModelMatrix());
		Utility.toOpenGLMatrix(modelView, tmp);
		mGL.glLoadMatrixf(tmp, 0);
	}
	
	/**
	 * Texture cache
	 */
	private HashMap<String, Texture> mTextureCache = new HashMap<String, Texture>();
	
	private String createTextureCacheKey(String assetURL, Texture.WrapMode wrapMode)
	{
		String key = assetURL + "|";
		switch(wrapMode)
		{
		case Repeat:
			assetURL += "r";
			break;
		case ClampToEdge:
			assetURL += "c";
			break;
		}
		return key;
	}
	
	public Texture loadTexture(String assetURL, Texture.WrapMode wrapMode)
	{
		// check the texture cache first
		String key = createTextureCacheKey(assetURL, wrapMode);
		Texture texture = mTextureCache.get(key);
		if(texture != null)
		{
			return texture;
		}
		texture = Texture.load(mGame, assetURL, wrapMode);
		mTextureCache.put(key, texture);
		
		// if we are on the rendering thread when calling this then we can load straight away
		if(android.os.Process.myTid() == mRenderThreadId)
		{
			texture.loadGL(mGL, mGame);
		}
		else
		{
			// else add a texture load request
			synchronized(this)
			{
				mTextureLoadRequests.add(texture);
			}
		}
		
		return texture;
	}
	
	public Texture loadTexture(String assetURL)
	{
		return loadTexture(assetURL, Texture.WrapMode.Repeat);
	}
	
	public void releaseTexture(Texture texture)
	{
		// remove from cache
		String key = createTextureCacheKey(texture.getAssetURL(), texture.getWrapMode());
		mTextureCache.remove(key);
		// release
		texture.cleanUp(mGL);
	}
	
	/**
	 * Creates a gl texture from the given bitmap graphic
	 * @param bitmap
	 * @param wrapMode
	 * @return
	 */
	public Texture createTexture(Bitmap bitmap, Texture.WrapMode wrapMode)
	{
		return Texture.create(bitmap, wrapMode);
		// TODO: Add load request
	}
	
	public void setBlendingEnabled(boolean enabled)
	{
		if(enabled)
		{
			mGL.glEnable(GL10.GL_BLEND);
			mGL.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}
		else
		{
			mGL.glDisable(GL10.GL_BLEND);
		}
	}
	
	public int getScreenWidth()
	{
		return mGame.getScreenWidth();
	}
	
	public int getScreenHeight()
	{
		return mGame.getScreenHeight();
	}
	
	public float getXDpi()
	{
		return mGame.getResources().getDisplayMetrics().xdpi;
	}
	
	public float getYDpi()
	{
		return mGame.getResources().getDisplayMetrics().ydpi;
	}
	
	public GL10 getGLContext()
	{
		return mGL;
	}
	
	/**
	 * Draw a textured sprite
	 * @param texture
	 */
	public void drawTexture(Texture texture, AttributeBuffer texcoords)
	{
		if(texture != null)
		{
			mGL.glBindTexture(GL10.GL_TEXTURE_2D, texture.getGLTextureID());
		}
		
		if(texcoords != null)
		{
			texcoords.bindTexCoords2D(mGL);
		}
		else
		{
			mDefaultDrawTextureTexCoords.bindTexCoords2D(mGL);
		}
		
		mDrawTextureMesh.bindVertex3(mGL);
		mDrawTextureIndices.drawTriangles(mGL);
	}
	
	public void drawTexture(Texture texture, AttributeBuffer texcoords, Colour colour)
	{
		mGL.glColor4f(colour.mRed, colour.mGreen, colour.mBlue, colour.mAlpha);
		drawTexture(texture, texcoords);
		mGL.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
}


