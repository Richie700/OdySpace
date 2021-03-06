package com.samuelberrien.odyspace.drawable.maps;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.samuelberrien.odyspace.R;
import com.samuelberrien.odyspace.utils.collision.Box;
import com.samuelberrien.odyspace.utils.game.Item;
import com.samuelberrien.odyspace.utils.graphics.ShaderLoader;
import com.samuelberrien.odyspace.utils.graphics.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by samuel on 01/04/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

public class HeightMap implements Item, Map {

	private final int NBSLICES = 30;
	private final int NBSTRIPS = 30;
	private int nbFaces;
	private float[] points;

	private FloatBuffer mPositions;

	private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	private int mLightPosHandle;
	private int mPositionHandle;
	private int mTextureHeightMapDataHandle;
	private int mTextureHeightMapUniformHandle;
	private int mTextureDataHandle;
	private int mTextureUniformHandle;
	private int mNbSlicesHandles;
	private int mNbStripsHandles;
	private int mCoeffHandle;
	private int mLightCoefHandle;
	private int mDistanceCoefHandle;
	private int mProgram;

	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;

	private float coeff;
	private float lightCoeff;
	private float distanceCoeff;

	private float limitHeight;

	private float mScale;
	private float[] mModelMatrix;

	public HeightMap(Context context,
					 int texHMResId,
					 int texResId,
					 float coeff,
					 float lightCoeff,
					 float distanceCoeff,
					 float scale,
					 float limitHeight) {
		int vertexShader = ShaderLoader.loadShader(
				GLES20.GL_VERTEX_SHADER,
				ShaderLoader.openShader(context, R.raw.height_map_vs));
		int fragmentShader = ShaderLoader.loadShader(
				GLES20.GL_FRAGMENT_SHADER,
				ShaderLoader.openShader(context, R.raw.height_map_fs));

		this.mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(this.mProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(this.mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(this.mProgram);

		mTextureHeightMapDataHandle = TextureHelper.loadTexture(context, texHMResId);
		mTextureDataHandle = TextureHelper.loadTexture(context, texResId);

		this.coeff = coeff;
		this.lightCoeff = lightCoeff;
		this.distanceCoeff = distanceCoeff;

		this.mScale = scale;
		this.limitHeight = limitHeight;
		this.mModelMatrix = new float[16];

		this.initPlan();
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vp");
		mTextureHeightMapUniformHandle = GLES20.glGetUniformLocation(mProgram, "textureHeight");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "textureMap");
		mNbStripsHandles = GLES20.glGetUniformLocation(mProgram, "nbStrips");
		mNbSlicesHandles = GLES20.glGetUniformLocation(mProgram, "nbSlices");
		mCoeffHandle = GLES20.glGetUniformLocation(mProgram, "coefficient");
		mLightCoefHandle = GLES20.glGetUniformLocation(mProgram, "u_light_coef");
		mDistanceCoefHandle = GLES20.glGetUniformLocation(mProgram, "u_distance_coef");
	}

	private void initPlan() {
		nbFaces = NBSTRIPS * (NBSLICES + 1) * 2;
		points = new float[nbFaces * 3];
		for (int indStrip = 0; indStrip < NBSTRIPS; indStrip++) {
			for (int indFace = 0; indFace <= NBSLICES; indFace++) {
				int indPoint = indStrip * (NBSLICES + 1) * 2 + indFace * 2;
				points[indPoint * 3] = (float) indFace / (float) NBSLICES;
				points[indPoint * 3 + 1] = 0.0f;
				points[indPoint * 3 + 2] = (float) indStrip / (float) NBSTRIPS;

				indPoint++;
				points[indPoint * 3] = (float) indFace / (float) NBSLICES;
				points[indPoint * 3 + 1] = 0.0f;
				points[indPoint * 3 + 2] = ((float) indStrip + 1) / (float) NBSTRIPS;
			}
		}
		mPositions = ByteBuffer.allocateDirect(points.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPositions.put(points).position(0);
	}

	@Override
	public float[] getRestreintArea(float[] position) {
		return new float[0];
	}

	@Override
	public float[] passToModelMatrix(float[] triangles) {
		float[] tmp;
		float[] res = new float[triangles.length];
		for (int i = 0; i < triangles.length; i += 3) {
			tmp = new float[]{triangles[i], triangles[i + 1], triangles[i + 2], 1f};
			Matrix.multiplyMV(tmp, 0, this.mModelMatrix, 0, tmp.clone(), 0);
			res[i] = tmp[0];
			res[i + 1] = tmp[1];
			res[i + 2] = tmp[2];
		}
		return res;
	}

	@Override
	public void update() {
		float[] mModelMatrix = new float[16];
		Matrix.setIdentityM(mModelMatrix, 0);

		Matrix.translateM(mModelMatrix, 0,
				-0.5f * this.mScale,
				this.limitHeight,
				-0.5f * this.mScale);
		Matrix.scaleM(mModelMatrix, 0, this.mScale, this.mScale, this.mScale);
		this.mModelMatrix = mModelMatrix.clone();
	}

	@Override
	public void draw(float[] pMatrix,
					 float[] vMatrix,
					 float[] mLightPosInEyeSpace,
					 float[] unused) {
		float[] mvMatrix = new float[16];
		float[] mvpMatrix = new float[16];

		Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, this.mModelMatrix, 0);
		Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);

		GLES20.glUseProgram(this.mProgram);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHeightMapDataHandle);
		GLES20.glUniform1i(mTextureHeightMapUniformHandle, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		GLES20.glUniform1i(mTextureUniformHandle, 1);

		mPositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mPositions);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		GLES20.glUniform3fv(mLightPosHandle, 1, mLightPosInEyeSpace, 0);

		GLES20.glUniform1i(mNbSlicesHandles, NBSLICES);
		GLES20.glUniform1i(mNbStripsHandles, NBSTRIPS);

		GLES20.glUniform1f(mCoeffHandle, this.coeff);

		GLES20.glUniform1f(mLightCoefHandle, this.lightCoeff);

		GLES20.glUniform1f(mDistanceCoefHandle, this.distanceCoeff);

		int nbStackTriangles = (NBSLICES + 1) * 2;
		for (int i = 0; i < NBSTRIPS; i++)
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * nbStackTriangles, nbStackTriangles);
	}

	@Override
	public boolean collideTest(float[] triangleArray, float[] modelMatrix, Box container) {
		return false;
	}

	@Override
	public boolean isCollided(Item other) {
		return false;
	}

	@Override
	public boolean isInside(Box levelLimits) {
		return false;
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public int getDamage() {
		return Integer.MAX_VALUE - 1;
	}

	@Override
	public void decrementLife(int minus) {

	}

	@Override
	public float[] clonePosition() {
		return new float[]{-0.5f * this.mScale, this.limitHeight, -0.5f * this.mScale};
	}
}
