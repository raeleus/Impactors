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
package com.ray3k.impactors.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.impactors.Core;
import com.ray3k.impactors.Entity;
import com.ray3k.impactors.EntityManager;
import com.ray3k.impactors.InputManager;
import com.ray3k.impactors.State;
import com.ray3k.impactors.entities.AlienEntity;
import com.ray3k.impactors.entities.AsteroidEntity;
import com.ray3k.impactors.entities.PlayerEntity;

public class GameState extends State {
    private String selectedCharacter;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private EntityManager entityManager;
    private int difficulty;
    private final static float ALIEN_DELAY = 20.0f;
    private float alienTimer;
    public static GameState gameState;
    
    public GameState(Core core) {
        super(core);
        gameState = this;
    }
    
    @Override
    public void start() {
        score = 0;
        
        inputManager = new InputManager();
        
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.apply();
        
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/skin/impactors-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        PlayerEntity player = new PlayerEntity(this);
        player.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f);
        
        difficulty = 3;
        spawnAsteroids(difficulty, AsteroidEntity.Type.LARGE);
        
        getSound("intro").play();
        
        alienTimer = ALIEN_DELAY;
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        boolean createAsteroids = true;
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof AsteroidEntity) {
                createAsteroids = false;
                break;
            }
        }
        
        if (createAsteroids) {
            difficulty++;
            spawnAsteroids(difficulty, AsteroidEntity.Type.LARGE);
        }
        
        alienTimer -= delta;
        if (alienTimer < 0) {
            alienTimer = ALIEN_DELAY;
            spawnAlien();
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }
    
    Vector2 temp = new Vector2();
    
    public void spawnAsteroids(int number, AsteroidEntity.Type type) {
        PlayerEntity player = null;
        
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof PlayerEntity) {
                player = (PlayerEntity) entity;
                break;
            }
        }
        
        if (player != null) {
            for (int i = 0; i < number; i++) {
                temp.set(400, 0);
                temp.rotate(MathUtils.random(360.0f));

                AsteroidEntity ast = new AsteroidEntity(this, type);
                float x = player.getX() + temp.x;
                float y = player.getY() + temp.y;
                
                if (x < 0) {
                    x = Gdx.graphics.getWidth() + x;
                } else if (x > Gdx.graphics.getWidth()) {
                    x -= Gdx.graphics.getWidth();
                }
                
                if (y < 0) {
                    y = Gdx.graphics.getHeight() + y;
                } else if (y > Gdx.graphics.getHeight()) {
                    y -= Gdx.graphics.getHeight();
                }
                
                ast.setPosition(x, y);
            }
        }
    }
    
    public void spawnAlien() {
        PlayerEntity player = null;
        
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof PlayerEntity) {
                player = (PlayerEntity) entity;
                break;
            }
        }
        
        if (player != null) {
            temp.set(400, 0);
            temp.rotate(MathUtils.random(360.0f));

            AlienEntity alien = new AlienEntity(this);
            float x = player.getX() + temp.x;
            float y = player.getY() + temp.y;

            if (x < 0) {
                x = Gdx.graphics.getWidth() + x;
            } else if (x > Gdx.graphics.getWidth()) {
                x -= Gdx.graphics.getWidth();
            }

            if (y < 0) {
                y = Gdx.graphics.getHeight() + y;
            } else if (y > Gdx.graphics.getHeight()) {
                y -= Gdx.graphics.getHeight();
            }

            alien.setPosition(x, y);
        }
    }
    
    public Sound getSound(String name) {
        return getCore().getSounds().get(name);
    }
}