package com.samuelberrien.odyspace.objects;

import android.content.Context;
import android.opengl.Matrix;

import com.samuelberrien.odyspace.drawable.maps.NoiseMap;
import com.samuelberrien.odyspace.drawable.obj.ObjModelMtl;
import com.samuelberrien.odyspace.utils.game.LevelLimits;
import com.samuelberrien.odyspace.utils.collision.Triangle;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;


/**
 * Created by samuel on 18/04/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

public class BaseItem extends ObjModelMtl {

    private native boolean areCollided(float[] mPointItem1, float[] mModelMatrix1, float[] mPointItem2, float[] mModelMatrix2);

    static {
        System.loadLibrary("collision");
    }

    protected int life;

    protected float[] mPosition;
    protected float[] mSpeed;
    protected float[] mAcceleration;

    protected float[] mRotationMatrix;

    protected float[] mModelMatrix;

    protected float radius;

    protected float scale;

    public BaseItem(Context context, String objFileName, String mtlFileName, float lightAugmentation, float distanceCoef, int life, float[] mPosition, float[] mSpeed, float[] mAcceleration, float scale) {
        super(context, objFileName, mtlFileName, lightAugmentation, distanceCoef);
        this.life = life;
        this.mPosition = mPosition;
        this.mSpeed = mSpeed;
        this.mAcceleration = mAcceleration;
        this.mRotationMatrix = new float[16];
        Matrix.setIdentityM(this.mRotationMatrix, 0);
        this.mModelMatrix = new float[16];
        Matrix.setIdentityM(this.mModelMatrix, 0);
        this.scale = scale;
        this.radius = this.scale;
    }

    public BaseItem(ObjModelMtl objModelMtl, int life, float[] mPosition, float[] mSpeed, float[] mAcceleration, float scale) {
        super(objModelMtl);
        this.life = life;
        this.mPosition = mPosition;
        this.mSpeed = mSpeed;
        this.mAcceleration = mAcceleration;
        this.mRotationMatrix = new float[16];
        Matrix.setIdentityM(this.mRotationMatrix, 0);
        this.mModelMatrix = new float[16];
        Matrix.setIdentityM(this.mModelMatrix, 0);
        this.scale = scale;
        this.radius = this.scale;
    }

    public void changeColor(Random rand) {
        ArrayList<FloatBuffer> tmpA = super.makeColor(rand);
        ArrayList<FloatBuffer> tmpD = super.makeColor(rand);
        ArrayList<FloatBuffer> tmpS = super.makeColor(rand);
        super.setColors(tmpA, tmpD, tmpS);
    }

    public boolean isAlive() {
        return this.life > 0;
    }

    public boolean isCollided(BaseItem other) {
        return this.areCollided(this.allCoordsFloatArray.clone(), this.mModelMatrix.clone(), other.allCoordsFloatArray.clone(), other.mModelMatrix.clone());
    }

    public void decrementsBothLife(BaseItem other) {
        int otherLife = other.life;
        other.life -= this.life;
        this.life -= otherLife;
    }

    public boolean isOutOfBound(LevelLimits levelLimits) {
        return !levelLimits.isInside(this.mPosition);
    }

    public void mapCollision(NoiseMap map) {
        if(this.areCollided(this.allCoordsFloatArray.clone(), this.mModelMatrix.clone(), map.getRestreintArea(this.mPosition).clone(), map.getModelMatrix().clone())) {
            this.life = 0;
        }
    }

    public void move() {
        this.mSpeed[0] += this.mAcceleration[0];
        this.mSpeed[1] += this.mAcceleration[1];
        this.mSpeed[2] += this.mAcceleration[2];

        this.mPosition[0] += this.mSpeed[0];
        this.mPosition[1] += this.mSpeed[1];
        this.mPosition[2] += this.mSpeed[2];

        float[] tmp = new float[16];
        Matrix.setIdentityM(tmp, 0);
        Matrix.translateM(tmp, 0, this.mPosition[0], this.mPosition[1], this.mPosition[2]);
        Matrix.scaleM(tmp, 0, this.scale, this.scale, this.scale);
        this.mModelMatrix = tmp.clone();
    }

    public void draw(float[] pMatrix, float[] vMatrix, float[] mLightPosInEyeSpace, float[] mCameraPosition) {
        float[] mvMatrix = new float[16];
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, this.mModelMatrix, 0);
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);
        super.draw(mvpMatrix, mvMatrix, mLightPosInEyeSpace, mCameraPosition);
    }
}
