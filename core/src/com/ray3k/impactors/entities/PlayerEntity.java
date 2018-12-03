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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import static com.ray3k.impactors.Core.DATA_PATH;
import com.ray3k.impactors.Entity;
import com.ray3k.impactors.InputManager;
import com.ray3k.impactors.states.GameState;

public class PlayerEntity extends Entity implements InputManager.KeyActionListener {
    private GameState gameState;
    private TextureRegion playerRegion;
    private TextureRegion thrustRegion;
    private static final float ROT_SPEED = -300.0f;
    private static final float THRUST_SPEED = 500.0f;
    private static final float MAX_SPEED = 1000.0f;
    private static final float BULLET_DELAY = .1f;
    private static final float BULLET_SPEED = 700.0f;
    private float bulletTimer;
    private static final float BULLET_LIFE = 1.0f;
    
    public PlayerEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
    }

    @Override
    public void create() {
        bulletTimer = -1.0f;
        
        setCheckingCollisions(true);
        playerRegion = getPlayerTexture();
        setOffsetX(playerRegion.getRegionWidth() / 2.0f);
        setOffsetY(playerRegion.getRegionHeight() / 2.0f);
        thrustRegion = getPlayerThrustTexture();
        
        setTextureRegion(playerRegion);
        
        setX(Gdx.graphics.getWidth() / 2.0f - getTextureRegion().getRegionWidth() / 2.0f);
        setY(30.0f);
        
        getCollisionBox().setSize(getTextureRegion().getRegionWidth() / 2.0f, getTextureRegion().getRegionHeight() / 2.0f);
        setCollisionBoxX(getTextureRegion().getRegionWidth() / 4.0f);
        setCollisionBoxY(getTextureRegion().getRegionHeight() / 4.0f);
        
        ((GameState)getCore().getStateManager().getState("game")).getInputManager().addKeyActionListener(this);
    }

    @Override
    public void act(float delta) {
        bulletTimer -= delta;
        if (bulletTimer < 0) {
            bulletTimer = -1.0f;
        }
        
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            setTextureRegion(thrustRegion);
        } else {
            setTextureRegion(playerRegion);
        }
        
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            addRotation(-ROT_SPEED * delta);
        } else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            addRotation(ROT_SPEED * delta);
        }
        
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            addMotion(THRUST_SPEED * delta, getRotation());
        }
        
        if (getSpeed() > MAX_SPEED) {
            setMotion(MAX_SPEED, getDirection());
        }
        
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
    }

    @Override
    public void act_end(float delta) {
        
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        
    }

    @Override
    public void destroy() {
        gameState.getSound("explosion").play();
        gameState.getSound("game-over").play();
        new WhiteFlashEntity(gameState);
        new GameOverTimerEntity(gameState, 1.0f);
    }

    @Override
    public void collision(Entity other) {
        if (other instanceof BulletEntity) {
            BulletEntity bullet = (BulletEntity) other;
            if (bullet.getParent() != this) {
                bullet.dispose();
                dispose();
            }
        } else if (other instanceof Enemy) {
            other.dispose();
            dispose();
        }
    }

    @Override
    public void keyPressed(int key) {
        if (!isDestroyed()) {
            if (key == Keys.SPACE) {
                if (bulletTimer < 0) {
                    gameState.getSound("laser").play();
                    bulletTimer = BULLET_DELAY;

                    BulletEntity bullet = new BulletEntity(gameState);
                    bullet.setParent(this);
                    bullet.setPosition(getX() + getTextureRegion().getRegionWidth() / 2.0f, getY() + getTextureRegion().getRegionHeight() / 2.0f);
                    bullet.getCollisionBox().setPosition(bullet.getX(), bullet.getY());
                    bullet.setMotion(BULLET_SPEED, getRotation());
                    bullet.addMotion(getSpeed(), getDirection());
                    bullet.setLifeCounter(BULLET_LIFE);
                }
            } else if (key == Keys.UP) {
                gameState.getSound("thruster").play();
            }
        }
    }
    
    private TextureRegion getPlayerTexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/players");
        
        return getCore().getAtlas().findRegion(names.random());
    }
    
    private TextureRegion getPlayerThrustTexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/players-thrust");
        
        return getCore().getAtlas().findRegion(names.random());
    }
}
