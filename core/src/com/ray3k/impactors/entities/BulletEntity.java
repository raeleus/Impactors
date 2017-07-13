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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import static com.ray3k.impactors.Core.DATA_PATH;
import com.ray3k.impactors.Entity;
import com.ray3k.impactors.states.GameState;

public class BulletEntity extends Entity {
    private Entity parent;
    private float lifeCounter;
    
    public BulletEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
    }
    
    @Override
    public void create() {
        setTextureRegion(getBullet());
        
        setCheckingCollisions(true);
        getCollisionBox().width = getTextureRegion().getRegionWidth();
        getCollisionBox().height = getTextureRegion().getRegionHeight();
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
        
        lifeCounter -= delta;
        if (lifeCounter < 0) {
            dispose();
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
    }

    @Override
    public void collision(Entity other) {
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }
    
    private TextureRegion getBullet() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/bullets");
        
        return getCore().getAtlas().findRegion(names.random());
    }

    public float getLifeCounter() {
        return lifeCounter;
    }

    public void setLifeCounter(float lifeCounter) {
        this.lifeCounter = lifeCounter;
    }
}
