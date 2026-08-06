package at.vl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

import at.vl.levels.TestScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private AssetManager assetManager;

    // Resolution 320×180
    private final float tileSize = 16f;
    private final float worldWidth = 320f / tileSize;
    private final float worldHeight = 180f / tileSize;

    @Override
    public void create() {
        assetManager = new AssetManager();

        setScreen(new TestScreen(this));
    }

    @Override
    public void dispose() {
        if (screen != null) screen.hide();
        assetManager.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }
}
