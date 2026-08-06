package at.vl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.JsonReader;

import at.vl.levels.TestScreen;
import at.vl.util.JsonHelper;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private AssetManager assetManager;

    // Resolution 320×180
    private float tileSize;
    private float worldWidth;
    private float worldHeight;

    // Json reader
    private JsonReader reader;


    @Override
    public void create() {
        assetManager = new AssetManager();
        reader = new JsonReader();
        JsonHelper.setMain(this);

        // Set configs
        tileSize = JsonHelper.getConfigValue().getFloat("TileSize");

        // Convert to world unit by dividing with tile size
        worldWidth = JsonHelper.getConfigValue().getFloat("WorldWidth") / tileSize;
        worldHeight = JsonHelper.getConfigValue().getFloat("WorldHeight") / tileSize;


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

    public JsonReader getReader() {
        return reader;
    }
}
