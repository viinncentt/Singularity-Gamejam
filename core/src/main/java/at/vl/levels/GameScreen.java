package at.vl.levels;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import at.vl.Main;

public abstract class GameScreen implements Screen {
    // Main
    protected Main main;

    // Textures
    protected AssetManager assetManager;
    protected SpriteBatch batch;

    // View
    protected FitViewport fitViewport;
    protected OrthographicCamera camera;
    protected float worldWidth;
    protected float worldHeight;

    // Constructor
    public GameScreen(Main main) {
        this.main = main;

        worldWidth = main.getWorldWidth();
        worldHeight = main.getWorldHeight();
        camera = new OrthographicCamera();
        camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f);

        fitViewport = new FitViewport(worldWidth, worldHeight, camera);

        batch = new SpriteBatch();
        assetManager = main.getAssetManager();
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
