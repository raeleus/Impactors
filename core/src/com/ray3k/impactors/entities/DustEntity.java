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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import static com.ray3k.impactors.Core.DATA_PATH;
import com.ray3k.impactors.Entity;
import com.ray3k.impactors.states.GameState;

public class DustEntity extends Entity{
    private static final float MIN_SPEED = 15.0f;
    private static final float MAX_SPEED = 100.0f;
    private static final float LIFE_MIN = .25f;
    private static final float LIFE_MAX = 1.0f;
    private float life;

    public DustEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
    }

    @Override
    public void create() {
        setTextureRegion(getDustTexture());
        life = MathUtils.random(LIFE_MIN, LIFE_MAX);
        setMotion(MathUtils.random(MIN_SPEED, MAX_SPEED), MathUtils.random(360.0f));
    }

    @Override
    public void act(float delta) {
        life -= delta;
        if (life < 0) {
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

    private TextureRegion getDustTexture() {
        Array<String> names = getCore().getImagePacks().get(DATA_PATH + "/dusts");
        
        return getCore().getAtlas().findRegion(names.random());
    }
}
