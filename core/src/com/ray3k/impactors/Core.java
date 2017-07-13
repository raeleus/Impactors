package com.ray3k.impactors;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.ray3k.impactors.states.GameOverState;
import com.ray3k.impactors.states.GameState;
import com.ray3k.impactors.states.LoadingState;
import com.ray3k.impactors.states.MenuState;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

public class Core extends ApplicationAdapter {
    public final static String VERSION = "1";
    public final static String DATA_PATH = "impactors_data";
    private final static long MS_PER_UPDATE = 10;
    private AssetManager assetManager;
    private StateManager stateManager;
    private SpriteBatch spriteBatch;
    private PixmapPacker pixmapPacker;
    private ObjectMap<String, Array<String>> imagePacks;
    private ObjectMap<String, Array<String>> soundPacks;
    private long previous;
    private long lag;
    private TextureAtlas atlas;

    @Override
    public void create() {
        try {
            initManagers();

            loadAssets();

            previous = TimeUtils.millis();
            lag = 0;

            stateManager.loadState("loading");
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Library Invaders Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }
    
    public void initManagers() {
        assetManager = new AssetManager(new LocalFileHandleResolver(), true);
        
        imagePacks = new ObjectMap<String, Array<String>>();
        for (String name : new String[] {"asteroids", "asteroids-m", "asteroids-s", "bullets", "dusts", "enemies", "players", "players-thrust", "scraps"}) {
            imagePacks.put(DATA_PATH + "/" + name, new Array<String>());
        }
        
        soundPacks = new ObjectMap<String, Array<String>>();
        for (String name : new String[] {"sfx-explosion", "sfx-gameover", "sfx-intro", "sfx-laser", "sfx-thruster", "sfx-ufo"}) {
            soundPacks.put(DATA_PATH + "/" + name, new Array<String>());
        }
        
        stateManager = new StateManager(this);
        stateManager.addState("loading", new LoadingState("menu", this));
        stateManager.addState("menu", new MenuState(this));
        stateManager.addState("game", new GameState(this));
        stateManager.addState("game-over", new GameOverState(this));
        
        spriteBatch = new SpriteBatch();
        
        pixmapPacker = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 5, true, new PixmapPacker.GuillotineStrategy());
    }
    
    @Override
    public void render() {
        try {
            long current = TimeUtils.millis();
            long elapsed = current - previous;
            previous = current;
            lag += elapsed;

            while (lag >= MS_PER_UPDATE) {
                stateManager.act(MS_PER_UPDATE / 1000.0f);
                lag -= MS_PER_UPDATE;
            }

            stateManager.draw(spriteBatch, lag / MS_PER_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Game Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        stateManager.dispose();
        pixmapPacker.dispose();
        if (atlas != null) {
            atlas.dispose();
        }
    }
    
    public void loadAssets() {
        assetManager.clear();
        
        assetManager.load(DATA_PATH + "/skin/impactors-ui.json", Skin.class);
        
        for (String directory : imagePacks.keys()) {
            FileHandle folder = Gdx.files.local(directory);
            for (FileHandle file : folder.list()) {
                assetManager.load(file.path(), Pixmap.class);
                imagePacks.get(directory).add(file.nameWithoutExtension());
            }
        }
        
        for (String directory : soundPacks.keys()) {
            FileHandle folder = Gdx.files.local(directory);
            for (FileHandle file : folder.list()) {
                assetManager.load(file.path(), Sound.class);
                soundPacks.get(directory).add(file.path());
            }
        }
        
        assetManager.load(DATA_PATH + "/gfx/white.png", Pixmap.class);
        
        assetManager.load(DATA_PATH + "/sfx/menu.wav", Sound.class);
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resize(int width, int height) {
        stateManager.resize(width, height);
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public PixmapPacker getPixmapPacker() {
        return pixmapPacker;
    }

    public ObjectMap<String, Array<String>> getImagePacks() {
        return imagePacks;
    }
    
    public ObjectMap<String, Array<String>> getSoundPacks() {
        return soundPacks;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }
}
