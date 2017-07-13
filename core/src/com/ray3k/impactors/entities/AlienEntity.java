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

public class AlienEntity extends Entity implements Enemy {
    private static final float MIN_SPEED = 50.0f;
    private static final float MAX_SPEED = 150.0f;
    private static final float FIRING_RATE = 2.0f;
    private static final float ASTEROID_KILL_RANGE = 100.0f;
    private static final float SPAWN_KILL_ZONE = 50.0f;
    private static final float BULLET_SPEED = 400.0f;
    private static final float BULLET_DELAY = 1.0f;
    private float bulletTimer;
    
    private GameState gameState;

    public AlienEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        create();
    }
    
    @Override
    public void create() {
        setTextureRegion(getAlienTexture());
        getCollisionBox().setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
        setOffsetX(getTextureRegion().getRegionWidth() / 2.0f);
        setOffsetY(getTextureRegion().getRegionHeight() / 2.0f);
        setMotion(MathUtils.random(MIN_SPEED, MAX_SPEED), MathUtils.random(360.0f));
        setCheckingCollisions(true);
        getSFXalien().play();
        bulletTimer = FIRING_RATE;
        
        if (gameState != null) {
            for (Entity entity : gameState.getEntityManager().getEntities()) {
                if (entity instanceof AsteroidEntity) {
                    if (getPosition().dst(entity.getPosition()) < SPAWN_KILL_ZONE) {
                        ((AsteroidEntity) entity).setSpawnChildren(false);
                        entity.dispose();
                    }
                }
            }
        }
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
        
        bulletTimer -= delta;
        if (bulletTimer < 0) {
            bulletTimer = FIRING_RATE;
            setMotion(getSpeed(), MathUtils.random(360.0f));
            
            Entity candidate = null;
            float candidateDistance = ASTEROID_KILL_RANGE;
            for (Entity entity : gameState.getEntityManager().getEntities()) {
                if (entity instanceof AsteroidEntity) {
                    float distance = getPosition().dst(entity.getPosition());
                    if (distance < candidateDistance) {
                        candidateDistance = distance;
                        candidate = entity;
                    }
                }
            }

            if (candidate == null) {
                for (Entity entity : gameState.getEntityManager().getEntities()) {
                    if (entity instanceof PlayerEntity) {
                        candidate = entity;
                    }
                }
            }

            if (candidate != null) {
                getSFXlaser().play();
                bulletTimer = BULLET_DELAY;

                BulletEntity bullet = new BulletEntity(gameState);
                bullet.setParent(this);
                bullet.setPosition(getX() + getTextureRegion().getRegionWidth() / 2.0f, getY() + getTextureRegion().getRegionHeight() / 2.0f);
                bullet.setMotion(BULLET_SPEED, (float) (Math.atan2(candidate.getY() - getY(), candidate.getX() - getX()) * 180.0f / Math.PI));
                bullet.addMotion(getSpeed(), getDirection());
                bullet.setLifeCounter(BULLET_DELAY);
            }
        }
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
        spawnDusts(3);
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BulletEntity) {
            if (((BulletEntity) other).getParent() != this) {
                other.dispose();
                dispose();
                gameState.addScore(10);
                getSFXexplosion().play();
            }
        } else if (other instanceof AsteroidEntity && !other.isDestroyed()) {
            dispose();
            other.dispose();
        }
    }
    
    private TextureRegion getAlienTexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/enemies");
        
        return getCore().getAtlas().findRegion(names.random());
    }
    
    public void spawnDusts(int number) {
        for (int i = 0; i < number; i++) {            
            DustEntity dust = new DustEntity(gameState);
            dust.setPosition(getX(), getY());
        }
    }
    
    private Sound getSFXexplosion() {
        Array<String> names = getCore().getSoundPacks().get(DATA_PATH + "/sfx-explosion");
        
        return getCore().getAssetManager().get(names.random(), Sound.class);
    }
    
    private Sound getSFXalien() {
        Array<String> names = getCore().getSoundPacks().get(DATA_PATH + "/sfx-ufo");
        
        return getCore().getAssetManager().get(names.random(), Sound.class);
    }
    
    private Sound getSFXlaser() {
        Array<String> names = getCore().getSoundPacks().get(DATA_PATH + "/sfx-laser");
        
        return getCore().getAssetManager().get(names.random(), Sound.class);
    }
}
