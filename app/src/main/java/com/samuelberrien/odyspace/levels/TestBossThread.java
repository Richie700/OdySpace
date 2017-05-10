package com.samuelberrien.odyspace.levels;

import android.content.Context;

import com.samuelberrien.odyspace.R;
import com.samuelberrien.odyspace.drawable.HeightMap;
import com.samuelberrien.odyspace.drawable.controls.Controls;
import com.samuelberrien.odyspace.drawable.controls.Joystick;
import com.samuelberrien.odyspace.objects.BaseItem;
import com.samuelberrien.odyspace.objects.Boss;
import com.samuelberrien.odyspace.objects.Ship;
import com.samuelberrien.odyspace.utils.collision.Octree;
import com.samuelberrien.odyspace.utils.game.Level;
import com.samuelberrien.odyspace.utils.game.LevelLimits;

import java.util.ArrayList;

/**
 * Created by samuel on 09/05/17.
 * Copyright samuel, 2016 - 2017.
 * Toute reproduction ou utilisation sans l'autorisation
 * de l'auteur engendrera des poursuites judiciaires.
 */

public class TestBossThread implements Level {

    private Context context;

    private Ship ship;
    private LevelLimits levelLimits;
    private HeightMap heightMap;
    private Boss boss;
    private ArrayList<BaseItem> rocketsShip;
    private ArrayList<BaseItem> rocketsBoss;
    private boolean isInit = false;
    private Joystick joystick;
    private Controls controls;

    @Override
    public void init(Context context, Ship ship, float levelLimitSize, Joystick joystick, Controls controls) {
        this.context = context;
        this.ship = ship;
        this.heightMap = new HeightMap(context, R.drawable.canyon_6_hm_2, R.drawable.canyon_6_tex_2, 0.025f, 0.8f, 3e-5f, levelLimitSize, -100f);
        this.levelLimits = new LevelLimits(levelLimitSize / 2f, -levelLimitSize / 2f, levelLimitSize / 2f, -100f, levelLimitSize / 2f, -levelLimitSize / 2f);
        this.boss = new Boss(this.context, "trump.obj", "trump.mtl", 15, new float[]{0f, 0f, 50f});
        this.rocketsShip = new ArrayList<>();
        this.rocketsBoss = new ArrayList<>();
        this.joystick = joystick;
        this.controls = controls;
        this.isInit = true;
    }

    @Override
    public void draw(float[] mProjectionMatrix, float[] mViewMatrix, float[] mLightPosInEyeSpace, float[] mCameraPosition) {
        this.ship.draw(mProjectionMatrix, mViewMatrix, mLightPosInEyeSpace, mCameraPosition);
        ArrayList<BaseItem> tmp = new ArrayList<>(this.rocketsShip);
        for (BaseItem r : tmp)
            r.draw(mProjectionMatrix, mViewMatrix, mLightPosInEyeSpace, mCameraPosition);
        tmp = new ArrayList<>(this.rocketsBoss);
        for (BaseItem r : tmp)
            r.draw(mProjectionMatrix, mViewMatrix, mLightPosInEyeSpace, mCameraPosition);
        this.boss.draw(mProjectionMatrix, mViewMatrix, mLightPosInEyeSpace, mCameraPosition);
        this.heightMap.draw(mProjectionMatrix, mViewMatrix, mLightPosInEyeSpace);
    }

    @Override
    public void drawLevelInfo(float ratio) {

    }

    @Override
    public void update() {
        float[] tmp = this.joystick.getStickPosition();
        this.ship.updateMaxSpeed(this.controls.getBoost());
        this.ship.move(tmp[0], tmp[1]);
        if (controls.isFire()) {
            this.ship.fire(this.rocketsShip);
            controls.turnOffFire();
        }
        ArrayList<BaseItem> tmpArr = new ArrayList<>();
        tmpArr.addAll(this.rocketsShip);
        for (BaseItem r : tmpArr)
            r.move();
        tmpArr = new ArrayList<>();
        tmpArr.addAll(this.rocketsBoss);
        for (BaseItem r : tmpArr)
            r.move();
        this.boss.move(this.ship);
        this.boss.fire(this.rocketsBoss, this.ship);
    }

    @Override
    public void collide() {
        ArrayList<BaseItem> ami = new ArrayList<>();
        ami.addAll(this.rocketsShip);
        ami.add(this.ship);
        ArrayList<BaseItem> ennemi = new ArrayList<>();
        ennemi.addAll(this.rocketsBoss);
        ennemi.add(this.boss);
        Octree octree = new Octree(this.levelLimits, null, ami, ennemi, 8f);
        octree.computeOctree();
    }

    @Override
    public boolean isInit() {
        return this.isInit;
    }

    @Override
    public void removeObjects() {
        for (int i = this.rocketsShip.size() - 1; i >= 0; i--)
            if (!this.rocketsShip.get(i).isAlive() || this.rocketsShip.get(i).isOutOfBound(this.levelLimits))
                this.rocketsShip.remove(i);
        for (int i = this.rocketsBoss.size() - 1; i >= 0; i--)
            if (!this.rocketsBoss.get(i).isAlive() || this.rocketsBoss.get(i).isOutOfBound(this.levelLimits))
                this.rocketsBoss.remove(i);
    }

    @Override
    public int getScore() {
        return this.boss.isAlive() ? 0 : 100;
    }

    @Override
    public boolean isDead() {
        return this.ship.isOutOfBound(this.levelLimits) || !this.ship.isAlive();
    }

    @Override
    public boolean isWinner() {
        return !this.boss.isAlive();
    }
}
