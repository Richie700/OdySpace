package com.samuelberrien.odyspace.utils;

import java.util.Random;

/**
 * Created by samuel on 26/04/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

public class LevelLimits {

    private float xMax;
    private float xMin;
    private float yMax;
    private float yMin;
    private float zMax;
    private float zMin;

    public LevelLimits(float xMax, float xMin, float yMax, float yMin, float zMax, float zMin){
        if(xMax < xMin || yMax < yMin || zMax < zMin){
            System.out.println("ERROR");
            throw new RuntimeException("Invalid(s) value(s) Max < Min");
        }
        this.xMax = xMax;
        this.xMin = xMin;
        this.yMax = yMax;
        this.yMin = yMin;
        this.zMax = zMax;
        this.zMin = zMin;
    }

    public boolean isInside(float[] xyz){
        return this.xMax > xyz[0] && this.xMin < xyz[0] && this.yMax > xyz[1] && this.yMin < xyz[1] && this.zMax > xyz[2] && this.zMin < xyz[2];
    }

    public float[] generateRandomPos(Random rand) {
        return new float[]{rand.nextFloat() * (this.xMax - this.xMin) + this.xMin, rand.nextFloat() * (this.yMax - this.yMin) + this.yMin, rand.nextFloat() * (this.zMax - this.zMin) + this.zMin};
    }

    public LevelLimits[] makeOctSons(){
        LevelLimits[] sons = new LevelLimits[8];
        sons[0] = new LevelLimits((this.xMax - this.xMin) / 2f + this.xMin, this.xMin, this.yMax, (this.yMax - this.yMin) / 2f + this.yMin, this.zMax, (this.zMax - this.zMin) / 2f + this.zMin);
        sons[1] = new LevelLimits(this.xMax, (this.xMax - this.xMin) / 2f + this.xMin, this.yMax, (this.yMax - this.yMin) / 2f + this.yMin, this.zMax, (this.zMax - this.zMin) / 2f + this.zMin);
        sons[2] = new LevelLimits((this.xMax - this.xMin) / 2f + this.xMin, this.xMin, this.yMax, (this.yMax - this.yMin) / 2f + this.yMin, (this.zMax - this.zMin) / 2f + this.zMin, this.zMin);
        sons[3] = new LevelLimits(this.xMax, (this.xMax - this.xMin) / 2f + this.xMin, this.yMax, (this.yMax - this.yMin) / 2f + this.yMin, (this.zMax - this.zMin) / 2f + this.zMin, this.zMin);

        sons[4] = new LevelLimits((this.xMax - this.xMin) / 2f + this.xMin, this.xMin, (this.yMax - this.yMin) / 2f + this.yMin, this.yMin, this.zMax, (this.zMax - this.zMin) / 2f + this.zMin);
        sons[5] = new LevelLimits(this.xMax, (this.xMax - this.xMin) / 2f + this.xMin, (this.yMax - this.yMin) / 2f + this.yMin, this.yMin, this.zMax, (this.zMax - this.zMin) / 2f + this.zMin);
        sons[6] = new LevelLimits((this.xMax - this.xMin) / 2f + this.xMin, this.xMin, (this.yMax - this.yMin) / 2f + this.yMin, this.yMin, (this.zMax - this.zMin) / 2f + this.zMin, this.zMin);
        sons[7] = new LevelLimits(this.xMax, (this.xMax - this.xMin) / 2f + this.xMin, (this.yMax - this.yMin) / 2f + this.yMin, this.yMin, (this.zMax - this.zMin) / 2f + this.zMin, this.zMin);
        return sons;
    }

    public float getSizeLength(){
        return this.xMax - this.xMin;
    }
}