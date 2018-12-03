/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.impactors.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import static com.ray3k.impactors.Core.DATA_PATH;
import com.ray3k.impactors.Entity;
import com.ray3k.impactors.states.GameState;

public class AsteroidEntity extends Entity implements Enemy {
    private static final float MIN_SPEED = 50.0f;
    private static final float MAX_SPEED = 150.0f;
    private static final float MIN_ROTATION_SPEED = 25.0f;
    private static final float MAX_ROTATION_SPEED = 100.0f;
    private float rotationSpeed;
    private boolean spawnChildren;
    
    public static enum Type {
        LARGE, MEDIUM, SMALL
    }
    private Type type;
    private GameState gameState;

    public AsteroidEntity(GameState gameState, Type type) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.type = type;
        this.gameState = gameState;
        create();
    }
    
    @Override
    public void create() {
        if (type == Type.LARGE) {
            setTextureRegion(getAsteroidTexture());
            getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
            setOffsetX(getTextureRegion().getRegionWidth() / 2.0f);
            setOffsetY(getTextureRegion().getRegionHeight() / 2.0f);
        } else if (type == Type.MEDIUM) {
            setTextureRegion(getAsteroidMtexture());
            getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());            
            setOffsetX(getTextureRegion().getRegionWidth() / 2.0f);
            setOffsetY(getTextureRegion().getRegionHeight() / 2.0f);
        } else if (type == Type.SMALL) {
            setTextureRegion(getAsteroidStexture());
            getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
            setOffsetX(getTextureRegion().getRegionWidth() / 2.0f);
            setOffsetY(getTextureRegion().getRegionHeight() / 2.0f);
        }
        setMotion(MathUtils.random(MIN_SPEED, MAX_SPEED), MathUtils.random(360.0f));
        setCheckingCollisions(true);
        rotationSpeed = MathUtils.randomSign() * MathUtils.random(MIN_ROTATION_SPEED, MAX_ROTATION_SPEED);
        spawnChildren = true;
    }

    @Override
    public void act(float delta) {
        if (getX() + getTextureRegion().getRegionWidth() < 0) {
            setX(Gdx.graphics.getWidth());
        } else if (getX() > Gdx.graphics.getWidth()) {
            setX(-getTextureRegion().getRegionWidth());
        }
        
        if (getY() + getTextureRegion().getRegionHeight() < 0) {
            setY(Gdx.graphics.getHeight());
        } else if (getY() > Gdx.graphics.getHeight()) {
            setY(-getTextureRegion().getRegionHeight());
        }
        
        addRotation(rotationSpeed * delta);
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
        if (type == Type.LARGE) {
            if (spawnChildren) {
                spawnAsteroids(3, Type.MEDIUM);
            }
        } else if (type == Type.MEDIUM) {
            if (spawnChildren) {
                spawnAsteroids(3, Type.SMALL);
            }
        } else {
            spawnDusts(3);
        }
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BulletEntity) {
            ((BulletEntity) other).dispose();
            dispose();
            gameState.addScore(10);
            gameState.getSound("explosion").play();
        }
    }
    
    private TextureRegion getAsteroidTexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/asteroids");
        
        return getCore().getAtlas().findRegion(names.random());
    }
    private TextureRegion getAsteroidMtexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/asteroids-m");
        
        return getCore().getAtlas().findRegion(names.random());
    }
    
    private TextureRegion getAsteroidStexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/asteroids-s");
        
        return getCore().getAtlas().findRegion(names.random());
    }
    
    public void spawnAsteroids(int number, AsteroidEntity.Type type) {
        for (int i = 0; i < number; i++) {            
            AsteroidEntity ast = new AsteroidEntity(gameState, type);
            ast.setPosition(getX(), getY());
        }
    }
    
    public void spawnDusts(int number) {
        for (int i = 0; i < number; i++) {            
            DustEntity dust = new DustEntity(gameState);
            dust.setPosition(getX(), getY());
        }
    }

    public boolean isSpawnChildren() {
        return spawnChildren;
    }

    public void setSpawnChildren(boolean spawnChildren) {
        this.spawnChildren = spawnChildren;
    }
}
