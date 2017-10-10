package com.samuelberrien.odyspace.controls;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.samuelberrien.odyspace.R;
import com.samuelberrien.odyspace.utils.graphics.Color;
import com.samuelberrien.odyspace.utils.graphics.ShaderLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.samuelberrien.odyspace.controls.GamePad.limitScreen;

/**
 * Created by samuel on 17/04/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

class Joystick extends Control {

	private boolean isVisible;

	private int nbPoint = 64;
	private double circleLength = 0.6d;
	private float[] mCirclePoint = new float[nbPoint * 3];
	private double stickLength = 0.2d;
	private float[] mStickPoint = new float[nbPoint * 3];

	private FloatBuffer circleVertexBuffer;
	private FloatBuffer stickVertexBuffer;

	private float[] mPosition = new float[3];
	private float[] mStickPosition = new float[3];

	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	private int mProgram;

	private float color[] = Color.ControlsColor;

	Joystick() {
		this.isVisible = false;
	}

	@Override
	void initGraphics(Context context) {
		int vertexShader = ShaderLoader.loadShader(GLES20.GL_VERTEX_SHADER, ShaderLoader.openShader(context, R.raw.simple_vs));
		int fragmentShader = ShaderLoader.loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderLoader.openShader(context, R.raw.simple_fs));

		this.mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(this.mProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(this.mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(this.mProgram);

		this.makeCricle();
		this.makeStick();
		this.bind();
	}

	private void makeCricle() {
		for (int i = 0; i < this.nbPoint; i++) {
			double mTmpAngle = (double) i * Math.PI * 2d / (double) this.nbPoint;
			this.mCirclePoint[i * 3 + 0] = (float) (this.circleLength * Math.cos(mTmpAngle));
			this.mCirclePoint[i * 3 + 1] = (float) (this.circleLength * Math.sin(mTmpAngle));
			this.mCirclePoint[i * 3 + 2] = 0f;
		}
		ByteBuffer bb = ByteBuffer.allocateDirect(this.mCirclePoint.length * 4);
		bb.order(ByteOrder.nativeOrder());
		circleVertexBuffer = bb.asFloatBuffer();
		circleVertexBuffer.put(this.mCirclePoint);
		circleVertexBuffer.position(0);
	}

	private void makeStick() {
		for (int i = 0; i < this.nbPoint; i++) {
			double mTmpAngle = (double) (i - 1) * Math.PI * 2d / (double) this.nbPoint;
			this.mStickPoint[i * 3 + 0] = (float) (this.stickLength * Math.cos(mTmpAngle));
			this.mStickPoint[i * 3 + 1] = (float) (this.stickLength * Math.sin(mTmpAngle));
			this.mStickPoint[i * 3 + 2] = 0f;
		}
		ByteBuffer bb = ByteBuffer.allocateDirect(this.mStickPoint.length * 4);
		bb.order(ByteOrder.nativeOrder());
		stickVertexBuffer = bb.asFloatBuffer();
		stickVertexBuffer.put(this.mStickPoint);
		stickVertexBuffer.position(0);
	}

	private void bind() {
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	}

	@Override
	void updatePosition(float x, float y, float ratio) {
		x = x * ratio;
		this.mPosition[0] = x;
		this.mPosition[1] = y;
		this.mPosition[2] = 0f;

		this.mStickPosition[0] = x;
		this.mStickPosition[1] = y;
		this.mStickPosition[2] = 0f;
	}

	@Override
	void updateStick(float x, float y, float ratio) {
		x = x * ratio;
		double length = Math.sqrt(Math.pow(this.mPosition[0] - x, 2d) + Math.pow(this.mPosition[1] - y, 2d));
		if (length > this.circleLength - this.stickLength) {
			double xDist = x - this.mPosition[0];
			double yDist = y - this.mPosition[1];
			this.mStickPosition[0] = this.mPosition[0] + (float) ((this.circleLength - this.stickLength) * xDist / length);
			this.mStickPosition[1] = this.mPosition[1] + (float) ((this.circleLength - this.stickLength) * yDist / length);
			this.mStickPosition[2] = 0f;
		} else {
			this.mStickPosition[0] = x;
			this.mStickPosition[1] = y;
			this.mStickPosition[2] = 0f;
		}
	}

	float[] getStickPosition() {
		if (this.isVisible) {
			return new float[]{-(this.mStickPosition[0] - this.mPosition[0]) / (float) (this.circleLength - this.stickLength), (this.mStickPosition[1] - this.mPosition[1]) / (float) (this.circleLength - this.stickLength)};
		} else {
			return new float[]{0f, 0f};
		}
	}

	@Override
	void setPointerID(int pointerID) {
		this.setVisible(true);
		super.setPointerID(pointerID);
	}

	private void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	void clear() {
		this.setVisible(false);
		super.clear();
	}

	@Override
	boolean canCatchID(float x, float y, float ratio) {
		return x > limitScreen;
	}

	@Override
	public void draw(float ratio) {
		if (this.isVisible) {
			GLES20.glUseProgram(this.mProgram);

			GLES20.glLineWidth(5f);

			float[] mViewMatrix = new float[16];
			Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			float[] mVPMatrix = new float[16];
			float[] mPMatrix = new float[16];
			Matrix.orthoM(mPMatrix, 0, -1f * ratio, 1f * ratio, -1f, 1f, -1f, 1f);
			Matrix.multiplyMM(mVPMatrix, 0, mPMatrix, 0, mViewMatrix, 0);
			float[] mMVPMatrix = new float[16];
			float[] mMMatrix = new float[16];
			Matrix.setIdentityM(mMMatrix, 0);
			Matrix.translateM(mMMatrix, 0, this.mPosition[0], this.mPosition[1], this.mPosition[2]);
			Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mMMatrix, 0);

			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, circleVertexBuffer);
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
			GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, this.mCirclePoint.length / 3);

			//Stick
			GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, stickVertexBuffer);
			Matrix.setIdentityM(mMMatrix, 0);
			Matrix.translateM(mMMatrix, 0, this.mStickPosition[0], this.mStickPosition[1], this.mStickPosition[2]);
			Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mMMatrix, 0);
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
			GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, this.mStickPoint.length / 3);
			GLES20.glDisableVertexAttribArray(mPositionHandle);

			GLES20.glLineWidth(1f);
		}
	}
}