package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import at.vl.ecs.components.Player;
import at.vl.util.JsonHelper;

public class PlayerHudSystem extends IteratingSystem {
    private ComponentMapper<Player> playerMapper;
    private Player player;

    // Libgdx Hud
    private Stage stage;
    private FitViewport fitViewport;

    private Table tableHearts;
    private Texture heartTexture;
    private TextureRegionDrawable heartDrawable;

    public PlayerHudSystem() {
        super(Aspect.all(Player.class));

        fitViewport = new FitViewport(JsonHelper.getConfigValue().getFloat("WorldWidth"), JsonHelper.getConfigValue().getFloat("WorldHeight"), new OrthographicCamera());

        stage = new Stage(fitViewport);

        tableHearts = new Table();
        tableHearts.top();
        tableHearts.pad(5f);
        tableHearts.left();
        tableHearts.setFillParent(true);

        heartTexture = new Texture("player/heart.png");
        heartDrawable = new TextureRegionDrawable(new TextureRegion(heartTexture));

        stage.addActor(tableHearts);
    }


    @Override
    protected void process(int entityId) {
        player = playerMapper.get(entityId);

        stage.act(world.getDelta());

        tableHearts.clearChildren();
        for (int i = 0; i < player.currentHealth; i++) {
            tableHearts.add(new Image(heartDrawable)).size(70f, 70f).padRight(4f);
        }

        stage.draw();
    }

    public void resize(int width, int height) {
        fitViewport.update(width, height, true); // true = center the camera
    }

    @Override
    protected void dispose() {
        heartTexture.dispose();
        stage.dispose();
    }
}
